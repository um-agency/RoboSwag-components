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
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import rx.functions.Func1;

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view == null) {
            throw new IllegalStateException("Background fragments are deprecated - view shouldn't be null");
        }
        viewController = createViewController(view, savedInstanceState);
    }

    @NonNull
    protected abstract TViewController createViewController(@NonNull View view, @Nullable Bundle savedInstanceState);

    @Override
    public void onFragmentStarted(@NonNull AbstractBaseFragment fragment) {
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

    protected void onStart(@NonNull TViewController viewController, @NonNull AbstractBaseActivity baseActivity) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null) {
            if (parentFragment instanceof OnFragmentStartedListener) {
                ((OnFragmentStartedListener) parentFragment).onFragmentStarted(this);
            }
        } else {
            baseActivity.onFragmentStarted(this);
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

    protected void onResume(@NonNull TViewController viewController, @NonNull AbstractBaseActivity baseActivity) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    private boolean tryForeachChild(Func1<AbstractBaseFragment, Boolean> actionOnChild) {
        FragmentManager fragmentManager = getChildFragmentManager();
        boolean result = false;

        if (fragmentManager.getFragments() == null) {
            return false;
        }

        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment != null
                    && fragment.isResumed()
                    && fragment instanceof AbstractBaseFragment) {
                result = result || actionOnChild.call((AbstractBaseFragment) fragment);
            }
        }
        return result;
    }

    /* Raises when device back button pressed */
    public boolean onBackPressed() {
        return tryForeachChild(AbstractBaseFragment::onBackPressed);
    }

    /* Raises when ActionBar home button pressed */
    public boolean onHomePressed() {
        return tryForeachChild(AbstractBaseFragment::onHomePressed);
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

    protected void onPause(@NonNull TViewController viewController, @NonNull AbstractBaseActivity baseActivity) {
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

    protected void onStop(@NonNull TViewController viewController, @NonNull AbstractBaseActivity baseActivity) {
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

    protected void onDestroyView(@NonNull TViewController viewController) {
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

        public ViewController(@NonNull View view) {
            context = view.getContext();
        }

        protected void onDestroy() {
            postHandler.removeCallbacksAndMessages(null);
        }

    }

}
