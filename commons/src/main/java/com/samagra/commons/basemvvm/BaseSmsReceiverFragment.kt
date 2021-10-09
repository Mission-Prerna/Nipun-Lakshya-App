package com.samagra.commons.basemvvm

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.samagra.commons.SmsBroadcastReceiver
import timber.log.Timber

abstract class BaseSmsReceiverFragment<VB : ViewDataBinding, T : BaseViewModel> :
    BaseFragment<VB, T>() {

    private val senderSms = null //You can change to an number
    private val SMS_CONSENT_REQUEST = 200  // Set to an unused request code
    lateinit var smsBroadcastReceiver: SmsBroadcastReceiver

    private fun registerToSmsBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver().also {
            it.smsBroadcastReceiverListener =
                object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
                    override fun onSuccess(intent: Intent?) {
                        Timber.i("registerToSmsBroadcastReceiver - onSuccess ")
                        intent?.let { context ->
                            startActivityForResult(
                                context,
                                SMS_CONSENT_REQUEST
                            )
                        }
                    }

                    override fun onFailure() {
                        Timber.i("registerToSmsBroadcastReceiver - onFailure ")
                        Timber.e("Fail BroadcastReceiver")
                    }
                }
        }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        requireActivity().registerReceiver(smsBroadcastReceiver, intentFilter)
    }

    protected fun startSmsUserConsent() {
        Timber.i("startSmsUserConsent")
        SmsRetriever.getClient(requireActivity()).also {
            //We can add sender phone number or leave it blank
            it.startSmsUserConsent(senderSms)
                .addOnSuccessListener {
                    Timber.i("startSmsUserConsent - onSuccessListener ")
                    Log.d("BaseSmsReceiverFragment", "LISTENING_SUCCESS")
                }
                .addOnFailureListener {
                    Timber.i("startSmsUserConsent - onFailureListener ")
                    Log.d("BaseSmsReceiverFragment", "LISTENING_FAILURE")
                }
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.i("onStart")
        registerToSmsBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop")
        requireActivity().unregisterReceiver(smsBroadcastReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SMS_CONSENT_REQUEST -> {
                Timber.i("onActivityResult")
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    //That gives all message to us. We need to get the code from inside with regex
                    val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                    Timber.i("onActivityResult - message : $message")
                    val code = message?.let { fetchVerificationCode(it) }
                    Timber.i("onActivityResult - code : $code")
                    if (!code.isNullOrEmpty()) {
                        startSmsUserConsent()
                        onCodeReceived(code)
                    }
                }
            }
        }
    }

    private fun fetchVerificationCode(message: String): String {
        return Regex("(\\d{4})").find(message)?.value ?: ""
    }

    abstract fun onCodeReceived(code: String)
}