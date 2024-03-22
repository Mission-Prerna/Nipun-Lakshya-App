package com.samagra.deeplink

import android.app.Activity
import android.os.Bundle
import com.airbnb.deeplinkdispatch.DeepLinkHandler
import com.chatbot.deeplink.ChatbotDeepLinkModule
import com.chatbot.deeplink.ChatbotDeepLinkModuleRegistry

@DeepLinkHandler(AppDeepLinkModule::class, ChatbotDeepLinkModule::class)
class DeepLinkActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deepLinkDelegate =
            DeepLinkDelegate(AppDeepLinkModuleRegistry(), ChatbotDeepLinkModuleRegistry())
        deepLinkDelegate.dispatchFrom(this)
        finish()
    }
}