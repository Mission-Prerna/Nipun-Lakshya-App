package org.odk.collect.android.formentry.media;

import android.content.Context;
import android.media.MediaPlayer;

import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.utilities.ScreenContext;
import org.odk.collect.async.Scheduler;

public class ScreenContextAudioHelperFactory implements AudioHelperFactory {

    private final Scheduler scheduler;
    private MediaPlayer mediaPlayer;

    public ScreenContextAudioHelperFactory(Scheduler scheduler, MediaPlayer mediaPlayer) {
        this.scheduler = scheduler;
        this.mediaPlayer = mediaPlayer;
    }

    public AudioHelper create(Context context) {
        ScreenContext screenContext = (ScreenContext) context;
        if(mediaPlayer == null) mediaPlayer = new MediaPlayer();
        return new AudioHelper(screenContext.getActivity(), screenContext.getViewLifecycle(), scheduler, mediaPlayer);
    }
}
