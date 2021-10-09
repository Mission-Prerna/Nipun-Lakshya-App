package org.odk.collect.android.audio;

import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.async.Scheduler;

class AudioPlayerViewModelFactory implements ViewModelProvider.Factory {

    private final MediaPlayer mediaPlayer;
    private final Scheduler scheduler;

    AudioPlayerViewModelFactory(MediaPlayer mediaPlayer, Scheduler scheduler) {
        this.mediaPlayer = mediaPlayer;
        this.scheduler = scheduler;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AudioPlayerViewModel(mediaPlayer, scheduler);
    }
}
