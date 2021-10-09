package com.samagra.parent.base;

import com.samagra.parent.helper.BackendNwHelper;

import org.odk.collect.android.contracts.IFormManagementContract;

import io.reactivex.disposables.CompositeDisposable;

/**
 * This is the base interface that all the 'Presenter Contracts' must extend.
 * Methods maybe added to it as and when required.
 *
 * @author Pranav Sharma
 */
public interface MvpPresenter<V extends MvpView, I extends MvpInteractor> {
    V getMvpView();

    I getMvpInteractor();

    void onAttach(V mvpView);

    void onDetach();

    boolean isViewAttached();

    CompositeDisposable getCompositeDisposable();

    BackendNwHelper getApiHelper();

    IFormManagementContract getIFormManagementContract();

}
