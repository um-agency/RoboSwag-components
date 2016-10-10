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

package ru.touchin.roboswag.components.adapters;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.concurrent.TimeUnit;

import ru.touchin.roboswag.components.utils.LifecycleBindable;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 12/8/2016.
 * ViewHolder that implements {@link LifecycleBindable} and uses parent bindable object as bridge (Activity, ViewController etc.).
 * It is important to use such ViewHolder to avoid endless bindings when parent bindable have started but ViewHolder already detached from window.
 * So inside method {@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)}
 * use only inner {@link #bind(Observable, Action1)} method but not parent's bind method.
 */
public class BindableViewHolder extends RecyclerView.ViewHolder implements LifecycleBindable {

    //HACK: it is needed to delay detach to avoid re-subscriptions on fast scroll
    private static final long DETACH_DELAY = TimeUnit.SECONDS.toMillis(1);

    @NonNull
    private final LifecycleBindable baseLifecycleBindable;
    @NonNull
    private final BehaviorSubject<Boolean> attachedToWindowSubject = BehaviorSubject.create();
    private final Observable<Boolean> attachedObservable;

    public BindableViewHolder(@NonNull final LifecycleBindable baseLifecycleBindable, @NonNull final View itemView) {
        super(itemView);
        this.baseLifecycleBindable = baseLifecycleBindable;
        attachedObservable = attachedToWindowSubject
                .switchMap(attached -> attached
                        ? Observable.just(true)
                        : Observable.timer(DETACH_DELAY, TimeUnit.MILLISECONDS).map(ignored -> false))
                .distinctUntilChanged()
                .replay(1)
                .refCount();
    }

    /**
     * Calls when {@link RecyclerView.ViewHolder} have attached to window.
     */
    @CallSuper
    public void onAttachedToWindow() {
        attachedToWindowSubject.onNext(true);
    }

    /**
     * Returns if {@link RecyclerView.ViewHolder} attached to window or not.
     *
     * @return True if {@link RecyclerView.ViewHolder} attached to window.
     */
    public boolean isAttachedToWindow() {
        return attachedToWindowSubject.hasValue() && attachedToWindowSubject.getValue();
    }

    /**
     * Calls when {@link RecyclerView.ViewHolder} have detached from window.
     */
    @CallSuper
    public void onDetachedFromWindow() {
        attachedToWindowSubject.onNext(false);
    }

    @SuppressWarnings("CPD-START")
    //CPD: it is same as in other implementation based on BaseLifecycleBindable
    @NonNull
    @Override
    public <T> Subscription bind(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        final String codePoint = Lc.getCodePoint(this, 1);
        final Observable<T> safeObservable = observable
                .onErrorResumeNext(throwable -> {
                    Lc.assertion(new ShouldNotHappenException("Unexpected error on bind at " + codePoint, throwable));
                    return Observable.never();
                })
                .observeOn(AndroidSchedulers.mainThread());
        return attachedObservable.switchMap(attached -> attached ? safeObservable : Observable.never())
                .subscribe(onNextAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable) {
        return baseLifecycleBindable.untilStop(observable);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        return baseLifecycleBindable.untilStop(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable,
                                      @NonNull final Action1<T> onNextAction,
                                      @NonNull final Action1<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilStop(observable, onNextAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable,
                                      @NonNull final Action1<T> onNextAction,
                                      @NonNull final Action1<Throwable> onErrorAction,
                                      @NonNull final Action0 onCompletedAction) {
        return baseLifecycleBindable.untilStop(observable, onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable) {
        return baseLifecycleBindable.untilDestroy(observable);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        return baseLifecycleBindable.untilDestroy(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilDestroy(observable, onNextAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction,
                                         @NonNull final Action0 onCompletedAction) {
        return baseLifecycleBindable.untilDestroy(observable, onNextAction, onErrorAction, onCompletedAction);
    }

}
