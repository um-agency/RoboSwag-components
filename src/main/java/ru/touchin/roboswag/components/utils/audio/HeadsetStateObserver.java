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

package ru.touchin.roboswag.components.utils.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.annotation.NonNull;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by Gavriil Sitnikov on 02/11/2015.
 * Simple observer of wired headset state (plugged in or not).
 */
public final class HeadsetStateObserver {

    @NonNull
    private final AudioManager audioManager;
    @NonNull
    private final Observable<Boolean> isPluggedInObservable;

    public HeadsetStateObserver(@NonNull final Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        isPluggedInObservable = Observable
                .<IsPluggedInReceiver>create(subscriber -> {
                    subscriber.onNext(new IsPluggedInReceiver());
                    subscriber.onCompleted();
                })
                .switchMap(isPluggedInReceiver -> Observable
                        .just(isPluggedIn())
                        .concatWith(isPluggedInReceiver.isPluggedInChangedEvent
                                .doOnSubscribe(() -> context.registerReceiver(isPluggedInReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG)))
                                .doOnUnsubscribe(() -> context.unregisterReceiver(isPluggedInReceiver))))
                .replay(1)
                .refCount();
    }

    /**
     * Returns if wired headset plugged in.
     *
     * @return True if headset is plugged in.
     */
    @SuppressWarnings("deprecation")
    public boolean isPluggedIn() {
        return audioManager.isWiredHeadsetOn();
    }

    /**
     * Observes plugged in state of wired headset.
     *
     * @return Returns observable which will provide current plugged in state and any of it's udpdate.
     */
    @NonNull
    public Observable<Boolean> observeIsPluggedIn() {
        return isPluggedInObservable;
    }

    private static class IsPluggedInReceiver extends BroadcastReceiver {

        @NonNull
        private final PublishSubject<Boolean> isPluggedInChangedEvent = PublishSubject.create();

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                if (intent.getExtras() == null) {
                    isPluggedInChangedEvent.onNext(false);
                } else {
                    isPluggedInChangedEvent.onNext(intent.getExtras().getInt("state") != 0);
                }
            }
        }

    }

}
