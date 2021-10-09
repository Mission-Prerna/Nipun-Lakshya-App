package com.chatbot

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import com.google.android.material.snackbar.Snackbar
import com.samagra.commons.constants.DeeplinkConstants
import com.samagra.commons.constants.DeeplinkConstants.Queries.BOT_ID
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import timber.log.Timber
import java.text.SimpleDateFormat
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

    private val downloadManager by lazy { getSystemService(DOWNLOAD_SERVICE) as DownloadManager }

    private var downloadTriggeredFor = DownloadableType.NONE

    private val onCompleteBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context?, intent: Intent?) {
            Timber.d("onReceive: onComplete")
            handleDownloadComplete()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)
      //  preventNonTeacherAccess()
        startLoading()
        logViewEvent()

        registerReceiver(
            onCompleteBroadcastReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    private fun preventNonTeacherAccess() {
        if (chatVM.isAccessAllowedToUser().not()) {
            finish()
        }
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
    fun onImageDownload(botId: String, imageUrl: String) {
        Timber.d("onImageDownload: bot: $botId, url: $imageUrl")
        triggerDownload(
            botId = botId,
            downloadableType = DownloadableType.IMAGE,
            url = imageUrl
        )
    }

    @JavascriptInterface
    fun onVideoDownload(botId: String, videoUrl: String) {
        Timber.d("onVideoDownload: bot: $botId, url: $videoUrl")
        triggerDownload(
            botId = botId,
            downloadableType = DownloadableType.VIDEO,
            url = videoUrl
        )
    }

    @JavascriptInterface
    fun onPdfDownload(botId: String, pdfUrl: String) {
        Timber.d("onPdfDownload: bot: $botId, url: $pdfUrl")
        triggerDownload(
            botId = botId,
            downloadableType = DownloadableType.DOC,
            url = pdfUrl
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

    private fun startLoading() {
        updateWebviewSettings()
        attachClientToWebview()
        addListeners()
        chatVM.fetchBots(this)
    }

    private fun addListeners() {
        chatVM.botsLiveData.observe(this) {
            if (it.isNullOrEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.failed_load_bot),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                loadChatbotInWebview(it)
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
                binding.webView.loadUrl(url)
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

    private fun loadChatbotInWebview(botIds: String) {
        val mimeType = "text/html"
        val encoding = "utf-8"
        val injection = chatVM.getInjection(botToFocus, botIds)
        Timber.d("loadChatbotInWebview: inject: $injection")
        binding.webView.loadDataWithBaseURL(
            chatVM.chatbotBaseUrl, injection, mimeType, encoding, null
        )
    }

    private fun triggerDownload(botId: String, downloadableType: DownloadableType, url: String) {
        onMainThread {
            Timber.d("triggerDownload: $url")
            try {
                val toastTitle = when (downloadableType) {
                    DownloadableType.NONE -> R.string.downloading
                    DownloadableType.DOC -> R.string.downloading_doc
                    DownloadableType.VIDEO -> R.string.downloading_video
                    DownloadableType.IMAGE -> R.string.downloading_image
                }
                Toast.makeText(this, getString(toastTitle), Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.VISIBLE
                downloadTriggeredFor = downloadableType
                val fileExtension = url
                    .substringBefore(delimiter = "?")
                    .substringAfterLast(delimiter = '.')
                Timber.d("triggerDownload: extension: $fileExtension")
                val fileNamePrefix = when (downloadableType) {
                    DownloadableType.NONE -> "nl_download"
                    DownloadableType.DOC -> "nl_doc_download"
                    DownloadableType.VIDEO -> "nl_video_download"
                    DownloadableType.IMAGE -> "nl_image_download"
                }
                val fileName = "${fileNamePrefix}_${dateString()}.$fileExtension"
                Timber.d("triggerDownload filename: $fileName")
                val request = DownloadManager.Request(Uri.parse(url))
                    .setTitle(fileName)
                    .setDescription("Downloading")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    .setAllowedOverMetered(true)
                downloadManager.enqueue(request)
                logPostHogEvent(
                    eventName = EVENT_CHATBOT_MEDIA_DOWNLOADED,
                    eventType = EVENT_TYPE_USER_ACTION,
                    eventPropertiesMap = mapOf(
                        BOT_ID to botId,
                        "type" to downloadableType.name
                    ),
                    edataType = TYPE_CLICK
                )
            } catch (t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.failed_download), Toast.LENGTH_SHORT)
                    .show()
                Timber.e(t, "triggerDownload: ")
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun dateString() = SimpleDateFormat("dd_MM_yyyy_hh_mm_ss").format(Date())

    private fun handleDownloadComplete() {
        binding.progressBar.visibility = View.GONE
        val msg = when (downloadTriggeredFor) {
            DownloadableType.NONE -> R.string.saved_to_downloads
            DownloadableType.DOC -> R.string.document_saved
            DownloadableType.VIDEO -> R.string.video_saved
            DownloadableType.IMAGE -> R.string.image_saved
        }
        showSnack(
            message = getString(msg),
            action = getString(R.string.view),
            callback = ::showUserDownloads
        )
    }

    private fun showUserDownloads() {
        startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
    }

    private fun showSnack(message: String, action: String, callback: () -> Unit) {
        Snackbar
            .make(
                binding.root,
                message,
                Snackbar.LENGTH_LONG
            )
            .setAction(
                action
            ) {
                callback.invoke()
            }.show()
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
}

enum class DownloadableType {
    NONE, IMAGE, DOC, VIDEO
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
