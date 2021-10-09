/*
 * Copyright (C) 2011 University of Washington
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

package org.odk.collect.android.formentry.questions;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.lifecycle.LiveData;

import com.google.android.material.button.MaterialButton;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.jetbrains.annotations.NotNull;

import org.odk.collect.android.R;
import org.odk.collect.android.audio.AudioButton;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.audio.Clip;
import org.odk.collect.android.utilities.ContentUriProvider;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.ScreenContext;
import org.odk.collect.android.utilities.StringUtils;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.nbistubs.BuildConfig;

import java.io.File;

import timber.log.Timber;

/**
 * Represents a label for a prompt/question or a select choice. The label can have media
 * attached to it as well as text (such as audio, video or an image).
 */
public class AudioVideoImageTextLabel extends RelativeLayout implements View.OnClickListener {

     AudioButton audioButton1212;

    MaterialButton videoButton;


    ImageView imageView;

    TextView missingImage;

    FrameLayout textContainer;

    TextView labelTextView;

    LinearLayout mediaButtonsContainer;

    private String videoURI;
    private int originalTextColor;
    private int playTextColor = Color.rgb(47,85,151);
    private CharSequence questionText;
    private String bigImageURI;
    private ReferenceManager referenceManager;

    public AudioVideoImageTextLabel(Context context) {
        super(context);
        View v = View.inflate(context, R.layout.audio_video_image_text_label, this);
        labelTextView = v.findViewById(R.id.text_label);
        audioButton1212 = v.findViewById(R.id.audioButton1);
        videoButton = v.findViewById(R.id.videoButton);
        imageView = v.findViewById(R.id.imageView);
        missingImage = v.findViewById(R.id.missingImage);
        textContainer = v.findViewById(R.id.text_container);
        labelTextView = v.findViewById(R.id.text_label);
        mediaButtonsContainer = v.findViewById(R.id.media_buttons);
    }

    public AudioVideoImageTextLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        View v = View.inflate(context, R.layout.audio_video_image_text_label, this);
        labelTextView = v.findViewById(R.id.text_label);
        audioButton1212 = v.findViewById(R.id.audioButton1);
        videoButton = v.findViewById(R.id.videoButton);
        imageView = v.findViewById(R.id.imageView);
        missingImage = v.findViewById(R.id.missingImage);
        textContainer = v.findViewById(R.id.text_container);
        labelTextView = v.findViewById(R.id.text_label);
        mediaButtonsContainer = v.findViewById(R.id.media_buttons);
    }

    public void setTextView(TextView questionText) {
        this.questionText = questionText.getText();

        this.labelTextView = questionText;
        this.labelTextView.setId(View.generateViewId());

        textContainer = findViewById(R.id.text_container);
        textContainer.removeAllViews();
        textContainer.addView(this.labelTextView);
    }

    public void setText(String questionText, boolean isRequiredQuestion, float fontSize) {
        this.questionText = questionText;

        if (questionText != null && !questionText.isEmpty()) {
            labelTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
            labelTextView.setText(StringUtils.textToHtml(FormEntryPromptUtils.markQuestionIfIsRequired(questionText, isRequiredQuestion)));
            labelTextView.setMovementMethod(LinkMovementMethod.getInstance());

            // Wrap to the size of the parent view
            labelTextView.setHorizontallyScrolling(false);
        } else {
            labelTextView.setVisibility(View.GONE);
        }
    }

    public void setAudio(String audioURI, AudioHelper audioHelper) {
        setupAudioButton(audioURI, audioHelper);
    }

    /**
     * This should move to separate setters like {@link #setAudio(String, AudioHelper)}
     */
    @Deprecated
    public void setImageVideo(String imageURI, String videoURI,
                              String bigImageURI, ReferenceManager referenceManager) {
        this.bigImageURI = bigImageURI;
        this.videoURI = videoURI;
        this.referenceManager = referenceManager;

        if (videoURI != null) {
            setupVideoButton();
        }

        if (imageURI != null) {
            setupBigImage(imageURI);
        }
    }

    public void setPlayTextColor(int textColor) {
        playTextColor = textColor;
        audioButton1212.setColors(getThemeUtils().getColorOnSurface(), playTextColor);
    }

    public void playVideo() {
        String videoFilename = "";
        try {
            videoFilename = referenceManager.deriveReference(videoURI).getLocalURI();
        } catch (InvalidReferenceException e) {
            Timber.e(e, "Invalid reference exception due to %s ", e.getMessage());
        }

        File videoFile = new File(videoFilename);
        if (!videoFile.exists()) {
            // We should have a video clip, but the file doesn't exist.
            String errorMsg = getContext().getString(R.string.file_missing, videoFilename);
            Timber.d("File %s is missing", videoFilename);
            ToastUtils.showLongToast(errorMsg);
            return;
        }

        Intent intent = new Intent("android.intent.action.VIEW");
        Uri uri =
                ContentUriProvider.getUriForFile(getContext(), "org.samagra.missionprerna.android.provider", videoFile);
        FileUtils.grantFileReadPermissions(intent, uri, getContext());
        intent.setDataAndType(uri, "video/*");
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(intent);
        } else {
            ToastUtils.showShortToast(getContext().getString(R.string.activity_not_found, getContext().getString(R.string.view_video)));
        }
    }

    public TextView getLabelTextView() {
        return labelTextView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.videoButton) {
            playVideo();
        } else if (id == R.id.imageView) {
            onImageClick();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        labelTextView = findViewById(R.id.text_label);
        if(labelTextView != null)
        labelTextView.setEnabled(enabled);
        imageView = findViewById(R.id.imageView);
        if(imageView != null)
        imageView.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {

        if(labelTextView != null && imageView != null)
        return labelTextView.isEnabled() && imageView.isEnabled();
        else if(imageView != null)
                return imageView.isEnabled();
        else if (labelTextView != null)
            return labelTextView.isEnabled();
        else
            return true;
    }

    private void onImageClick() {
        if (bigImageURI != null) {
            openImage();
        } else {
            selectItem();
        }
    }

    private void openImage() {
        try {
            File bigImage = new File(referenceManager.deriveReference(bigImageURI).getLocalURI());
            Intent intent = new Intent("android.intent.action.VIEW");
            Uri uri =
                    ContentUriProvider.getUriForFile(getContext(), "org.samagra.missionprerna.android.provider", bigImage);
            FileUtils.grantFileReadPermissions(intent, uri, getContext());
            intent.setDataAndType(uri, "image/*");
            getContext().startActivity(intent);
        } catch (InvalidReferenceException e) {
            Timber.e(e, "Invalid image reference due to %s ", e.getMessage());
        } catch (ActivityNotFoundException e) {
            Timber.d(e, "No Activity found to handle due to %s", e.getMessage());
            ToastUtils.showShortToast(getContext().getString(R.string.activity_not_found,
                    getContext().getString(R.string.view_image)));
        }
    }

    private void selectItem() {
        if (labelTextView instanceof RadioButton) {
            ((RadioButton) labelTextView).setChecked(true);
        } else if (labelTextView instanceof CheckBox) {
            CheckBox checkbox = (CheckBox) labelTextView;
            checkbox.setChecked(!checkbox.isChecked());
        }
    }

    private void setupBigImage(String imageURI) {
        String errorMsg = null;

        try {
            String imageFilename = this.referenceManager.deriveReference(imageURI).getLocalURI();
            final File imageFile = new File(imageFilename);
            if (imageFile.exists()) {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int screenWidth = metrics.widthPixels;
                int screenHeight = metrics.heightPixels;
                Bitmap b = FileUtils.getBitmapScaledToDisplay(imageFile, screenHeight, screenWidth);
                imageView = findViewById(R.id.imageView);
                if (b != null) {
                    imageView.setVisibility(VISIBLE);
                    imageView.setImageBitmap(b);
                    imageView.setOnClickListener(this);
                } else {
                    // Loading the image failed, so it's likely a bad file.
                    errorMsg = getContext().getString(R.string.file_invalid, imageFile);
                }
            } else {
                // We should have an image, but the file doesn't exist.
                errorMsg = getContext().getString(R.string.file_missing, imageFile);
            }

            if (errorMsg != null) {
                // errorMsg is only set when an error has occurred
                Timber.e(errorMsg);
                imageView.setVisibility(View.GONE);
                missingImage.setVisibility(VISIBLE);
                missingImage.setText(errorMsg);
            }
        } catch (InvalidReferenceException e) {
            Timber.e(e, "Invalid image reference due to %s ", e.getMessage());
        }
    }

    private void setupVideoButton() {
        videoButton.setVisibility(VISIBLE);
        mediaButtonsContainer.setVisibility(VISIBLE);
        videoButton.setOnClickListener(this);
    }

    private void setupAudioButton(String audioURI, AudioHelper audioHelper) {
        audioButton1212.setVisibility(VISIBLE);
        mediaButtonsContainer.setVisibility(VISIBLE);

        ScreenContext activity = getScreenContext();
        String clipID = getTag() != null ? getTag().toString() : "";
        LiveData<Boolean> isPlayingLiveData = audioHelper.setAudio(audioButton1212, new Clip(clipID, audioURI));

        originalTextColor = Color.rgb(47,85,151);
//                labelTextView.getTextColors().getDefaultColor();
        isPlayingLiveData.observe(activity.getViewLifecycle(), isPlaying -> {
            if (isPlaying) {
                labelTextView.setTextColor(playTextColor);
            } else {
                labelTextView.setTextColor(originalTextColor);
                // then set the text to our original (brings back any html formatting)
                labelTextView.setText(questionText);
            }
        });
    }

    @NotNull
    private ThemeUtils getThemeUtils() {
        return new ThemeUtils(getContext());
    }

    private ScreenContext getScreenContext() {
        try {
            return (ScreenContext) getContext();
        } catch (ClassCastException e) {
            throw new RuntimeException(getContext().toString() + " must implement " + ScreenContext.class.getName());
        }
    }
}
