/*
 * Copyright (C) 2018 Shobhit Agarwal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.audio;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.odk.collect.android.R;


import static java.lang.Math.max;
import static java.lang.Math.min;

public class AudioControllerView extends FrameLayout {

    TextView currentDurationLabel;

    TextView totalDurationLabel;

    ImageButton playButtonjljk;
    Button fastForwardBtn;

    private final SwipeListener swipeListener;

    private Boolean playing = false;
    private Integer position = 0;
    private Integer duration = 0;

    Button fastRewindBtn;
    private Listener listener;

    public AudioControllerView(Context context) {
        super(context);

        View view =   View.inflate(context, R.layout.audio_controller_layout, this);
        playButtonjljk = view.findViewById(R.id.playBtnee);
        fastForwardBtn = view.findViewById(R.id.fastForwardBtn);
        SeekBar seekBarCTA = findViewById(R.id.seekBarButton);
        swipeListener = new SwipeListener();
        seekBarCTA.setOnSeekBarChangeListener(swipeListener);
        playButtonjljk.setImageResource(R.drawable.ic_play_arrow_24dp);
        currentDurationLabel = findViewById(R.id.currentDuration);
        totalDurationLabel = findViewById(R.id.totalDuration);
        fastForwardBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fastForwardMedia();
            }
        });
        fastRewindBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rewindMedia();
            }
        });
        playButtonjljk.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClicked();
                    }
                }
        );
    }

    void fastForwardMedia() {
        int newPosition = position + 5000;
        onPositionChanged(newPosition);
    }

    void rewindMedia() {
        int newPosition = position - 5000;
        onPositionChanged(newPosition);
    }

    void playClicked() {
        if (playing) {
            listener.onPauseClicked();
        } else {
            listener.onPlayClicked();
        }
    }

    private void onPositionChanged(Integer newPosition) {
        Integer correctedPosition = max(0, min(duration, newPosition));

        setPosition(correctedPosition);
        if (listener != null) {
            listener.onPositionChanged(correctedPosition);
        }
    }

    public void hidePlayer() {
        setVisibility(GONE);
    }

    public void showPlayer() {
        setVisibility(View.VISIBLE);
    }

    private static String getTime(long seconds) {
        return new DateTime(seconds, DateTimeZone.UTC).toString("mm:ss");
    }

    public void setPlaying(Boolean playing) {
        this.playing = playing;

        if (playing) {
            playButtonjljk.setImageResource(R.drawable.ic_pause_24dp);
        } else {
            playButtonjljk.setImageResource(R.drawable.ic_play_arrow_24dp);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setPosition(Integer position) {
        if (!swipeListener.isSwiping()) {
            renderPosition(position);
        }
    }

    public void setDuration(Integer duration) {
        this.duration = duration;

        totalDurationLabel.setText(getTime(duration));
        SeekBar seekBarCTA = findViewById(R.id.seekBarButton);
        seekBarCTA.setMax(duration);

        setPosition(0);
    }

    private void renderPosition(Integer position) {
        this.position = position;

        currentDurationLabel.setText(getTime(position));
        SeekBar seekBarCTA = findViewById(R.id.seekBarButton);

        seekBarCTA.setProgress(position);
    }

    public interface SwipableParent {
        void allowSwiping(boolean allowSwiping);
    }

    public interface Listener {

        void onPlayClicked();

        void onPauseClicked();

        void onPositionChanged(Integer newPosition);
    }

    private class SwipeListener implements SeekBar.OnSeekBarChangeListener {

        private Boolean wasPlaying = false;
        private Boolean swiping = false;

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            swiping = true;
            ((SwipableParent) getContext()).allowSwiping(false);

            if (playing) {
                listener.onPauseClicked();
                wasPlaying = true;
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int newProgress, boolean fromUser) {
            if (fromUser) {
                renderPosition(newProgress);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            swiping = false;
            ((SwipableParent) getContext()).allowSwiping(true);

            onPositionChanged(position);

            if (wasPlaying) {
                listener.onPlayClicked();
                wasPlaying = false;
            }
        }

        Boolean isSwiping() {
            return swiping;
        }
    }
}
