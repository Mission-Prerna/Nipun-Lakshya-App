package com.samagra.commons.travel;

import androidx.lifecycle.Observer;

public class TravellingObserver<T> {
    final Observer<T> observer;
    int lastVersion;

    public TravellingObserver(Observer<T> observer, int version) {
        this.observer = observer;
        lastVersion = version;
    }

}