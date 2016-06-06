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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import ru.touchin.roboswag.components.navigation.activities.ViewControllerActivity;
import ru.touchin.roboswag.components.navigation.fragments.ViewControllerFragment;
import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * Class to control view of specific fragment, activity and application by logic bridge.
 */
public class ViewController<TActivity extends ViewControllerActivity<?>,
        TFragment extends ViewControllerFragment<?, TActivity>>
        implements UiBindable {

    @NonNull
    private final TActivity activity;
    @NonNull
    private final TFragment fragment;
    @NonNull
    private final ViewGroup container;
    @NonNull
    private final BehaviorSubject<Boolean> isCreatedSubject = BehaviorSubject.create(true);
    @NonNull
    private final BehaviorSubject<Boolean> isStartedSubject = BehaviorSubject.create();
    @NonNull
    private final BaseUiBindable baseUiBindable = new BaseUiBindable(isCreatedSubject, isStartedSubject);

    @SuppressWarnings("PMD.UnusedFormalParameter")
    //UnusedFormalParameter: savedInstanceState could be used by children
    public ViewController(@NonNull final CreationContext<TActivity, TFragment> creationContext,
                          @Nullable final Bundle savedInstanceState) {
        this.activity = creationContext.activity;
        this.fragment = creationContext.fragment;
        this.container = creationContext.container;
    }

    public boolean isDestroyed() {
        return !isCreatedSubject.getValue();
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
     * Return a localized string from the application's package's default string table.
     *
     * @param resId Resource id for the string
     */
    public final String getString(@StringRes final int resId) {
        return getActivity().getString(resId);
    }

    /**
     * Return a localized formatted string from the application's package's default string table, substituting the format arguments as defined in
     * {@link java.util.Formatter} and {@link java.lang.String#format}.
     *
     * @param resId      Resource id for the format string
     * @param formatArgs The format arguments that will be used for substitution.
     */
    public final String getString(@StringRes final int resId, final Object... formatArgs) {
        return getActivity().getString(resId, formatArgs);
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
    public <T> Observable<T> bind(@NonNull final Observable<T> observable) {
        return baseUiBindable.bind(observable);
    }

    @NonNull
    public <T> Observable<T> untilStop(@NonNull final Observable<T> observable) {
        return baseUiBindable.untilStop(observable);
    }

    @NonNull
    @Override
    public <T> Observable<T> untilDestroy(@NonNull final Observable<T> observable) {
        return baseUiBindable.untilDestroy(observable);
    }

    public void onStart() {
        isStartedSubject.onNext(true);
    }

    public void onAppear(@NonNull final ViewControllerFragment.AppearType appearType) {
        //do nothing
    }

    public void onSaveInstanceState(@NonNull final Bundle savedInstanceState) {
        // do nothing
    }

    public void onStop() {
        isStartedSubject.onNext(false);
    }

    public void onDestroy() {
        isCreatedSubject.onNext(false);
    }

    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        return false;
    }

    /**
     * Class to simplify constructor override.
     */
    public static class CreationContext<TActivity extends ViewControllerActivity<?>,
            TFragment extends ViewControllerFragment<?, TActivity>> {

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

    }

}