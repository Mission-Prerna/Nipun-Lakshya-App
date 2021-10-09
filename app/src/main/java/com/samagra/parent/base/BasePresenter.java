package com.samagra.parent.base;

import com.samagra.parent.helper.BackendNwHelper;

import org.odk.collect.android.contracts.IFormManagementContract;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

/**
 * A class that serves as a base for all the presenters (handles business logic) for the activities (serves as view).
 * The class uses Java Generics. The V and I stands for View and Interactor respectively. Since View and Interactors
 * are different for each activity, Java Generics are used. The class must implement {@link MvpPresenter}.
 *
 * @author Pranav Sharma
 */
public class BasePresenter<V extends MvpView, I extends MvpInteractor> implements MvpPresenter<V, I> {

    private V mvpView;
    private I mvpInteractor;
    private IFormManagementContract iFormManagementContract;
    private BackendNwHelper apiHelper;
    private CompositeDisposable compositeDisposable;


    @Inject
    public BasePresenter(I mvpInteractor, CompositeDisposable compositeDisposable, BackendNwHelper backendNwHelper, IFormManagementContract iFormManagementContract) {
        this.apiHelper = backendNwHelper;
        this.mvpInteractor = mvpInteractor;
        this.iFormManagementContract = iFormManagementContract;
        this.compositeDisposable = compositeDisposable;
    }

    @Override
    public CompositeDisposable getCompositeDisposable() {
        return this.compositeDisposable;
    }

    @Override
    public BackendNwHelper getApiHelper() {
        return apiHelper;
    }

    @Override
    public V getMvpView() {
        return mvpView;
    }

    @Override
    public I getMvpInteractor() {
        return mvpInteractor;
    }

    @Override
    public void onAttach(V mvpView) {
        this.mvpView = mvpView;
    }

    @Override
    public void onDetach() {
        this.mvpView = null;
    }

    @Override
    public boolean isViewAttached() {
        return this.mvpView != null;
    }
    @Override
    public IFormManagementContract getIFormManagementContract() {
        return iFormManagementContract;
    }

}
