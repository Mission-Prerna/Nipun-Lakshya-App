package com.samagra.commons;

import androidx.annotation.NonNull;

import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;

import io.reactivex.Observable;

/**
 * Created by Umang Bhola on 16/5/20.
 * Samagra- Transforming Governance
 */
public class EventBus {
    private final Relay<Object> busSubject;

    public EventBus() {
        busSubject = PublishRelay.create().toSerialized();
    }

    /**
     * Registers for a particular event and returns an observable for subscription.
     *
     * @param eventClass the event
     * @param <T>        the class type of the event
     * @return observable that can be subscribed to.
     */
    public <T> Observable<T> register(@NonNull Class<T> eventClass) {
        return busSubject
                .filter(event -> event.getClass().equals(eventClass))
                .map(obj -> (T) obj);
    }

    /**
     * Sends an event to all the observers who have registered to receive the event type.
     *
     * @param event an RxEvent of any type.
     */
    public void post(@NonNull REvent event) {
        busSubject.accept(event);
    }

    public Relay<Object> getBusSubject() {
        return busSubject;
    }
}
