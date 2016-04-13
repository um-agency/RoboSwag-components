/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.roboswag.components.audio;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.SeekBar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 02/11/2015.
 * TODO: fill description
 */
public final class VolumeController {

    @Nullable
    private static VolumeController instance;

    @NonNull
    public static VolumeController getInstance(@NonNull final Context context) {
        synchronized (VolumeController.class) {
            if (instance == null) {
                instance = new VolumeController(context);
            }
            return instance;
        }
    }

    private final AudioManager audioManager;
    private final int maxVolume;
    private final BehaviorSubject<Integer> volumeSubject;
    private final Observable<Integer> volumeObservable;

    private final Map<SeekBar, Subscription> seekBars = new HashMap<>();
    private final Set<VolumeButtons> volumeButtonsSet = new HashSet<>();

    private VolumeController(@NonNull final Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volumeSubject = BehaviorSubject.create();
        final VolumeObserver volumeObserver = new VolumeObserver();
        volumeObservable = volumeSubject
                .distinctUntilChanged()
                .doOnSubscribe(() -> {
                    context.getContentResolver()
                            .registerContentObserver(Settings.System.CONTENT_URI, true, volumeObserver);
                    updateVolume();
                })
                .doOnUnsubscribe(() -> context.getContentResolver().unregisterContentObserver(volumeObserver))
                .replay(1)
                .refCount();
    }

    private void updateVolume() {
        volumeSubject.onNext(getVolume());
    }

    public void setVolume(final int value) {
        if (value < 0 || value > maxVolume) {
            Lc.assertion(new ShouldNotHappenException("Volume: " + value + " out of bounds [0," + maxVolume + ']'));
            return;
        }
        if (getVolume() != value) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
            updateVolume();
        }
    }

    public int getVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @NonNull
    public Observable<Integer> observeVolume() {
        return volumeObservable;
    }

    public void attachSeekBar(@NonNull final SeekBar seekBar) {
        if (seekBars.containsKey(seekBar)) {
            Lc.assertion(new ShouldNotHappenException("SeekBar already attached"));
            return;
        }
        seekBar.setMax(maxVolume);
        seekBar.setProgress(getVolume());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(final SeekBar ignored, final int progress, final boolean fromUser) {
                setVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(final SeekBar ignored) {
                //ignored
            }

            @Override
            public void onStopTrackingTouch(final SeekBar ignored) {
                //ignored
            }

        });

        seekBars.put(seekBar, observeVolume()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(seekBar::setProgress));
    }

    public void detachSeekBar(@NonNull final SeekBar seekBar) {
        final Subscription subscription = seekBars.get(seekBar);
        if (subscription == null) {
            Lc.assertion(new ShouldNotHappenException("SeekBar not attached yet"));
            return;
        }
        seekBar.setOnSeekBarChangeListener(null);
        subscription.unsubscribe();
        seekBars.remove(seekBar);
    }

    @SuppressWarnings("PMD.AccessorClassGeneration")
    public void attachVolumeButtons(@NonNull final View volumeUpButton, @NonNull final View volumeDownButton) {
        final VolumeButtons volumeButtons = new VolumeButtons(volumeUpButton, volumeDownButton);
        if (volumeButtonsSet.contains(volumeButtons)) {
            Lc.assertion(new ShouldNotHappenException("VolumeButtons already attached"));
            return;
        }

        volumeButtons.volumeUpButton.setOnClickListener(v -> {
            if (getVolume() != maxVolume) {
                setVolume(getVolume() + 1);
            }
        });

        volumeButtons.volumeDownButton.setOnClickListener(v -> {
            if (getVolume() != 0) {
                setVolume(getVolume() - 1);
            }
        });
        volumeButtonsSet.add(volumeButtons);
    }

    @SuppressWarnings("PMD.AccessorClassGeneration")
    public void detachVolumeButtons(@NonNull final View volumeUpButton, @NonNull final View volumeDownButton) {
        final VolumeButtons volumeButtons = new VolumeButtons(volumeUpButton, volumeDownButton);
        if (!volumeButtonsSet.contains(volumeButtons)) {
            Lc.assertion(new ShouldNotHappenException("VolumeButtons not attached yet"));
            return;
        }

        volumeButtons.volumeUpButton.setOnClickListener(null);
        volumeButtons.volumeDownButton.setOnClickListener(null);
        volumeButtonsSet.remove(volumeButtons);
    }

    private static class VolumeButtons {

        @NonNull
        private final View volumeUpButton;
        @NonNull
        private final View volumeDownButton;

        private VolumeButtons(@NonNull final View volumeUpButton, @NonNull final View volumeDownButton) {
            this.volumeUpButton = volumeUpButton;
            this.volumeDownButton = volumeDownButton;
        }

        @Override
        public boolean equals(final Object object) {
            return object instanceof VolumeButtons
                    && ((VolumeButtons) object).volumeDownButton == volumeDownButton
                    && ((VolumeButtons) object).volumeUpButton == volumeUpButton;
        }

        @Override
        public int hashCode() {
            return volumeDownButton.hashCode() + volumeUpButton.hashCode();
        }

    }

    private class VolumeObserver extends ContentObserver {

        public VolumeObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(final boolean selfChange) {
            updateVolume();
        }

    }

}
