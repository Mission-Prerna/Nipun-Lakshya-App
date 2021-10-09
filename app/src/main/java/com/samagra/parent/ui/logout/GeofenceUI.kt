package com.samagra.parent.ui.logout

import android.app.Activity
import android.content.Context
import com.example.assets.uielements.CustomMessageDialog
import com.example.assets.uielements.CustomMessageDialog.CallbackListner
import com.samagra.commons.models.geofencing.LocationMismatchDialogModel
import com.samagra.parent.R

object GeofenceUI {

    fun locationMatchFailureDialog(
        context: Context?,
        dialogProps: LocationMismatchDialogModel?,
        buttonClicked: () -> Unit
    ) {
        context?.let {
            var customDialog: CustomMessageDialog? = null
            if (customDialog == null) {
                customDialog = CustomMessageDialog(
                    it,
                    it.getDrawable(R.drawable.ic_location),
                    dialogProps?.title ?: it.getString(R.string.unable_to_match_location),
                    dialogProps?.description
                        ?: it.getString(R.string.unable_to_match_location_discription_message),
                    true
                )
                customDialog.setOnFinishListener {
                    customDialog.dismiss()
                    buttonClicked()
                }
                showDialog(context, customDialog)
            }
        }
    }

    private fun showDialog(context: Context, customDialog: CustomMessageDialog) {
        if ((context as Activity).isFinishing.not()) {
            customDialog.show()
        }
    }

    fun enableLocationToStartFlow(
        context: Context?,
        negativeCta: () -> Unit,
        positiveCta: () -> Unit
    ) {
        context?.let {
            var customDialog: CustomMessageDialog? = null
            if (customDialog == null) {
                customDialog = CustomMessageDialog(
                    it,
                    it.getDrawable(R.drawable.ic_location),
                    it.getString(R.string.enable_location_from_device_settings),
                    it.getString(R.string.enable_location)
                )
                customDialog.setOnFinishListener("No thanks", "Okay", {
                    customDialog.dismiss()
                    negativeCta()
                }, {
                    customDialog.dismiss()
                    positiveCta.invoke()
                })
                showDialog(context, customDialog)
            }
        }
    }

    fun startLocationMatchDialog(context: Context?): CustomMessageDialog? {
        context?.let {
            val customDialog = CustomMessageDialog(
                it,
                it.getDrawable(R.drawable.ic_location),
                it.getString(R.string.please_wait),
                it.getString(R.string.checking_location),
                true, false
            )
            showDialog(context, customDialog)
            return customDialog
        }
        return null
    }

    fun locationMatchedDialog(context: Context?, logoutClicked: (CustomMessageDialog) -> Unit) {
        context?.let {
            var customDialog: CustomMessageDialog? = null
            if (customDialog == null) {
                customDialog = CustomMessageDialog(
                    it, it.getDrawable(R.drawable.ic_location),
                    it.getString(R.string.location_matched_discription),
                    null
                )
                customDialog.setOnFinishListener {
                    logoutClicked.invoke(customDialog)
                }
                showDialog(context, customDialog)
            }
        }
    }

    fun allowLocationPermissionDialog(
        context: Context?,
        negativeCtaClicked: (CustomMessageDialog) -> Unit,
        positiveCtaClicked: (CustomMessageDialog) -> Unit
    ) {
        context?.let {
            var customDialog: CustomMessageDialog? = null
            if (customDialog == null) {
                customDialog = CustomMessageDialog(
                    it,
                    it.getDrawable(R.drawable.ic_location),
                    it.getString(R.string.allow_permission_title),
                    it.getString(R.string.allow_permission_discription), false, true
                )
                customDialog.setOnFinishListener(context.getString(R.string.okay),
                    object : CallbackListner {
                        override fun onSuccess() {
                            customDialog.dismiss()
                            positiveCtaClicked.invoke(customDialog)
                        }

                        override fun onFailure() {
                            customDialog.dismiss()
                            negativeCtaClicked(customDialog)
                        }
                    })
                showDialog(context, customDialog)
            }
        }
    }
}