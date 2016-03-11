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
import android.util.SparseArray;
import android.view.ViewGroup;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * Class to control view of specific fragment, activity and application by logic bridge.
 */
public class ViewController<TLogicBridge,
        TActivity extends ViewControllerActivity<TLogicBridge>,
        TFragment extends ViewControllerFragment<?, TLogicBridge, TActivity>> {

    private static final String SUPPORT_FRAGMENT_VIEW_STATE_EXTRA = "android:view_state";

    @NonNull
    private final TLogicBridge logicBridge;
    @NonNull
    private final TActivity activity;
    @NonNull
    private final TFragment fragment;
    @NonNull
    private final ViewGroup container;
    @NonNull
    private final Subscription savedStateSubscription;

    public ViewController(@NonNull final CreationContext<TLogicBridge, TActivity, TFragment> creationContext,
                          @Nullable final Bundle savedInstanceState) {
        this.logicBridge = creationContext.logicBridge;
        this.activity = creationContext.activity;
        this.fragment = creationContext.fragment;
        this.container = creationContext.container;

        savedStateSubscription = getRestoreSavedStateObservable(creationContext, savedInstanceState)
                .first()
                .filter(savedState -> savedState != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onRestoreSavedState);
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
    protected Observable<Bundle> getRestoreSavedStateObservable(@NonNull final CreationContext<TLogicBridge, TActivity, TFragment> creationContext,
                                                                @Nullable final Bundle savedInstanceState) {
        return Observable.just(savedInstanceState);
    }

    /**
     * Returns logic bridge to use and affect application logic.
     *
     * @return Returns logic bridge object.
     */
    @NonNull
    public TLogicBridge getLogicBridge() {
        return logicBridge;
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
        savedStateSubscription.unsubscribe();
    }


    /**
     * Class to simplify constructor override.
     */
    public static class CreationContext<TLogicBridge,
            TActivity extends ViewControllerActivity<TLogicBridge>,
            TFragment extends ViewControllerFragment<?, TLogicBridge, TActivity>> {

        @NonNull
        private final TLogicBridge logicBridge;
        @NonNull
        private final TActivity activity;
        @NonNull
        private final TFragment fragment;
        @NonNull
        private final ViewGroup container;

        public CreationContext(@NonNull final TLogicBridge logicBridge,
                               @NonNull final TActivity activity,
                               @NonNull final TFragment fragment,
                               @NonNull final ViewGroup container) {
            this.logicBridge = logicBridge;
            this.activity = activity;
            this.fragment = fragment;
            this.container = container;
        }

    }

}