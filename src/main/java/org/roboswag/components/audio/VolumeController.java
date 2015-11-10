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

package org.roboswag.components.audio;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 02/11/2015.
 * TODO: fill description
 */
public class VolumeController {

    @Nullable
    private static VolumeController instance;

    @NonNull
    public synchronized static VolumeController getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new VolumeController(context);
        }
        return instance;
    }

    private final AudioManager audioManager;
    private final int maxVolume;
    private final BehaviorSubject<Integer> volumeSubject;
    private final Observable<Integer> volumeObservable;

    @Nullable
    private VolumeObserver volumeObserver;
    @Nullable
    private SeekBar seekBar;
    @Nullable
    private ImageView volumeDown;
    @Nullable
    private ImageView volumeUp;
    @Nullable
    private Subscription seekBarSubscription;

    private VolumeController(@NonNull Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volumeSubject = BehaviorSubject.create();
        volumeObservable = volumeSubject
                .distinctUntilChanged()
                .doOnSubscribe(() -> {
                    volumeObserver = new VolumeObserver();
                    context.getContentResolver()
                            .registerContentObserver(Settings.System.CONTENT_URI, true, volumeObserver);
                    updateVolume();
                })
                .doOnUnsubscribe(() -> {
                    if (volumeObserver == null) {
                        throw new IllegalStateException("VolumeObserver is null on unsubscribe");
                    }
                    context.getContentResolver()
                            .unregisterContentObserver(volumeObserver);
                    volumeObserver = null;
                })
                .replay(1)
                .refCount();
    }

    private void updateVolume() {
        volumeSubject.onNext(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    public void setVolume(int value) {
        if (value < 0 || value > maxVolume) {
            throw new IllegalStateException("Volume: " + value + " out of bounds [0," + maxVolume + "]");
        }
        if (getVolume() != value) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
        }
    }

    public int getVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @NonNull
    public Observable<Integer> observeVolume() {
        return volumeObservable;
    }

    public void attachSeekBar(@NonNull SeekBar seekBar) {
        if (this.seekBar != null) {
            throw new IllegalArgumentException("Attached SeekBar is not null");
        }
        this.seekBar = seekBar;
        this.seekBar.setMax(maxVolume);
        this.seekBar.setProgress(getVolume());
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });

        seekBarSubscription = observeVolume()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(seekBar::setProgress);
    }

    public void attachVolumeButtons(@NonNull ImageView volumeDown, @NonNull ImageView volumeUp) {
        if (this.volumeDown != null && this.volumeUp != null) {
            throw new IllegalArgumentException("Attached volume buttons is not null");
        }
        this.volumeDown = volumeDown;
        this.volumeUp = volumeUp;

        volumeUp.setOnClickListener(v -> {
            if (getVolume() != maxVolume) {
                setVolume(getVolume() + 1);
            }
        });

        volumeDown.setOnClickListener(v -> {
            if (getVolume() != 0) {
                setVolume(getVolume() - 1);
            }
        });
        seekBarSubscription = observeVolume()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(seekBar::setProgress);
    }

    public void detachSeekBar(@NonNull SeekBar seekBar) {
        if (this.seekBar != seekBar) {
            throw new IllegalArgumentException("Wrong SeekBar: " + seekBar + " != " + this.seekBar);
        }
        if (seekBarSubscription == null) {
            throw new IllegalStateException("SeekBarSubscription is null on detach of SeekBar");
        }

        this.seekBar.setOnSeekBarChangeListener(null);
        seekBarSubscription.unsubscribe();
        seekBarSubscription = null;
        this.seekBar = null;
    }

    public void detachVolumeButtons(@NonNull ImageView volumeDownImageView, @NonNull ImageView volumeUpImageView) {
        if (this.volumeDown != volumeDownImageView && this.volumeUp != volumeUpImageView) {
            throw new IllegalArgumentException("Wrong SeekBar: " + seekBar + " != " + this.seekBar);
        }

        this.volumeDown = null;
        this.volumeUp = null;
    }

    private class VolumeObserver extends ContentObserver {

        public VolumeObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            updateVolume();
        }

    }

}
