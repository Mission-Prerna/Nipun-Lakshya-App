package com.samagra.commons.travel;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.HashMap;
import java.util.Map;

public class VersionedMutableLiveData<T> extends MutableLiveData<T> {

    public int version;
    private TravellingObserver observerWrapper;
    private Map<String, TravellingObserver> observerWrapperMap = new HashMap<>();
    private LifecycleOwner owner;
    private static String ownerName;

    public void observe(LifecycleOwner owner, TravellingObserver observerWrapper) {
        observerWrapperMap.put(owner.getClass().getName(), observerWrapper);
        if (observerWrapper != null) {
            observe(owner, observerWrapper.observer);
        }
    }

    public void observeForever(LifecycleOwner owner, TravellingObserver observerWrapper) {
        this.owner = owner;
        observerWrapperMap.put(owner.getClass().getName(), observerWrapper);
        if (observerWrapper != null) {
            observeForever(observerWrapper.observer);
        }
    }

    public void observeForever(String owner, TravellingObserver observerWrapper) {
        ownerName = owner;
        observerWrapperMap.put(owner, observerWrapper);
        if (observerWrapper != null) {
            observeForever(observerWrapper.observer);
        }
    }

    @MainThread
    public void observe(final LifecycleOwner owner, final Observer<? super T> observer) {

        // Observe the internal MutableLiveData
        super.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(@Nullable T t) {
                for (String lifecycleOwnerName : observerWrapperMap.keySet()) {
                    if (lifecycleOwnerName.equals(owner.getClass().getName())) {
                        observerWrapper = observerWrapperMap.get(lifecycleOwnerName);
                        break;
                    }
                }
                if (observerWrapper != null && observerWrapper.lastVersion < version) {
                    observer.onChanged(t);
                    observerWrapper.lastVersion = version;
                }
            }
        });
    }

    @MainThread
    public void setValue(@Nullable T t) {
        version++;
        super.setValue(t);
    }

    @Override
    public void observeForever(@NonNull Observer<? super T> observer) {
        super.observeForever(new Observer<T>() {
            @Override
            public void onChanged(@Nullable T t) {
                for (String lifecycleOwnerName : observerWrapperMap.keySet()) {
                    String ownerName = null;
                    if (owner != null) {
                        ownerName = owner.getClass().getName();
                    } else {
                        ownerName = VersionedMutableLiveData.ownerName;
                    }
                    if (lifecycleOwnerName.equals(ownerName)) {
                        observerWrapper = observerWrapperMap.get(lifecycleOwnerName);
                        break;
                    }
                }
                if (observerWrapper != null && observerWrapper.lastVersion < version) {
                    //                    observer.onChanged(t);
                    if(observerWrapper.observer != null){
                        observerWrapper.observer.onChanged(t);
                    }
                    observerWrapper.lastVersion = version;
                }
            }
        });

    }
}
