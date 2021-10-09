package com.samagra.ancillaryscreens.utils

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.samagra.ancillaryscreens.fcm.NotificationViewModel
import com.samagra.commons.basemvvm.BaseRepository

@Suppress("UNCHECKED_CAST")
class ViewModelProviderFactory(
    val application: Application,
    private val repository: BaseRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                return NotificationViewModel(
                ) as T
            }
            else -> {
                throw IllegalArgumentException("View Model class not initialized in Factory : ${modelClass.name}")
            }
        }
    }
}