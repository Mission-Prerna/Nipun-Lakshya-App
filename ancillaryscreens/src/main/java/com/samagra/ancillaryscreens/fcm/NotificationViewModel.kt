package com.samagra.ancillaryscreens.fcm

import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber

class NotificationViewModel : ViewModel() {
    fun registerFCMToken(mentorId: Int) {
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
                postFCMToken(token, mentorId)
            }
    }

    private fun postFCMToken(token: String?, mentorId: Int) {
        NotificationRepository().postFCMToken(token!!, mentorId, {
            Timber.i("Token Uploaded successfully")
        }, {
            Timber.i("Token Upload failed")
            //TODO add exponential backoff
            //postFCMToken(token, mentorId)
        })
    }

}