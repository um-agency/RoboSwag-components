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

package ru.touchin.roboswag.components.navigation.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

import ru.touchin.roboswag.components.utils.BaseLifecycleBindable;
import ru.touchin.roboswag.components.utils.LifecycleBindable;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Gavriil Sitnikov on 08/03/2016.
 * Base activity to use in components repository.
 */
@SuppressWarnings("PMD.TooManyMethods")
public abstract class BaseActivity extends AppCompatActivity
        implements LifecycleBindable {

    @NonNull
    private final ArrayList<OnBackPressedListener> onBackPressedListeners = new ArrayList<>();
    @NonNull
    private final BaseLifecycleBindable baseLifecycleBindable = new BaseLifecycleBindable();
    private boolean resumed;

    /**
     * Returns if activity resumed.
     *
     * @return True if resumed.
     */
    public boolean isActuallyResumed() {
        return resumed;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseLifecycleBindable.onCreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        baseLifecycleBindable.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
    }

    @Override
    protected void onPause() {
        resumed = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        baseLifecycleBindable.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        baseLifecycleBindable.onDestroy();
        super.onDestroy();
    }

    /**
     * Hides device keyboard that is showing over {@link Activity}.
     * Do NOT use it if keyboard is over {@link android.app.Dialog} - it won't work as they have different {@link Activity#getWindow()}.
     */
    public void hideSoftInput() {
        if (getCurrentFocus() == null) {
            return;
        }
        final InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        getWindow().getDecorView().requestFocus();
    }

    /**
     * Shows device keyboard over {@link Activity} and focuses {@link View}.
     * Do NOT use it if keyboard is over {@link android.app.Dialog} - it won't work as they have different {@link Activity#getWindow()}.
     * Do NOT use it if you are not sure that view is already added on screen.
     * Better use it onStart of element if view is part of it or onConfigureNavigation if view is part of navigation.
     *
     * @param view View to get focus for input from keyboard.
     */
    public void showSoftInput(@NonNull final View view) {
        view.requestFocus();
        final InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public void addOnBackPressedListener(@NonNull final OnBackPressedListener onBackPressedListener) {
        onBackPressedListeners.add(onBackPressedListener);
    }

    public void removeOnBackPressedListener(@NonNull final OnBackPressedListener onBackPressedListener) {
        onBackPressedListeners.remove(onBackPressedListener);
    }

    @Override
    public void onBackPressed() {
        for (final OnBackPressedListener onBackPressedListener : onBackPressedListeners) {
            if (onBackPressedListener.onBackPressed()) {
                return;
            }
        }

        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            supportFinishAfterTransition();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @SuppressWarnings("CPD-START")
    //CPD: it is same as in other implementation based on BaseLifecycleBindable
    @NonNull
    @Override
    public <T> Subscription bind(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        return baseLifecycleBindable.bind(observable, onNextAction);
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

    @SuppressWarnings("CPD-END")
    //CPD: it is same as in other implementation based on BaseLifecycleBindable
    /**
     * Interface to be implemented for someone who want to intercept device back button pressing event.
     */
    public interface OnBackPressedListener {

        /**
         * Calls when user presses device back button.
         *
         * @return True if it is processed by this object.
         */
        boolean onBackPressed();

    }

}
