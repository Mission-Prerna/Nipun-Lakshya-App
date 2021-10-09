package com.samagra.commons

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber

class CompositeDisposableHelper {

    companion object {

        private lateinit var compositeDisposable: CompositeDisposable

        fun initialize() {
            compositeDisposable = CompositeDisposable()
        }

        fun getCompositeDisposableInstance(): CompositeDisposable {
          return compositeDisposable
        }

        fun addCompositeDisposable(disposable: Disposable) {
            if (this::compositeDisposable.isInitialized) {
                compositeDisposable.add(disposable)
            } else {
                Timber.e("Composite disposable not initialized!")
            }
        }

        fun destroyCompositeDisposable() {
            compositeDisposable.clear()
        }
    }
}