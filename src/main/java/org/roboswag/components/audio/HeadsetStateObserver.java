package org.roboswag.components.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 02/11/2015.
 * TODO: fill description
 */
public class HeadsetStateObserver {

    @Nullable
    private static HeadsetStateObserver instance;

    @NonNull
    public static synchronized HeadsetStateObserver getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new HeadsetStateObserver(context);
        }
        return instance;
    }

    private final AudioManager audioManager;
    private final BehaviorSubject<Boolean> isPluggedInSubject;
    private final Observable<Boolean> isPluggedInObservable;

    @Nullable
    private IsPluggedInReceiver isPluggedInReceiver;

    private HeadsetStateObserver(@NonNull Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        isPluggedInSubject = BehaviorSubject.create();
        isPluggedInObservable = isPluggedInSubject
                .distinctUntilChanged()
                .doOnSubscribe(() -> {
                    isPluggedInReceiver = new IsPluggedInReceiver();
                    context.registerReceiver(isPluggedInReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
                    isPluggedInSubject.onNext(isPluggedIn());
                })
                .doOnUnsubscribe(() -> {
                    if (isPluggedInReceiver == null) {
                        throw new IllegalStateException("IsPluggedInReceiver is null on unsubscribe");
                    }

                    context.unregisterReceiver(isPluggedInReceiver);
                    isPluggedInReceiver = null;
                })
                .replay(1)
                .refCount();
    }

    @SuppressWarnings("deprecation")
    public boolean isPluggedIn() {
        return audioManager.isWiredHeadsetOn();
    }

    public Observable<Boolean> observeIsPluggedIn() {
        return isPluggedInObservable;
    }

    private class IsPluggedInReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                isPluggedInSubject.onNext(intent.getExtras().getInt("state") != 0);
            }
        }

    }

}
