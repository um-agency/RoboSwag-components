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

import android.bluetooth.BluetoothA2dp;
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
 * Simple observer of wired and wireless (bluetooth A2DP) headsets state (plugged in or not).
 * <br><font color="yellow"> You require android.permission.BLUETOOTH if want to observe wireless headset state </font>
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
                                .doOnSubscribe(() -> {
                                    final IntentFilter headsetStateIntentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
                                    headsetStateIntentFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
                                    context.registerReceiver(isPluggedInReceiver, headsetStateIntentFilter);
                                })
                                .doOnUnsubscribe(() -> context.unregisterReceiver(isPluggedInReceiver))))
                .replay(1)
                .refCount();
    }

    /**
     * Returns if wired or wireless headset plugged in.
     *
     * @return True if headset is plugged in.
     */
    @SuppressWarnings("deprecation")
    public boolean isPluggedIn() {
        return audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn();
    }

    /**
     * Observes plugged in state of headset.
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
                isPluggedInChangedEvent.onNext(intent.getIntExtra("state", 0) != 0);
            }
            if (BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())) {
                final int bluetoothState = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);
                switch (bluetoothState) {
                    case BluetoothA2dp.STATE_DISCONNECTED:
                        isPluggedInChangedEvent.onNext(false);
                        break;
                    case BluetoothA2dp.STATE_CONNECTED:
                        isPluggedInChangedEvent.onNext(true);
                        break;
                    default:
                        break;
                }
            }
        }

    }

}
