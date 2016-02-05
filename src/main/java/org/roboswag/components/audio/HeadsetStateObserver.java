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
public final class HeadsetStateObserver {

    @Nullable
    private static HeadsetStateObserver instance;

    @NonNull
    public static HeadsetStateObserver getInstance(@NonNull final Context context) {
        synchronized (HeadsetStateObserver.class) {
            if (instance == null) {
                instance = new HeadsetStateObserver(context);
            }
            return instance;
        }
    }

    private final AudioManager audioManager;
    private final BehaviorSubject<Boolean> isPluggedInSubject;
    private final Observable<Boolean> isPluggedInObservable;

    private HeadsetStateObserver(@NonNull final Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        isPluggedInSubject = BehaviorSubject.create();
        final IsPluggedInReceiver isPluggedInReceiver = new IsPluggedInReceiver();
        isPluggedInObservable = isPluggedInSubject
                .distinctUntilChanged()
                .doOnSubscribe(() -> {
                    context.registerReceiver(isPluggedInReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
                    isPluggedInSubject.onNext(isPluggedIn());
                })
                .doOnUnsubscribe(() -> context.unregisterReceiver(isPluggedInReceiver))
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
        public void onReceive(final Context context, final Intent intent) {
            if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                if (intent.getExtras() == null) {
                    isPluggedInSubject.onNext(false);
                } else {
                    isPluggedInSubject.onNext(intent.getExtras().getInt("state") != 0);
                }
            }
        }

    }

}
