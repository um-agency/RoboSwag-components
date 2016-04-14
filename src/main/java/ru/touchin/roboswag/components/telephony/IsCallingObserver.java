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

package ru.touchin.roboswag.components.telephony;

import android.Manifest;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import rx.Observable;

/**
 * Created by Gavriil Sitnikov on 02/11/2015.
 * TODO: fill description
 */
public final class IsCallingObserver {

    private static boolean isCallingState(final int state) {
        return state != TelephonyManager.CALL_STATE_IDLE;
    }

    @NonNull
    private final TelephonyManager phoneStateManager;
    @NonNull
    private final Observable<Boolean> isCallingObservable;

    public IsCallingObserver(@NonNull final Context context) {
        phoneStateManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        isCallingObservable = Observable
                .<Boolean>create(subscriber -> {
                    phoneStateManager.listen(new PhoneStateListener() {
                        @Override
                        public void onCallStateChanged(final int state, final String incomingNumber) {
                            super.onCallStateChanged(state, incomingNumber);
                            subscriber.onNext(isCallingState(phoneStateManager.getCallState()));
                        }
                    }, PhoneStateListener.LISTEN_CALL_STATE);
                    subscriber.onNext(isCallingState(phoneStateManager.getCallState()));
                })
                .distinctUntilChanged()
                .replay(1)
                .refCount();
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    @NonNull
    public Observable<Boolean> observeIsCalling() {
        return isCallingObservable;
    }

}
