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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import org.roboswag.components.utils.UiUtils;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * TODO: fill description
 */
public abstract class AbstractBaseFragment<TViewController extends AbstractBaseFragment.ViewController> extends Fragment
        implements OnFragmentStartedListener {

    @Nullable
    private TViewController viewController;

    /* Returns base activity */
    @Nullable
    protected AbstractBaseActivity getBaseActivity() {
        return (AbstractBaseActivity) getActivity();
    }

    @Nullable
    protected TViewController getViewController() {
        return viewController;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view == null) {
            throw new IllegalStateException("Background fragments are deprecated - view shouldn't be null");
        }
        viewController = createViewController(view, savedInstanceState);
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
    public boolean onBackPressed() {
        return UiUtils.tryForeachFragment(getChildFragmentManager(), AbstractBaseFragment::onBackPressed);
    }

    /* Raises when ActionBar home button pressed */
    public boolean onHomePressed() {
        return UiUtils.tryForeachFragment(getChildFragmentManager(), AbstractBaseFragment::onHomePressed);
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

    protected void onPause(@NonNull final TViewController viewController, @NonNull final AbstractBaseActivity baseActivity) {
        //do nothing
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

    public class ViewController {

        private final Context context;
        private final Handler postHandler = new Handler();

        /* Returns post handler to executes code on UI thread */
        @NonNull
        protected Handler getPostHandler() {
            return postHandler;
        }

        @NonNull
        protected Context getContext() {
            return context;
        }

        public ViewController(@NonNull final View view) {
            context = view.getContext();
        }

        protected void onDestroy() {
            postHandler.removeCallbacksAndMessages(null);
        }

    }

}
