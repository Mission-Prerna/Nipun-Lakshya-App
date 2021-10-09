package com.samagra.gatekeeper

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object Gatekeeper : GatekeeperBottomSheetInteractions {

    private const val ACTION_DESTRUCT = "DESTRUCT"
    private const val ACTION_DISMISS = "DISMISS"

    var warningShown = false
    private const val TAG = "Gatekeeper"

    fun assess(
        fragmentManager: FragmentManager,
        baseUrl: String,
        applicationId: String,
        apiKey: String,
        actor: String,
        skipWarning: Boolean,
        singleError: Boolean
    ) {
        if (singleError && warningShown) {
            Log.d(TAG, "assess: Error shown already. Skipping now.")
            return
        }
        val service = createService(baseUrl)
        service.getGatekeeper(
            applicationId = applicationId,
            apiKey = apiKey
        ).enqueue(object : Callback<GatekeeperRemoteResponse> {
            override fun onResponse(
                call: Call<GatekeeperRemoteResponse>,
                response: Response<GatekeeperRemoteResponse>
            ) {
                val gatekeeperResponse = response.body() ?: return
                handleGatekeeperResponse(
                    fragmentManager = fragmentManager,
                    actor = actor,
                    gatekeeperResponse = gatekeeperResponse,
                    skipWarning = skipWarning
                )
            }

            override fun onFailure(call: Call<GatekeeperRemoteResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun createService(baseUrl: String): GatekeeperService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(GatekeeperService::class.java)
    }

    private fun handleGatekeeperResponse(
        fragmentManager: FragmentManager,
        actor: String,
        gatekeeperResponse: GatekeeperRemoteResponse,
        skipWarning: Boolean
    ) {
        if (actor.isNotEmpty()) {
            gatekeeperResponse.actors?.firstOrNull {
                it.id == actor
            }?.let { actorBlock ->
                handleError(
                    fragmentManager = fragmentManager,
                    error = actorBlock.error,
                    skipWarning = skipWarning
                )
                return
            }
        }

        gatekeeperResponse.system?.error?.let { error ->
            handleError(
                fragmentManager = fragmentManager,
                error = error,
                skipWarning = skipWarning
            )
            return
        }

    }

    private fun handleError(fragmentManager: FragmentManager, error: Error, skipWarning: Boolean) {
        if (skipWarning && error.action == ACTION_DISMISS) {
            return
        }
        val sheet = GatekeeperBottomSheet.newInstance(error)
        sheet.interactions = this
        sheet.show(fragmentManager, GatekeeperBottomSheet.TAG)
        warningShown = true
    }


    override fun onDialogDismiss(context: Context?, action: String?) {
        if (context is AppCompatActivity && action == ACTION_DESTRUCT) {
            warningShown = false
            context.finishAffinity()
        }
    }
}