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
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.Serializable;
import java.lang.reflect.Constructor;

import ru.touchin.roboswag.components.services.LogicService;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.android.RxAndroidUtils;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * Fragment that creates {@link ViewController} between {@link #onViewCreated} and {@link #onDestroyView}.
 * [phase 1]
 */
public abstract class ViewControllerFragment<TState extends Serializable, TLogicBridge, TActivity extends AppCompatActivity>
        extends ViewFragment<TActivity> {

    private static final String VIEW_CONTROLLER_STATE_EXTRA = "VIEW_CONTROLLER_STATE_EXTRA";

    /**
     * Creates {@link Bundle} which will store state.
     *
     * @param state State to use into ViewController.
     * @return Returns bundle with state inside.
     */
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

    /**
     * Returns specific object which contains state of ViewController.
     *
     * @return Object of TState type.
     */
    @Nullable
    public TState getState() {
        return state;
    }

    /**
     * It should return specific Service class where from this fragment should get interface to logic.
     *
     * @return Returns class of specific LogicService.
     */
    @NonNull
    protected abstract Class<? extends LogicService<TLogicBridge>> getLogicServiceClass();

    /**
     * It should return specific ViewController class to control instantiated view by logic bridge after activity creation.
     *
     * @return Returns class of specific ViewController.
     */
    @NonNull
    protected abstract Class<? extends ViewController<TState, TLogicBridge, TActivity,
            ? extends ViewControllerFragment<TState, TLogicBridge, TActivity>>> getViewControllerClass();

    // need throwable for app stability
    @SuppressWarnings({"PMD.AvoidCatchingThrowable", "unchecked"})
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() == null) {
            Lc.assertion("Context is null in onCreate");
        }

        state = savedInstanceState != null
                ? (TState) savedInstanceState.getSerializable(VIEW_CONTROLLER_STATE_EXTRA)
                : (getArguments() != null ? (TState) getArguments().getSerializable(VIEW_CONTROLLER_STATE_EXTRA) : null);

        viewControllerSubscription = Observable
                .combineLatest(RxAndroidUtils.observeService(getContext(), getLogicServiceClass())
                                .map(service -> service != null ? service.getLogicBridge() : null)
                                .distinctUntilChanged()
                                .observeOn(backgroundScheduler),
                        activitySubject.distinctUntilChanged().observeOn(backgroundScheduler),
                        viewSubject.distinctUntilChanged().observeOn(backgroundScheduler),
                        (logicBridge, activity, view) -> {
                            if (activity == null || view == null || logicBridge == null) {
                                return null;
                            }

                            final ViewController.CreationContext<? extends Serializable, TLogicBridge, TActivity,
                                    ? extends ViewControllerFragment<TState, TLogicBridge, TActivity>> creationContext
                                    = new ViewController.CreationContext<>(logicBridge, activity, this, view.first);
                            if (getViewControllerClass().getConstructors().length > 1) {
                                Lc.assertion("There should be single constructor for " + getViewControllerClass());
                                return null;
                            }
                            try {
                                final Constructor<?> constructor = getViewControllerClass().getConstructors()[0];
                                switch (constructor.getParameterTypes().length) {
                                    case 2:
                                        return (ViewController) constructor.newInstance(creationContext, view.second);
                                    case 3:
                                        return (ViewController) constructor.newInstance(this, creationContext, view.second);
                                    default:
                                        Lc.assertion("Wrong constructor parameters count: " + constructor.getParameterTypes().length);
                                        return null;
                                }
                            } catch (final Throwable throwable) {
                                Lc.assertion(throwable);
                                return null;
                            }
                        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onViewControllerChanged);
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
        if (getView() instanceof PlaceholderView) {
            ((PlaceholderView) getView()).removeAllViews();
            ((PlaceholderView) getView())
                    .addView(this.viewController.getContainer(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            Lc.assertion("View of fragment should be PlaceholderView");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle stateToSave) {
        super.onSaveInstanceState(stateToSave);
        if (viewController != null) {
            stateToSave.putSerializable(VIEW_CONTROLLER_STATE_EXTRA, state);
        } else if (getArguments() != null && getArguments().containsKey(VIEW_CONTROLLER_STATE_EXTRA)) {
            stateToSave.putSerializable(VIEW_CONTROLLER_STATE_EXTRA, getArguments().getSerializable(VIEW_CONTROLLER_STATE_EXTRA));
        }
    }

    @Override
    protected void onDestroyView(@NonNull final View view) {
        viewSubject.onNext(null);
        super.onDestroyView(view);
    }

    @Override
    public void onDestroy() {
        viewControllerSubscription.unsubscribe();
        super.onDestroy();
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
