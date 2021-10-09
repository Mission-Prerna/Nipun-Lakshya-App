package com.samagra.commons.travel;

public class BroadcastAction<T> {

    private T liveActionValue;
    private BroadcastEvents liveActionEvent;

    public BroadcastAction(BroadcastEvents liveActionEvent) {
        this.liveActionEvent = liveActionEvent;
    }
    public BroadcastAction(T liveActionValue, BroadcastEvents liveActionEvent) {
        this(liveActionEvent);
        this.liveActionValue = liveActionValue;
    }

    public T getLiveActionValue() {
        return liveActionValue;
    }

    public BroadcastEvents getLiveActionEvent() {
        return liveActionEvent;
    }
}
