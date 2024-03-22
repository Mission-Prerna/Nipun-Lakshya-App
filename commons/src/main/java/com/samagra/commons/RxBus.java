package com.samagra.commons;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * An event bus built using RxJava. This event bus helps in inter-module communication
 * by receiving messages from modules and transferring messages to relevant modules.
 *
 * @author Pranav Sharma
 */
public class RxBus {
    private PublishSubject<Object> bus = PublishSubject.create();

    public RxBus() {
        // empty constructor
    }

    public void send(Object o) {
        bus.onNext(o);
    }

    public Observable<Object> toObservable() {
        return bus;
    }

    public boolean hasObservers() {
        return bus.hasObservers();
    }
}
