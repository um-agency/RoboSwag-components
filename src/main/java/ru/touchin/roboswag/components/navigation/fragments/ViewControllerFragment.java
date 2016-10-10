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

package ru.touchin.roboswag.components.navigation.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import ru.touchin.roboswag.components.navigation.AbstractState;
import ru.touchin.roboswag.components.navigation.ViewController;
import ru.touchin.roboswag.components.navigation.activities.ViewControllerActivity;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;
import rx.Subscription;
import rx.exceptions.OnErrorThrowable;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * Fragment instantiated in specific activity of {@link TActivity} type that is holding {@link ViewController} inside.
 *
 * @param <TState>    Type of object which is representing it's fragment state;
 * @param <TActivity> Type of {@link ViewControllerActivity} where fragment could be attached to.
 */
@SuppressWarnings("PMD.TooManyMethods")
public abstract class ViewControllerFragment<TState extends AbstractState, TActivity extends ViewControllerActivity<?>>
        extends ViewFragment<TActivity> {

    private static final String VIEW_CONTROLLER_STATE_EXTRA = "VIEW_CONTROLLER_STATE_EXTRA";

    private static boolean inDebugMode;

    /**
     * Enables debugging features like serialization of {@link #getState()} every creation.
     */
    public static void setInDebugMode() {
        inDebugMode = true;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private static <T extends Serializable> T reserialize(@NonNull final T serializable) {
        Parcel parcel = Parcel.obtain();
        parcel.writeSerializable(serializable);
        final byte[] serializableBytes = parcel.marshall();
        parcel.recycle();
        parcel = Parcel.obtain();
        parcel.unmarshall(serializableBytes, 0, serializableBytes.length);
        parcel.setDataPosition(0);
        final T result = (T) parcel.readSerializable();
        parcel.recycle();
        return result;
    }

    /**
     * Creates {@link Bundle} which will store state.
     *
     * @param state State to use into ViewController.
     * @return Returns bundle with state inside.
     */
    @NonNull
    public static Bundle createState(@Nullable final AbstractState state) {
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
    private boolean isStarted;

    /**
     * Returns specific {@link AbstractState} which contains state of fragment and it's {@link ViewController}.
     *
     * @return Object represents state.
     */
    public TState getState() {
        return state;
    }

    /**
     * It should return specific {@link ViewController} class to control instantiated view by logic after activity creation.
     *
     * @return Returns class of specific {@link ViewController}.
     */
    @NonNull
    public abstract Class<? extends ViewController<TActivity,
            ? extends ViewControllerFragment<TState, TActivity>>> getViewControllerClass();

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(!isChildFragment());

        state = savedInstanceState != null
                ? (TState) savedInstanceState.getSerializable(VIEW_CONTROLLER_STATE_EXTRA)
                : (getArguments() != null ? (TState) getArguments().getSerializable(VIEW_CONTROLLER_STATE_EXTRA) : null);
        if (state != null) {
            if (inDebugMode) {
                state = reserialize(state);
            }
            state.onCreate();
        }
        viewControllerSubscription = Observable
                .combineLatest(activitySubject.distinctUntilChanged(), viewSubject.distinctUntilChanged(),
                        (activity, viewInfo) -> {
                            final ViewController newViewController = createViewController(activity, viewInfo);
                            if (newViewController != null) {
                                newViewController.onCreate();
                            }
                            return newViewController;
                        })
                .subscribe(this::onViewControllerChanged,
                        throwable -> Lc.cutAssertion(throwable,
                                OnErrorThrowable.class, InvocationTargetException.class, InflateException.class));
    }

    @Nullable
    private ViewController createViewController(@Nullable final TActivity activity,
                                                @Nullable final Pair<PlaceholderView, Bundle> viewInfo) {
        if (activity == null || viewInfo == null) {
            return null;
        }

        if (getViewControllerClass().getConstructors().length != 1) {
            throw OnErrorThrowable.from(new ShouldNotHappenException("There should be single constructor for " + getViewControllerClass()));
        }
        final Constructor<?> constructor = getViewControllerClass().getConstructors()[0];
        final ViewController.CreationContext creationContext = new ViewController.CreationContext(activity, this, viewInfo.first);
        try {
            switch (constructor.getParameterTypes().length) {
                case 2:
                    return (ViewController) constructor.newInstance(creationContext, viewInfo.second);
                case 3:
                    return (ViewController) constructor.newInstance(this, creationContext, viewInfo.second);
                default:
                    throw OnErrorThrowable
                            .from(new ShouldNotHappenException("Wrong constructor parameters count: " + constructor.getParameterTypes().length));
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
        if (view instanceof PlaceholderView) {
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
    protected void onStart(@NonNull final View view, @NonNull final TActivity activity) {
        super.onStart(view, activity);
        isStarted = true;
        if (viewController != null) {
            viewController.onStart();
        }
    }

    @Override
    protected void onAppear(@NonNull final View view, @NonNull final TActivity activity) {
        super.onAppear(view, activity);
        if (viewController != null) {
            viewController.onAppear();
        }
    }

    @Override
    protected void onResume(@NonNull final View view, @NonNull final TActivity activity) {
        super.onResume(view, activity);
        if (viewController != null) {
            viewController.onResume();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (viewController != null) {
            viewController.onLowMemory();
        }
    }

    /**
     * Calls when activity configuring ActionBar, Toolbar, Sidebar etc.
     * If it will be called or not depends on {@link #hasOptionsMenu()} and {@link #isMenuVisible()}.
     *
     * @param menu     The options menu in which you place your items;
     * @param inflater Helper to inflate menu items.
     */
    protected void onConfigureNavigation(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        if (viewController != null) {
            viewController.onConfigureNavigation(menu, inflater);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        onConfigureNavigation(menu, inflater);
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
        if (this.viewController != null) {
            if (isStarted) {
                this.viewController.onStart();
            }
            this.viewController.getActivity().supportInvalidateOptionsMenu();
        }
    }

    @Override
    protected void onPause(@NonNull final View view, @NonNull final TActivity activity) {
        super.onPause(view, activity);
        if (viewController != null) {
            viewController.onPause();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (viewController != null) {
            viewController.onSaveInstanceState(savedInstanceState);
        }
        savedInstanceState.putSerializable(VIEW_CONTROLLER_STATE_EXTRA, state);
    }

    @Override
    protected void onDisappear(@NonNull final View view, @NonNull final TActivity activity) {
        super.onDisappear(view, activity);
        if (viewController != null) {
            viewController.onDisappear();
        }
    }

    @Override
    protected void onStop(@NonNull final View view, @NonNull final TActivity activity) {
        isStarted = false;
        if (viewController != null) {
            viewController.onStop();
        }
        super.onStop(view, activity);
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
