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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.Serializable;
import java.lang.reflect.Constructor;

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;
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
    private final BehaviorSubject<Pair<PlaceholderView, Bundle>> viewSubject = BehaviorSubject.create();
    @Nullable
    private ViewController viewController;
    private Subscription viewControllerSubscription;
    private TState state;

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

        setHasOptionsMenu(getParentFragment() == null);

        state = savedInstanceState != null
                ? (TState) savedInstanceState.getSerializable(VIEW_CONTROLLER_STATE_EXTRA)
                : (getArguments() != null ? (TState) getArguments().getSerializable(VIEW_CONTROLLER_STATE_EXTRA) : null);
        viewControllerSubscription = createViewControllerObservable().subscribe(this::onViewControllerChanged, Lc::assertion);
    }

    @NonNull
    private Observable<ViewController> createViewControllerObservable() {
        return Observable
                .combineLatest(activitySubject
                                .switchMap(activity -> activity != null ? activity.observeLogicBridge() : Observable.just(null))
                                .distinctUntilChanged()
                                .observeOn(AndroidSchedulers.mainThread()),
                        activitySubject.distinctUntilChanged().observeOn(AndroidSchedulers.mainThread()),
                        viewSubject.distinctUntilChanged().observeOn(AndroidSchedulers.mainThread()),
                        this::getViewController);
    }

    @Nullable
    private ViewController getViewController(@Nullable final TLogicBridge logicBridge,
                                             @Nullable final TActivity activity,
                                             @Nullable final Pair<PlaceholderView, Bundle> viewInfo) {
        if (logicBridge == null || activity == null || viewInfo == null) {
            return null;
        }

        if (getViewControllerClass().getConstructors().length != 1) {
            throw OnErrorThrowable.from(new ShouldNotHappenException("There should be single constructor for " + getViewControllerClass()));
        }
        final Constructor<?> constructor = getViewControllerClass().getConstructors()[0];
        final ViewController.CreationContext<TLogicBridge, TActivity,
                ? extends ViewControllerFragment<TState, TLogicBridge, TActivity>> creationContext
                = new ViewController.CreationContext<>(logicBridge, activity, this, viewInfo.first);
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
        if(view instanceof PlaceholderView) {
            viewSubject.onNext(new Pair<>((PlaceholderView) view, savedInstanceState));
        } else {
            Lc.assertion("View should be instanceof PlaceholderView");
        }
    }

    @Override
    public void onActivityCreated(@NonNull final View view, @NonNull final TActivity activity, @Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(view, activity, savedInstanceState);
        activitySubject.onNext(activity);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (viewController != null) {
            viewController.onConfigureNavigation(menu, inflater);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        return (viewController != null && viewController.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item);
    }

    private void onViewControllerChanged(@Nullable final ViewController viewController) {
        if (this.viewController != null) {
            this.viewController.onDestroy();
        }
        this.viewController = viewController;
        if (this.viewController == null) {
            return;
        }
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
        viewSubject.onNext(null);
        super.onDestroyView(view);
    }

    @Override
    public void onDetach() {
        activitySubject.onNext(null);
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        viewControllerSubscription.unsubscribe();
        if (viewController != null && !viewController.isDestroyed()) {
            viewController.onDestroy();
            viewController = null;
        }
        super.onDestroy();
    }

    private static class PlaceholderView extends FrameLayout {

        public PlaceholderView(@NonNull final Context context) {
            super(context);
        }

    }

}
