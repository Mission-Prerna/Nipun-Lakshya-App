package com.samagra.ancillaryscreens.fcm

import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import timber.log.Timber

class NotificationViewModel : ViewModel() {
    fun registerFCMToken(
        prefs: CommonsPrefsHelperImpl
    ) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String> ->
                if (!task.isSuccessful) {
                    Timber
                        .i("NotificationViewModel : Fetching FCM registration token failed : Exception :: " + task.exception)
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Timber.i("NotificationViewModel : Token : $token")
                postFCMToken(
                    token = token,
                    prefs = prefs
                )
            }
    }

    private fun postFCMToken(
        token: String?,
        prefs: CommonsPrefsHelperImpl
    ) {
        if (token.isNullOrEmpty()) return
        NotificationRepository().postFCMToken(
            token = token,
            prefs = prefs,
            onSuccess = {
                Timber.i("Token Uploaded successfully")
            }
        ) {
            Timber.i("Token Upload failed")
            //TODO add exponential backoff
            //postFCMToken(token, mentorId)
        }
    }

}