package com.chatbot

import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.deeplinkdispatch.DeepLink
import com.chatbot.databinding.ActivityChatbotBinding
import com.samagra.commons.constants.DeeplinkConstants
import com.samagra.commons.constants.DeeplinkConstants.Queries.BOT_ID
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import timber.log.Timber
import java.util.*


@DeepLink("${DeeplinkConstants.CHATBOT}?${BOT_ID}={botId}")
class ChatBotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotBinding

    private val chatVM by viewModels<ChatBotVM>()

    private var botListInFocus = false

    private val botToFocus by lazy {
        intent.extras?.getString(BOT_ID, "") ?: ""
    }

    private var backClickedByUserOnce = false

    //Downloads
    private val downloadedFiles by lazy { mutableMapOf<String, Uri>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startLoading()
        logViewEvent()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }

    override fun onBackPressed() {
        backClickedByUserOnce = true
        if (botListInFocus) {
            finish()
            return
        } else if (binding.webView.canGoBack()) {
            binding.webView.goBack()
            return
        }
        super.onBackPressed()
    }

    @JavascriptInterface
    fun onEvent(eventName: String, eventProperties: String?) {
        Timber.d("onEvent: eventName: $eventName, prop: $eventProperties")
        logPostHogEvent(
            eventName = eventName,
            eventType = EVENT_TYPE_USER_ACTION,
            eventProperties = eventProperties
        )
    }

    @JavascriptInterface
    fun log(message: String) {
        Timber.d("log: $message")
    }

    @JavascriptInterface
    fun onChatCompleted(id: String, response: String) {
        Timber.d("onChatCompleted: id: $id, response: $response")
        chatVM.submitChat(id, response)
    }

    @JavascriptInterface
    fun onMsgSaveUpdate(botId: String, messageId: String, starred: Boolean, savedMsg: String) {
        Timber.d("onMsgSaveUpdate: $savedMsg, bot: $botId, message: $messageId, starred: $starred")
        chatVM.saveStarredMessages(savedMsg)
        logPostHogEvent(
            eventName = "nl-chatbotscreen-starmessage",
            eventType = EVENT_TYPE_USER_ACTION,
            eventPropertiesMap = mapOf(
                "botId" to botId,
                "messageId" to messageId,
                "starred" to starred,
                chatVM.getUserIdPair()
            )
        )
    }

    @JavascriptInterface
    fun onBotDetailsLoaded(botDetailsJson: String) {
        chatVM.saveBotDetails(botDetailsJson)
    }

    @JavascriptInterface
    fun onBotListingScreenFocused(focused: Boolean) {
        botListInFocus = focused
    }

    @JavascriptInterface
    fun onMediaReceived(botId: String, msgId: String) {
        Timber.d("onMediaReceived: bot: $botId, message: $msgId")
        logPostHogEvent(
            eventName = EVENT_CHATBOT_MEDIA_ACCESSED,
            eventType = EVENT_TYPE_SCREEN_VIEW,
            eventPropertiesMap = mapOf(
                BOT_ID to botId,
                "messageId" to msgId
            )
        )
    }

    @JavascriptInterface
    fun onConvStarted(botId: String, timeinstance: String) {
        Timber.d("onConvStarted: $botId, $timeinstance")
        chatVM.onConversationStarted(botId)
    }

    @JavascriptInterface
    fun isAssetDownloaded(
        messageId: String,
        assetId: String,
        url: String,
        type: String
    ): Boolean {
        Timber.d("isAssetDownloaded: messageID: $messageId, assetId: $assetId, url: $url, type: $type")
        val downloadedUri = chatVM.getAssetUri(
            context = this,
            url = url,
            type = DownloadableType.from(type)
        )
        return if (downloadedUri != null) {
            downloadedFiles[assetId] = downloadedUri
            true
        } else {
            false
        }
    }

    @JavascriptInterface
    fun onAssetClicked(
        messageId: String,
        assetId: String,
        url: String,
        type: String
    ) {
        Timber.d("onAssetClicked: messageID: $messageId, assetId: $assetId, url: $url, type: $type")
        val downloadType = DownloadableType.from(type)
        val downloadedFileName = downloadedFiles[assetId]
        val eventName: String
        if (downloadedFileName != null) {
            openAsset(
                asset = downloadedFileName,
                type = downloadType
            )
            eventName = EVENT_CHATBOT_MEDIA_VIEWED
        } else {
            onMainThread {
                binding.progressBar.visibility = View.VISIBLE
            }
            chatVM.downloadAsset(
                context = this,
                assetId = assetId,
                url = url,
                type = downloadType
            )
            eventName = EVENT_CHATBOT_MEDIA_DOWNLOADED
        }
        logPostHogEvent(
            eventName = eventName,
            eventType = EVENT_TYPE_USER_ACTION,
            eventPropertiesMap = mapOf(
                "assetId" to assetId,
                "messageId" to messageId
            ),
            edataType = TYPE_CLICK
        )
    }

    @JavascriptInterface
    fun onTriggerLogout() {
        chatVM.clearLocalStorage()
        binding.progressBar.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            val intent: Intent =
                packageManager.getLaunchIntentForPackage(this.packageName) ?: return@postDelayed
            val componentName: ComponentName? = intent.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
            startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        }, 1000)
    }

    @JavascriptInterface
    fun onDestroyScreen() {
        Timber.d("onDestroyScreen: ")
        finish()
    }

    private fun startLoading() {
        updateWebviewSettings()
        attachClientToWebview()
        val userConfiguredBots = chatVM.userConfiguredBots()
        val userStartedBots = chatVM.userStartedBots()
        addListeners()
        if (userConfiguredBots.isEmpty() || userStartedBots.isEmpty()) {
            chatVM.fetchConfiguredBots()
        } else {
            loadChatbotInWebview(
                userConfiguredBots = userConfiguredBots,
                userStartedBots = userStartedBots
            )
        }
    }

    private fun addListeners() {
        Timber.d("addListeners: parent")
        chatVM.botsLiveData.observe(this) { state ->
            Timber.d("addListeners: $state")
            when (state) {
                is ChatbotListingState.Failure -> {
                    Timber.d("addListeners: failure: $state")
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        getString(state.reason),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (state.destructive) finish()
                }

                is ChatbotListingState.Success -> loadChatbotInWebview(
                    userConfiguredBots = state.userConfiguredBots,
                    userStartedBots = state.userStartedBots
                )

                is ChatbotListingState.OpenAsset -> {
                    downloadedFiles[state.assetId] = state.fileToOpen
                    openAsset(
                        asset = state.fileToOpen,
                        type = state.type
                    )
                }
            }
        }
    }

    private fun updateWebviewSettings() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mixedContentMode =
                WebSettings.MIXED_CONTENT_ALWAYS_ALLOW or WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
        if (BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true)
        binding.webView.addJavascriptInterface(this, "androidInteract")
    }

    private fun logViewEvent() {
        logPostHogEvent(
            eventName = EVENT_CHATBOT_VIEW,
            eventType = EVENT_TYPE_SCREEN_VIEW,
            eventProperties = chatVM.getUserIdMap()
        )

        if (intent.getBooleanExtra(DeepLink.IS_DEEP_LINK, false)) {
            chatVM.logNotificationReadTelemetry(
                context = this,
                data = intent.extras.toStringMapSet()
            )
        }
    }

    private fun attachClientToWebview() {
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                Timber.d("onPageStarted: $url")
                if (backClickedByUserOnce) {
                    // If user has clicked back once remove botToFocus from localstorage.
                    // This is a fix for API 30
                    onMainThread {
                        binding.webView.evaluateJavascript(
                            "localStorage.setItem('botToFocus','null');",
                            null
                        )
                    }
                }
                if (favicon == null) {
                    val icon: Bitmap = BitmapFactory.decodeResource(
                        resources, R.drawable.bot
                    )
                    super.onPageStarted(view, url, icon)
                } else {
                    super.onPageStarted(view, url, favicon)
                }
                binding.progressBar.visibility = View.VISIBLE
                invalidateOptionsMenu()
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Timber.d("shouldOverrideUrlLoading: $url")
                val isWebLink = chatVM.isWebLink(url)
                if (isWebLink.first && isWebLink.second.isNullOrEmpty().not()) {
                    openLinkInBrowser(isWebLink.second!!)
                } else {
                    binding.webView.loadUrl(url)
                }
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView, request: WebResourceRequest, error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadChatbotInWebview(
        userConfiguredBots: List<String>,
        userStartedBots: List<String>
    ) {
        val mimeType = "text/html"
        val encoding = "utf-8"
        val injection = chatVM.getInjection(
            botToFocus = botToFocus,
            userConfiguredBots = userConfiguredBots,
            userStartedBots = userStartedBots
        )
        Timber.d("loadChatbotInWebview: inject: $injection")
        binding.webView.loadDataWithBaseURL(
            chatVM.chatbotIndexUrl, injection, mimeType, encoding, null
        )
    }

    //Assets
    private fun openAsset(asset: Uri, type: DownloadableType) {
        onMainThread {
            binding.progressBar.visibility = View.GONE
            triggerOpen(asset, type.mimeType)
        }
    }

    private fun triggerOpen(asset: Uri, type: String) {
        Timber.d("openDoc: opendoc $asset $type")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(asset, type)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(intent)
        } catch (t: Throwable) {
            Toast.makeText(this, getString(R.string.no_app_found), Toast.LENGTH_SHORT).show()
        }
    }

    private fun onMainThread(function: () -> Unit) {
        Handler(Looper.getMainLooper()).post {
            function.invoke()
        }
    }

    private fun logPostHogEvent(eventName: String, eventType: String, eventProperties: String?) {
        logPostHogEvent(
            eventName = eventName,
            eventType = eventType,
            eventPropertiesMap = chatVM.getPropertiesMapFromJson(eventProperties)
        )
    }

    private fun logPostHogEvent(
        eventName: String,
        eventType: String,
        eventPropertiesMap: Map<String, Any>? = null,
        edataType: String = TYPE_VIEW
    ) {
        val cDataArrayList = arrayListOf<Cdata>()
        eventPropertiesMap?.forEach {
            cDataArrayList.add(
                Cdata(
                    type = it.key,
                    id = it.value.toString()
                )
            )
        }
        val properties = PostHogManager.createProperties(
            page = CHATBOT_SCREEN,
            eventType = eventType,
            eid = EID_IMPRESSION,
            context = PostHogManager.createContext(
                id = APP_ID,
                pid = NL_APP_CHATBOT,
                dataList = cDataArrayList
            ),
            eData = Edata(NL_CHATBOT, edataType),
            objectData = null,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        PostHogManager.capture(
            context = this,
            eventName = eventName,
            properties = properties
        )
    }

    private fun openLinkInBrowser(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

enum class DownloadableType(val mimeType: String) {
    NONE("plain/text"),
    IMAGE("image/*"),
    DOC("application/pdf"),
    VIDEO("video/*");

    companion object {
        fun from(type: String): DownloadableType {
            return when (type.uppercase()) {
                IMAGE.name -> IMAGE
                VIDEO.name -> VIDEO
                DOC.name -> DOC
                else -> NONE
            }
        }
    }
}

private fun Bundle?.toStringMapSet(): Map<String, String> {
    val resultMap = mutableMapOf<String, String>()
    this?.keySet()?.forEach {
        if (this[it] is String) {
            resultMap[it] = this[it] as String
        }
    }
    return resultMap
}
