package com.chatbot

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anggrayudi.storage.file.CreateMode
import com.anggrayudi.storage.media.FileDescription
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.media.MediaStoreCompat
import com.anggrayudi.storage.media.MediaType
import com.chatbot.model.ChatbotTelemetryAction
import com.chatbot.notification.ChatbotNotificationHandler
import com.chatbot.notification.NotificationTelemetryType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.samagra.commons.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import timber.log.Timber

class ChatBotVM : ViewModel() {

    val botsLiveData = MutableLiveData<ChatbotListingState>()

    private val iconVisibilityMutableLiveData = MutableLiveData<BotIconState>()
    val iconVisibilityLiveData: LiveData<BotIconState> = iconVisibilityMutableLiveData

    private val nlFolder = "NipunLakshya"
    private val chatbotBaseUrl = "file:///android_asset/chatbot/"
    val chatbotIndexUrl = "${chatbotBaseUrl}index.html"

    val gson by lazy { Gson() }

    fun saveStarredMessages(savedMsg: String) = AppPreferences.saveStarredMessages(savedMsg)

    fun getInjection(
        botToFocus: String,
        userConfiguredBots: List<String>,
        userStartedBots: List<String>
    ): String {
        val injectionBuilder = StringBuilder()
        val unstartedBots = mutableListOf<String>()
        if (userStartedBots.isNotEmpty()) {
            unstartedBots.addAll(userConfiguredBots.minus(userStartedBots.toSet()))
        }
        Timber.d("getInjection: configured: $userConfiguredBots")
        Timber.d("getInjection: started: $userStartedBots")
        Timber.d("getInjection: unstarted: $unstartedBots")

        injectionBuilder.append(
            "<script type='text/javascript'>"
                + "localStorage.setItem('auth', '${AppPreferences.getUserAuth()}' );"
                + "localStorage.setItem('mobile', '${AppPreferences.getUserMobile()}');"
                + "localStorage.setItem('botList','${gson.toJson(userConfiguredBots)}' );"
                + "localStorage.setItem('unstartedBotList','${gson.toJson(unstartedBots)}' );"
                + "localStorage.setItem('starredChats', '${AppPreferences.getStarredMsgs()}');"
        )

        if (botToFocus.isNotEmpty())
            injectionBuilder.append("localStorage.setItem('botToFocus', '$botToFocus' );")

        ChatBotRepository.getChatbotUrls().forEach {
            injectionBuilder.append("localStorage.setItem('${it.key}', '${it.value}' );")
        }

        injectionBuilder.append("window.location.replace('${chatbotIndexUrl}');" + "</script>")
        return injectionBuilder.toString()
    }

    fun clearLocalStorage() = AppPreferences.clearLocal()

    fun fetchConfiguredBots() {
        viewModelScope.launch {
            try {
                val userConfiguredBots = ChatBotRepository.fetchMentorBots()
                if (userConfiguredBots.isNullOrEmpty()) {
                    val botList = cachedChatBotList()
                    botsLiveData.postValue(
                        ChatbotListingState.Success(
                            userConfiguredBots = botList,
                            userStartedBots = botList
                        )
                    )
                } else {
                    val userStartedBots = getUserStartedBots(backupBots = userConfiguredBots)
                    botsLiveData.postValue(
                        ChatbotListingState.Success(
                            userConfiguredBots = userConfiguredBots,
                            userStartedBots = userStartedBots
                        )
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)
                botsLiveData.postValue(
                    ChatbotListingState.Failure(
                        reason = R.string.failed_load_bot,
                        destructive = true
                    )
                )
            }
        }
    }

    private suspend fun getUserStartedBots(backupBots: List<String>): List<String> {
        return try {
            val startedBots = ChatBotRepository.getChatbotsWithAction(ChatbotTelemetryAction.STARTED)
            if (startedBots.isNullOrEmpty()) {
                if (startedBots?.isEmpty() == true) {
                    //If no bots are started yet, this is the first launch for the user.
                    // Mark all bots as started
                    ChatBotRepository.setBotsWithAction(
                        botIds = backupBots,
                        action = ChatbotTelemetryAction.STARTED
                    )
                }
                backupBots
            } else {
                startedBots
            }
        } catch (t: Throwable) {
            Timber.e(t, "getUserStartedBots: ${t.message}")
            backupBots
        }
    }

    fun logNotificationReadTelemetry(context: Context, data: Map<String, String>) {
        ChatbotNotificationHandler.triggerNotificationTelemetry(
            context = context,
            type = NotificationTelemetryType.READ,
            messageData = data
        )
    }

    fun getPropertiesMapFromJson(eventProperties: String?): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        if (eventProperties.isNullOrEmpty().not()) {
            val eventPropertiesObject: Map<String, String> = gson.fromJson(
                eventProperties,
                object : TypeToken<Map<String, String>>() {}.type)
            eventPropertiesObject.keys.forEach {
                result[it] = eventPropertiesObject.getOrDefault(it, "")
            }
        }
        result["userId"] = AppPreferences.getUserId()
        Timber.d("getPropertiesMapFromJson: map: $result")
        return result
    }

    fun getUserIdMap(): String = gson.toJson(mapOf(getUserIdPair()))
    fun getUserIdPair() = "userId" to AppPreferences.getUserId()

    fun identifyChatIconState() {
        viewModelScope.launch {
            val isChatBotVisibilityEnabled = ChatBotRepository.isChatbotEnabledForActor()
            Timber.d("identifyChatIconState: visibility: $isChatBotVisibilityEnabled")
            if (isChatBotVisibilityEnabled) {
                val remoteBots = try {
                    ChatBotRepository.fetchMentorBots()
                } catch (e: Exception) {
                    Timber.e(e, "identifyChatIconState: API failed")
                    null
                }
                Timber.d("identifyChatIconState: $remoteBots")
                val userBots = if (remoteBots.isNullOrEmpty()) {
                    cachedChatBotList()
                } else {
                    remoteBots
                }
                val userStartedBots = getUserStartedBots(backupBots = userBots)
                ChatBotRepository.setUserConfiguredBots(userBots)
                ChatBotRepository.setUserStartedBots(userStartedBots)
                iconVisibilityMutableLiveData.postValue(
                    BotIconState.Show(animate = userBots.size > userStartedBots.size)
                )
            } else {
                iconVisibilityMutableLiveData.postValue(BotIconState.Hide)
            }
        }
    }

    private fun cachedChatBotList(): List<String> =
        gson.fromJson(AppPreferences.chatBotList, object : TypeToken<List<String>>() {}.type)

    fun onConversationStarted(botId: String) {
        viewModelScope.launch {
            ChatBotRepository.setBotWithAction(
                botId = botId,
                action = ChatbotTelemetryAction.STARTED
            )
        }
    }

    fun userConfiguredBots() = ChatBotRepository.getUserConfiguredBots()
    fun userStartedBots() = ChatBotRepository.getUserStartedBots()
    fun isWebLink(url: String): Pair<Boolean, String?> {
        val webLink = url.removePrefix(chatbotBaseUrl)
        return if (webLink.contains("http")) {
            Pair(true, webLink)
        } else {
            Pair(false, null)
        }
    }

    fun getAssetUri(
        context: ChatBotActivity,
        url: String,
        type: DownloadableType
    ): Uri? {
        val mediaType = when (type) {
            DownloadableType.IMAGE -> MediaType.IMAGE
            DownloadableType.VIDEO -> MediaType.VIDEO
            else -> MediaType.DOWNLOADS
        }
        val assetName = url.substringBefore("?").substringAfterLast("/")
        return MediaStoreCompat.fromFileName(
            context = context,
            mediaType = mediaType,
            name = assetName
        )?.uri
    }

    fun downloadAsset(
        context: Context,
        url: String,
        type: DownloadableType,
        assetId: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = ChatBotRepository.downloadAsset(url)
            if (response != null) {
                val assetName = url.substringBefore("?").substringAfterLast("/")
                val file = saveDownloadedFile(
                    assetName = assetName,
                    type = type,
                    context = context,
                    response = response
                )
                if (file != null) {
                    Timber.d("downloadAsset: success!")
                    botsLiveData.postValue(
                        ChatbotListingState.OpenAsset(
                            assetId = assetId,
                            fileToOpen = file.uri,
                            type = type
                        )
                    )
                    return@launch
                } else {
                    Timber.e("downloadAsset: File could not be created for asset $assetName")
                }
            } else {
                Timber.e("downloadAsset: download came null: $url")
            }
            botsLiveData.postValue(
                ChatbotListingState.Failure(
                    reason = R.string.failed_download,
                    destructive = false
                )
            )
        }
    }

    private fun saveDownloadedFile(
        assetName: String,
        type: DownloadableType,
        context: Context,
        response: ResponseBody
    ): MediaFile? {
        val downloadFileDescriptor = FileDescription(
            name = assetName,
            subFolder = nlFolder,
            mimeType = type.mimeType)
        Timber.d("downloadAsset: assetname: $assetName")
        val file = when (type) {
            DownloadableType.IMAGE -> {
                Timber.d("downloadAsset: Image")
                MediaStoreCompat.createImage(
                    context = context,
                    file = downloadFileDescriptor,
                    mode = CreateMode.REUSE,
                )
            }

            DownloadableType.VIDEO -> {
                Timber.d("downloadAsset: video")
                MediaStoreCompat.createVideo(
                    context = context,
                    file = downloadFileDescriptor,
                    mode = CreateMode.REUSE
                )
            }

            DownloadableType.DOC -> {
                Timber.d("downloadAsset: doc")
                MediaStoreCompat.createDownload(
                    context = context,
                    file = downloadFileDescriptor,
                    mode = CreateMode.REUSE,
                )
            }

            else -> {
                Timber.d("downloadAsset: else case")
                MediaStoreCompat.createDownload(
                    context = context,
                    file = downloadFileDescriptor,
                    mode = CreateMode.REUSE,
                )
            }
        }
        response.byteStream().use { inputStream ->
            Timber.d("downloadAsset: bytestream")
            file?.openOutputStream(append = true)?.use { targetOutputStream ->
                Timber.d("downloadAsset: file output stream")
                inputStream.copyTo(targetOutputStream)
            }
        }
        return file
    }
}


sealed class BotIconState {
    object Hide : BotIconState()
    class Show(val animate: Boolean) : BotIconState()
}

sealed class ChatbotListingState {

    data class Failure(
        @StringRes val reason: Int,
        val destructive: Boolean
    ) : ChatbotListingState()

    data class Success(
        val userConfiguredBots: List<String>,
        val userStartedBots: List<String>
    ) : ChatbotListingState()

    data class OpenAsset(
        val assetId: String,
        val fileToOpen: Uri,
        val type: DownloadableType
    ) : ChatbotListingState()
}
