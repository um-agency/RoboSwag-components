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

package ru.touchin.roboswag.components.utils;

import android.support.annotation.NonNull;

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 18/04/16.
 * Simple implementation of {@link LifecycleBindable}. Could be used to not implement interface but use such object inside.
 */
public class BaseLifecycleBindable implements LifecycleBindable {

    @NonNull
    private final BehaviorSubject<Boolean> isCreatedSubject = BehaviorSubject.create();
    @NonNull
    private final BehaviorSubject<Boolean> isStartedSubject = BehaviorSubject.create();

    /**
     * Call it on parent's onCreate method.
     */
    public void onCreate() {
        isCreatedSubject.onNext(true);
    }

    /**
     * Call it on parent's onStart method.
     */
    public void onStart() {
        isStartedSubject.onNext(true);
    }

    /**
     * Call it on parent's onStop method.
     */
    public void onStop() {
        isStartedSubject.onNext(false);
    }

    /**
     * Call it on parent's onDestroy method.
     */
    public void onDestroy() {
        isCreatedSubject.onNext(false);
    }

    @NonNull
    @Override
    public <T> Subscription bind(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return isStartedSubject.switchMap(started -> started ? observable.observeOn(AndroidSchedulers.mainThread()) : Observable.never())
                .takeUntil(isCreatedSubject.filter(created -> !created))
                .subscribe(onNextAction,
                        throwable -> Lc.assertion(new ShouldNotHappenException("Unexpected error on untilStop at " + codePoint, throwable)));
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(observable, Actions.empty(),
                throwable -> Lc.assertion(new ShouldNotHappenException("Unexpected error on untilStop at " + codePoint, throwable)),
                Actions.empty());
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(observable, onNextAction,
                throwable -> Lc.assertion(new ShouldNotHappenException("Unexpected error on untilStop at " + codePoint, throwable)),
                Actions.empty());
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable,
                                      @NonNull final Action1<T> onNextAction,
                                      @NonNull final Action1<Throwable> onErrorAction) {
        return untilStop(observable, onNextAction, onErrorAction, Actions.empty());
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable,
                                      @NonNull final Action1<T> onNextAction,
                                      @NonNull final Action1<Throwable> onErrorAction,
                                      @NonNull final Action0 onCompletedAction) {
        return until(observable, isStartedSubject.map(started -> !started), onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(observable, Actions.empty(),
                throwable -> Lc.assertion(new ShouldNotHappenException("Unexpected error on untilDestroy at " + codePoint, throwable)),
                Actions.empty());
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(observable, onNextAction,
                throwable -> Lc.assertion(new ShouldNotHappenException("Unexpected error on untilDestroy at " + codePoint, throwable)),
                Actions.empty());
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction) {
        return untilDestroy(observable, onNextAction, onErrorAction, Actions.empty());
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction,
                                         @NonNull final Action0 onCompletedAction) {
        return until(observable, isCreatedSubject.map(created -> !created), onNextAction, onErrorAction, onCompletedAction);
    }

    private <T> Subscription until(@NonNull final Observable<T> observable,
                                   @NonNull final Observable<Boolean> conditionSubject,
                                   @NonNull final Action1<T> onNextAction,
                                   @NonNull final Action1<Throwable> onErrorAction,
                                   @NonNull final Action0 onCompletedAction) {
        return isCreatedSubject.first()
                .switchMap(created -> created
                        ? observable.observeOn(AndroidSchedulers.mainThread()).doOnCompleted(onCompletedAction)
                        : Observable.empty())
                .takeUntil(conditionSubject.filter(condition -> condition))
                .subscribe(onNextAction, onErrorAction);
    }

}
