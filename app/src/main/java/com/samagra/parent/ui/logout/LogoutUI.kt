package com.samagra.parent.ui.logout

import android.content.Context
import com.example.assets.uielements.CustomMessageDialog
import com.samagra.parent.R

object LogoutUI {

    fun confirmLogoutWithSync(context: Context, logoutClicked: () -> Unit) {
        val customDialog = CustomMessageDialog(
            context,
            null,
            context.getString(R.string.sync_data_before_logout),
            null
        )
        customDialog.setOnFinishListener {
            logoutClicked.invoke()
        }
        customDialog.show()
    }

    fun confirmLogout(context: Context?, logoutClicked: () -> Unit) {
        context?.let {
            val customDialog = CustomMessageDialog(
                it, null,
                it.getString(R.string.are_you_sure_want_to_logout),
                null
            )
            customDialog.setOnFinishListener(it.getString(R.string.yes),
                it.getString(com.samagra.ancillaryscreens.R.string.cancel),
                {
                    logoutClicked.invoke()
                }
            ) {
                //handle dismiss
            }
            customDialog.show()
        }
    }
}