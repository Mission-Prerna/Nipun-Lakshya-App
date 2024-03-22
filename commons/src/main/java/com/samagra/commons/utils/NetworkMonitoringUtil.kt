/*
* Class to Monitor the internet state at runtime
* initialize it on Application class for one time only
* monitoringUtil.checkNetworkState(); / check the current network state
* monitoringUtil.registerNetworkCallbackEvents(); / register callback events to listen the network state
* */

package com.samagra.commons.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.samagra.commons.CommonUtilities
import timber.log.Timber

class NetworkMonitoringUtil(val context: Context) : ConnectivityManager.NetworkCallback() {

    private val networkStateManager: NetworkStateManager? = NetworkStateManager.instance
    private var connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        // Triggered when the device is connected to the network
        networkStateManager?.setNetworkConnectivityStatus(true)
        Timber.e("onAvailable() called: Connected to network")
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        // Triggered when the device loses network connectivity
        Timber.e("onLost() called: Lost network connection")
        networkStateManager?.setNetworkConnectivityStatus(false)
    }

    /**
     * Registers the Network-Request callback
     * (Note: Register only once to prevent duplicate callbacks)
     */
    fun registerNetworkCallbackEvents() {
        Timber.e("registerNetworkCallbackEvents() called")
        connectivityManager.registerNetworkCallback(networkRequest, this)
    }

    /**
     * Check current Network state
     */
    fun checkNetworkState() {
        try {
            // Set the initial value for the live-data
            networkStateManager?.setNetworkConnectivityStatus(
                CommonUtilities.isNetworkConnected(connectivityManager)
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}