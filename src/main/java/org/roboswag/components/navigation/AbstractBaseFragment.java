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

package org.roboswag.components.navigation;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.View;

import org.roboswag.components.savestate.AbstractSavedStateController;
import org.roboswag.components.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * TODO: fill description
 */
// Yes, it's a God class with a lot of methods. Deal with it
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods"})
public abstract class AbstractBaseFragment<TViewController extends AbstractBaseFragment.ViewController> extends Fragment
        implements OnFragmentStartedListener {

    @Nullable
    private TViewController viewController;
    @Nullable
    private Map<String, Parcelable> tempSavedStates;

    /* Returns base activity */
    @Nullable
    protected AbstractBaseActivity getBaseActivity() {
        return (AbstractBaseActivity) getActivity();
    }

    @Nullable
    protected TViewController getViewController() {
        return viewController;
    }

    public boolean isNestedFragment() {
        return getParentFragment() != null || getTargetFragment() != null;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(!isNestedFragment());
    }

    protected void configureActionBar(@NonNull final AbstractBaseActivity baseActivity) {
        //do nothing
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // activity could be null if fragment has added as child but not attached to activity yet
        if (getBaseActivity() != null) {
            configureActionBar(getBaseActivity());
        }
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view == null) {
            throw new IllegalStateException("Background fragments are deprecated - view shouldn't be null");
        }
        viewController = createViewController(view, savedInstanceState);
        if (viewController.doRestoreStateOnCreate()) {
            viewController.restoreState();
        }
    }

    @NonNull
    protected abstract TViewController createViewController(@NonNull final View view, @Nullable final Bundle savedInstanceState);

    @Override
    public void onFragmentStarted(@NonNull final AbstractBaseFragment fragment) {
        //do nothing
    }

    @Deprecated
    @Override
    public void onStart() {
        super.onStart();
        if (viewController == null || getBaseActivity() == null) {
            throw new IllegalStateException("ViewController or BaseActivity is null at onStart point");
        }
        onStart(viewController, getBaseActivity());
    }

    protected void onStart(@NonNull final TViewController viewController, @NonNull final AbstractBaseActivity baseActivity) {
        final Fragment parentFragment = getParentFragment();
        if (parentFragment == null) {
            baseActivity.onFragmentStarted(this);
        } else {
            if (parentFragment instanceof OnFragmentStartedListener) {
                ((OnFragmentStartedListener) parentFragment).onFragmentStarted(this);
            }
        }
    }

    @Deprecated
    @Override
    public void onResume() {
        super.onResume();
        if (viewController == null || getBaseActivity() == null) {
            throw new IllegalStateException("ViewController or BaseActivity is null at onResume point");
        }
        onResume(viewController, getBaseActivity());
    }

    protected void onResume(@NonNull final TViewController viewController, @NonNull final AbstractBaseActivity baseActivity) {
        //do nothing
    }

    /* Raises when device back button pressed */
    public boolean onBackPressed(@NonNull final AbstractBaseActivity baseActivity) {
        return UiUtils.tryForeachFragment(getChildFragmentManager(), fragment -> fragment.onBackPressed(baseActivity));
    }

    /* Raises when ActionBar home button pressed */
    public boolean onHomePressed(@NonNull final AbstractBaseActivity baseActivity) {
        return UiUtils.tryForeachFragment(getChildFragmentManager(), fragment -> fragment.onHomePressed(baseActivity));
    }

    @Deprecated
    @Override
    public void onPause() {
        if (viewController == null || getBaseActivity() == null) {
            throw new IllegalStateException("ViewController or BaseActivity is null at onPause point");
        }
        onPause(viewController, getBaseActivity());
        super.onPause();
    }

    @SuppressWarnings("unchecked")
    protected void onPause(@NonNull final TViewController viewController, @NonNull final AbstractBaseActivity baseActivity) {
        tempSavedStates = viewController.getSavedStates();
    }

    @Override
    public void onSaveInstanceState(final Bundle stateToSave) {
        super.onSaveInstanceState(stateToSave);
        if (tempSavedStates != null) {
            for (final Map.Entry<String, Parcelable> entry : tempSavedStates.entrySet()) {
                stateToSave.putParcelable(entry.getKey(), entry.getValue());
            }
        }
    }

    @Deprecated
    @Override
    public void onStop() {
        if (viewController == null || getBaseActivity() == null) {
            throw new IllegalStateException("ViewController or BaseActivity is null at onStop point");
        }
        onStop(viewController, getBaseActivity());
        super.onStop();
    }

    protected void onStop(@NonNull final TViewController viewController, @NonNull final AbstractBaseActivity baseActivity) {
        //do nothing
    }

    @Deprecated
    @Override
    public void onDestroyView() {
        if (viewController == null) {
            throw new IllegalStateException("ViewController is null at onStop point");
        }
        onDestroyView(viewController);
        super.onDestroyView();
        this.viewController = null;
    }

    protected void onDestroyView(@NonNull final TViewController viewController) {
        viewController.onDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tempSavedStates = null;
    }

    public class ViewController {

        private final Context context;
        @Nullable
        private final Bundle savedInstanceState;
        private final Handler postHandler = new Handler();
        private final List<AbstractSavedStateController> savedStateControllers = new ArrayList<>();

        public ViewController(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
            context = view.getContext();
            this.savedInstanceState = savedInstanceState;
        }

        @NonNull
        public Map<String, Parcelable> getSavedStates() {
            final Map<String, Parcelable> result = new HashMap<>();
            for (final AbstractSavedStateController savedStateController : savedStateControllers) {
                result.put(String.valueOf(savedStateController.getId()), savedStateController.getState());
            }
            return result;
        }

        protected void attachSavedStateController(@NonNull final AbstractSavedStateController savedStateController) {
            savedStateControllers.add(savedStateController);
        }

        public void restoreState() {
            if (savedInstanceState == null && tempSavedStates == null) {
                return;
            }
            for (final AbstractSavedStateController savedStateController : savedStateControllers) {
                final String key = String.valueOf(savedStateController.getId());
                final Parcelable savedState = tempSavedStates != null
                        ? tempSavedStates.get(key)
                        : (savedInstanceState != null ? savedInstanceState.getParcelable(key) : null);
                if (savedState != null) {
                    savedStateController.restoreState(savedState);
                }
            }
            tempSavedStates = null;
        }

        /* Returns post handler to executes code on UI thread */
        @NonNull
        protected Handler getPostHandler() {
            return postHandler;
        }

        @NonNull
        protected Context getContext() {
            return context;
        }

        protected boolean doRestoreStateOnCreate() {
            return true;
        }

        protected void onDestroy() {
            postHandler.removeCallbacksAndMessages(null);
        }

    }

}
