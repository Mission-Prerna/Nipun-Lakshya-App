package com.samagra.commons.travel;


public class BroadcastActionSingleton {
    private static BroadcastActionSingleton mAppEvents;

    private VersionedMutableLiveData<BroadcastAction> mLiveActionEvent;

    private BroadcastActionSingleton() {
        mLiveActionEvent = new VersionedMutableLiveData<>();
    }

    public static synchronized BroadcastActionSingleton getInstance() {
        if (mAppEvents == null) {
            mAppEvents = new BroadcastActionSingleton();
        }
        return mAppEvents;
    }

    public VersionedMutableLiveData<BroadcastAction> getLiveAppAction() {
        return mLiveActionEvent;
    }
}
