/**
 * Singleton Manager class to maintain current Network-Status throughout application
 */

/*
* observe state with live data
NetworkStateManager.instance?.networkConnectivityStatusLiveData?.observe(this){
            Log.e("GGGG", "OBSERVE AssessmentHomeActivity : called with networkStatus $it")
}

* listen state via initializing listener
NetworkStateManager.instance?.initCallbackListener(object :NetworkStateManager.NetworkCallback{

            override fun getNetworkStatus(networkStatus: Boolean) {
                Log.e("GGGG", "LISTEN network AssessmentHomeActivity : $networkStatus")
            }
})

* get the current state
Log.e("GGGG", "networkConnectivityStatus from AssessmentHomeActivity : ${NetworkStateManager.instance?.networkConnectivityStatus}")

*
* */

package com.samagra.commons.utils

import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

class NetworkStateManager(val callback: NetworkStatus) {

    /**
     * Updates the active network status callback
     */
    fun setNetworkConnectivityStatus(connectivityStatus: Boolean) {
        Timber.e("setNetworkConnectivityStatus() called with: connectivityStatus = [$connectivityStatus]")
        // used for the interface callbacks
        callback.onStatusChangedCallback(connectivityStatus)

        //used for the livedata.
        if (Looper.myLooper() === Looper.getMainLooper()) {
            activeNetworkStatusMLD.setValue(connectivityStatus)
        } else {
            activeNetworkStatusMLD.postValue(connectivityStatus)
        }
    }

    /**
     * Observe the current network status via live data
     */
    val networkConnectivityStatusLiveData: LiveData<Boolean>
        get() {
            Timber.e("getNetworkConnectivityStatus() called")
            return activeNetworkStatusMLD
        }

    /*
    * Gives current state of network one time
    * */
    val networkConnectivityStatus: Boolean
        get() {
            return nStatus
        }

    companion object {
        private var nStatus: Boolean = false
        private var mNetworkCallback: NetworkCallback? = null
        private var INSTANCE: NetworkStateManager? = null
        private val activeNetworkStatusMLD = MutableLiveData<Boolean>()

        @JvmStatic
        @get:Synchronized
        val instance: NetworkStateManager?
            get() {
                if (INSTANCE == null) {
                    Timber.e("getInstance() called: Creating new instance")
                    INSTANCE = NetworkStateManager(object : NetworkStatus {
                        override fun onStatusChangedCallback(isNetworkActive: Boolean) {
                            mNetworkCallback?.getNetworkStatus(isNetworkActive)
                            nStatus = isNetworkActive
                        }
                    })
                }
                return INSTANCE
            }
    }


    /*
    * Apply listener if view life cycle is not present
    * */
    fun initCallbackListener(networkCallback: NetworkCallback) {
        mNetworkCallback = networkCallback
    }

    /*
    * Gets the network state from the ConnectivityManager.NetworkCallback() callbacks
    * */
    interface NetworkStatus {
        fun onStatusChangedCallback(isNetworkActive: Boolean)
    }

    /*
    * Gets the network state to the initialized locations (classes, views etc)
    * */
    interface NetworkCallback {
        fun getNetworkStatus(networkStatus: Boolean)
    }
}