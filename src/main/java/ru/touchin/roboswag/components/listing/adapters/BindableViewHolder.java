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

import ru.touchin.roboswag.components.navigation.UiBindable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 12/8/2016.
 * TODO: fill description
 */
public class BindableViewHolder extends RecyclerView.ViewHolder implements UiBindable {

    @NonNull
    private final UiBindable baseBindable;
    @NonNull
    private final BehaviorSubject<Boolean> isStartedSubject = BehaviorSubject.create();

    public BindableViewHolder(@NonNull final UiBindable baseBindable, @NonNull final View itemView) {
        super(itemView);
        this.baseBindable = baseBindable;
    }

    @CallSuper
    public void onAttachedToWindow() {
        isStartedSubject.onNext(true);
    }

    @CallSuper
    public void onDetachedFromWindow() {
        isStartedSubject.onNext(false);
    }

    @NonNull
    @Override
    public <T> Observable<T> bind(@NonNull final Observable<T> observable) {
        return isStartedSubject
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
        return baseBindable.untilStop(observable);
    }
}
