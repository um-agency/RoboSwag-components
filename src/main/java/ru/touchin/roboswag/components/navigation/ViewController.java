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

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import ru.touchin.roboswag.components.utils.Logic;
import ru.touchin.roboswag.core.log.Lc;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * Class to control view of specific fragment, activity and application by logic bridge.
 */
public class ViewController<TLogic extends Logic,
        TActivity extends ViewControllerActivity<TLogic>,
        TFragment extends ViewControllerFragment<?, TLogic, TActivity>> {

    private static final String SUPPORT_FRAGMENT_VIEW_STATE_EXTRA = "android:view_state";

    @NonNull
    private final TActivity activity;
    @NonNull
    private final TFragment fragment;
    @NonNull
    private final ViewGroup container;
    @NonNull
    private final BehaviorSubject<Boolean> isDestroyedSubject = BehaviorSubject.create(false);

    public ViewController(@NonNull final CreationContext<TLogic, TActivity, TFragment> creationContext,
                          @Nullable final Bundle savedInstanceState) {
        this.activity = creationContext.activity;
        this.fragment = creationContext.fragment;
        this.container = creationContext.container;

        bind(getRestoreSavedStateObservable(creationContext, savedInstanceState)
                .first()
                .filter(savedState -> savedState != null))
                .subscribe(this::onRestoreSavedState);
    }

    public boolean isDestroyed() {
        return isDestroyedSubject.getValue();
    }

    /**
     * Sets {@link Observable} which will be used to get a moment when controller should restore it's state.
     * It will be waits for first non-null {@link Bundle} that contains saved state.
     *
     * @param creationContext    Context passed into {@link ViewController} constructor.
     * @param savedInstanceState Saved state of {@link ViewController}.
     * @return {@link Observable} to get restore time to.
     */
    @NonNull
    protected Observable<Bundle> getRestoreSavedStateObservable(@NonNull final CreationContext<TLogic, TActivity, TFragment> creationContext,
                                                                @Nullable final Bundle savedInstanceState) {
        return Observable.just(savedInstanceState);
    }

    /**
     * Returns application's logic.
     *
     * @return Returns logic;
     */
    @NonNull
    public TLogic getLogic() {
        return getActivity().getLogic();
    }

    /**
     * Returns view's activity.
     *
     * @return Returns activity;
     */
    @NonNull
    public TActivity getActivity() {
        return activity;
    }

    /**
     * Returns view's activity.
     *
     * @return Returns activity;
     */
    @NonNull
    public TFragment getFragment() {
        return fragment;
    }

    /**
     * Returns view instantiated in {@link #getFragment} fragment attached to {@link #getActivity} activity.
     *
     * @return Returns view;
     */
    @NonNull
    public ViewGroup getContainer() {
        return container;
    }

    /**
     * Calls when activity configuring ActionBar, Toolbar, Sidebar etc.
     * If it will be called or not depends on {@link Fragment#hasOptionsMenu()} and {@link Fragment#isMenuVisible()}.
     *
     * @param menu     The options menu in which you place your items;
     * @param inflater Helper to inflate menu items.
     */
    public void onConfigureNavigation(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        // do nothing
    }

    @NonNull
    protected <T> Observable<T> bind(@NonNull final Observable<T> observable) {
        return observable
                .onErrorResumeNext(throwable -> {
                    // there should be no exceptions during binding
                    Lc.assertion(throwable);
                    return Observable.empty();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .takeUntil(isDestroyedSubject.filter(isDestroyed -> isDestroyed).first());
    }

    /**
     * Called when savedInstanceState is ready to be restored.
     *
     * @param savedInstanceState Saved state.
     */
    protected void onRestoreSavedState(@NonNull final Bundle savedInstanceState) {
        final SparseArray<Parcelable> viewStates = savedInstanceState.getSparseParcelableArray(SUPPORT_FRAGMENT_VIEW_STATE_EXTRA);
        if (viewStates != null) {
            container.restoreHierarchyState(viewStates);
        }
    }

    public void onSaveInstanceState(@NonNull final Bundle savedInstanceState) {
        // do nothing
    }

    public void onDestroy() {
        isDestroyedSubject.onNext(true);
    }

    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        return false;
    }

    /**
     * Class to simplify constructor override.
     */
    public static class CreationContext<TLogic extends Logic,
            TActivity extends ViewControllerActivity<TLogic>,
            TFragment extends ViewControllerFragment<?, TLogic, TActivity>> {

        @NonNull
        private final TActivity activity;
        @NonNull
        private final TFragment fragment;
        @NonNull
        private final ViewGroup container;

        public CreationContext(@NonNull final TActivity activity,
                               @NonNull final TFragment fragment,
                               @NonNull final ViewGroup container) {
            this.activity = activity;
            this.fragment = fragment;
            this.container = container;
        }

        @NonNull
        public TActivity getActivity() {
            return activity;
        }

        @NonNull
        public TFragment getFragment() {
            return fragment;
        }

        @NonNull
        public ViewGroup getContainer() {
            return container;
        }

    }

}