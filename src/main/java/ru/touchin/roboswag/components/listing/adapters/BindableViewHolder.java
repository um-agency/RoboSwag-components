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

package ru.touchin.roboswag.components.listing.adapters;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.concurrent.TimeUnit;

import ru.touchin.roboswag.components.navigation.UiBindable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 12/8/2016.
 * TODO: fill description
 */
public class BindableViewHolder extends RecyclerView.ViewHolder implements UiBindable {

    // it is needed to delay detach to avoid re-subscriptions on fast scroll
    private static final long DETACH_DELAY = TimeUnit.SECONDS.toMillis(1);

    @NonNull
    private final UiBindable baseBindable;
    @NonNull
    private final BehaviorSubject<Boolean> attachedToWindowSubject = BehaviorSubject.create();
    private final Observable<Boolean> attachedObservable;

    public BindableViewHolder(@NonNull final UiBindable baseBindable, @NonNull final View itemView) {
        super(itemView);
        this.baseBindable = baseBindable;
        attachedObservable = attachedToWindowSubject
                .switchMap(attached -> attached
                        ? Observable.just(true)
                        : Observable.timer(DETACH_DELAY, TimeUnit.MILLISECONDS).map(ignored -> false))
                .replay(1)
                .refCount();
    }

    @CallSuper
    public void onAttachedToWindow() {
        attachedToWindowSubject.onNext(true);
    }

    public boolean isAttachedToWindow() {
        return attachedToWindowSubject.hasValue() && attachedToWindowSubject.getValue();
    }

    @CallSuper
    public void onDetachedFromWindow() {
        attachedToWindowSubject.onNext(false);
    }

    @NonNull
    @Override
    public <T> Observable<T> bind(@NonNull final Observable<T> observable) {
        return attachedObservable
                .switchMap(isStarted -> isStarted ? observable.observeOn(AndroidSchedulers.mainThread()) : Observable.never());
    }

    @NonNull
    @Override
    public <T> Observable<T> untilStop(@NonNull final Observable<T> observable) {
        return baseBindable.untilStop(observable);
    }

    @NonNull
    @Override
    public <T> Observable<T> untilDestroy(@NonNull final Observable<T> observable) {
        return baseBindable.untilDestroy(observable);
    }
}
