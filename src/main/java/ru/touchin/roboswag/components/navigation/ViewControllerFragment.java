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

package ru.touchin.roboswag.components.navigation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.Serializable;
import java.lang.reflect.Constructor;

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import ru.touchin.roboswag.core.utils.android.RxAndroidUtils;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorThrowable;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * Fragment instantiated in specific activity of {@link TActivity} type that is holding {@link ViewController} inside.
 */
public abstract class ViewControllerFragment<TState extends Serializable, TLogicBridge, TActivity extends ViewControllerActivity<TLogicBridge>>
        extends ViewFragment<TActivity> {

    private static final String VIEW_CONTROLLER_STATE_EXTRA = "VIEW_CONTROLLER_STATE_EXTRA";

    /**
     * Creates {@link Bundle} which will store state.
     *
     * @param state State to use into ViewController.
     * @return Returns bundle with state inside.
     */
    @NonNull
    public static Bundle createState(@Nullable final Serializable state) {
        final Bundle result = new Bundle();
        result.putSerializable(VIEW_CONTROLLER_STATE_EXTRA, state);
        return result;
    }

    private final BehaviorSubject<TActivity> activitySubject = BehaviorSubject.create();
    private final BehaviorSubject<Pair<ViewGroup, Bundle>> viewSubject = BehaviorSubject.create();
    private final Scheduler backgroundScheduler = RxAndroidUtils.createLooperScheduler();
    @Nullable
    private ViewController viewController;
    private Subscription viewControllerSubscription;
    private TState state;

    private final Observable<ViewController> viewControllerObservable = Observable
            .combineLatest(activitySubject
                            .switchMap(activity -> activity != null ? activity.observeLogicBridge() : Observable.just(null))
                            .distinctUntilChanged()
                            .observeOn(backgroundScheduler),
                    activitySubject.distinctUntilChanged().observeOn(backgroundScheduler),
                    viewSubject.distinctUntilChanged().observeOn(backgroundScheduler),
                    (logicBridge, activity, viewInfo) -> {
                        if (logicBridge == null || activity == null || viewInfo == null) {
                            return null;
                        }

                        final ViewController.CreationContext<TLogicBridge, TActivity,
                                ? extends ViewControllerFragment<TState, TLogicBridge, TActivity>> creationContext
                                = new ViewController.CreationContext<>(logicBridge, activity, this, viewInfo.first);
                        if (getViewControllerClass().getConstructors().length != 1) {
                            throw OnErrorThrowable
                                    .from(new ShouldNotHappenException("There should be single constructor for " + getViewControllerClass()));
                        }
                        final Constructor<?> constructor = getViewControllerClass().getConstructors()[0];
                        try {
                            switch (constructor.getParameterTypes().length) {
                                case 2:
                                    return (ViewController) constructor.newInstance(creationContext, viewInfo.second);
                                case 3:
                                    return (ViewController) constructor.newInstance(this, creationContext, viewInfo.second);
                                default:
                                    Lc.assertion("Wrong constructor parameters count: " + constructor.getParameterTypes().length);
                                    return null;
                            }
                        } catch (final Exception exception) {
                            throw OnErrorThrowable.from(exception);
                        }
                    })
            .observeOn(AndroidSchedulers.mainThread());

    /**
     * Returns specific object which contains state of ViewController.
     *
     * @return Object of TState type.
     */
    public TState getState() {
        return state;
    }

    /**
     * It should return specific ViewController class to control instantiated view by logic bridge after activity creation.
     *
     * @return Returns class of specific ViewController.
     */
    @NonNull
    public abstract Class<? extends ViewController<TLogicBridge, TActivity,
            ? extends ViewControllerFragment<TState, TLogicBridge, TActivity>>> getViewControllerClass();

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() == null) {
            Lc.assertion("Context is null in onCreate");
            return;
        }

        state = savedInstanceState != null
                ? (TState) savedInstanceState.getSerializable(VIEW_CONTROLLER_STATE_EXTRA)
                : (getArguments() != null ? (TState) getArguments().getSerializable(VIEW_CONTROLLER_STATE_EXTRA) : null);
    }

    @Deprecated
    @NonNull
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return new PlaceholderView(inflater.getContext());
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewSubject.onNext(new Pair<>(new FrameLayout(view.getContext()), savedInstanceState));
        viewControllerSubscription = viewControllerObservable.subscribe(this::onViewControllerChanged, Lc::assertion);
    }

    @Override
    public void onActivityCreated(@NonNull final View view, @NonNull final TActivity activity, @Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(view, activity, savedInstanceState);
        activitySubject.onNext(activity);
    }

    private void onViewControllerChanged(@Nullable final ViewController viewController) {
        if (this.viewController != null) {
            this.viewController.onDestroy();
        }
        this.viewController = viewController;
        if (this.viewController == null) {
            return;
        }
        if (getView() == null || !(getView() instanceof PlaceholderView)) {
            Lc.assertion("View of fragment should be PlaceholderView");
            return;
        }
        ((PlaceholderView) getView()).removeAllViews();
        ((PlaceholderView) getView())
                .addView(this.viewController.getContainer(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        viewController.getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (viewController != null) {
            viewController.onSaveInstanceState(savedInstanceState);
            savedInstanceState.putSerializable(VIEW_CONTROLLER_STATE_EXTRA, state);
        } else if (getArguments() != null && getArguments().containsKey(VIEW_CONTROLLER_STATE_EXTRA)) {
            savedInstanceState.putSerializable(VIEW_CONTROLLER_STATE_EXTRA, getArguments().getSerializable(VIEW_CONTROLLER_STATE_EXTRA));
        }
    }

    @Override
    protected void onDestroyView(@NonNull final View view) {
        viewControllerSubscription.unsubscribe();
        viewControllerSubscription = null;
        viewSubject.onNext(null);
        super.onDestroyView(view);
    }

    @Override
    public void onDetach() {
        activitySubject.onNext(null);
        super.onDetach();
    }

    private static class PlaceholderView extends FrameLayout {

        public PlaceholderView(@NonNull final Context context) {
            super(context);
        }

    }

}
