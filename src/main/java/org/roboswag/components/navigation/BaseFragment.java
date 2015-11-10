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
import android.view.View;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * TODO: fill description
 */
public abstract class BaseFragment<TViewHolder extends BaseFragment.ViewHolder> extends Fragment
        implements OnFragmentStartedListener {

    @Nullable
    private TViewHolder viewHolder;

    /* Returns base activity */
    @Nullable
    protected BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    @Nullable
    protected TViewHolder getViewHolder() {
        return viewHolder;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view == null) {
            throw new IllegalStateException("Background fragments are deprecated - view shouldn't be null");
        }
        viewHolder = createViewHolder(view, savedInstanceState);
    }

    @NonNull
    protected abstract TViewHolder createViewHolder(@NonNull View view, @Nullable Bundle savedInstanceState);

    @Override
    public void onFragmentStarted(BaseFragment fragment) {
    }

    @Deprecated
    @Override
    public void onStart() {
        super.onStart();
        if (viewHolder == null || getBaseActivity() == null) {
            throw new IllegalStateException("ViewHolder or BaseActivity is null at onStart point");
        }
        onStart(viewHolder, getBaseActivity());
    }

    protected void onStart(@NonNull TViewHolder viewHolder, @NonNull BaseActivity baseActivity) {
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
        if (viewHolder == null || getBaseActivity() == null) {
            throw new IllegalStateException("ViewHolder or BaseActivity is null at onResume point");
        }
        onResume(viewHolder, getBaseActivity());
    }

    protected void onResume(@NonNull TViewHolder viewHolder, @NonNull BaseActivity baseActivity) {
    }

    /* Raises when device back button pressed */
    public boolean onBackPressed() {
        FragmentManager fragmentManager = getChildFragmentManager();
        boolean result = false;

        if (fragmentManager.getFragments() == null) {
            return false;
        }

        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment != null
                    && fragment.isResumed()
                    && fragment instanceof BaseFragment) {
                result = result || ((BaseFragment) fragment).onBackPressed();
            }
        }
        return result;
    }

    /* Raises when ActionBar home button pressed */
    public boolean onHomePressed() {
        FragmentManager fragmentManager = getChildFragmentManager();
        boolean result = false;

        if (fragmentManager.getFragments() == null) {
            return false;
        }

        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment != null
                    && fragment.isResumed()
                    && fragment instanceof BaseFragment) {
                result = result || ((BaseFragment) fragment).onHomePressed();
            }
        }
        return result;
    }

    @Deprecated
    @Override
    public void onPause() {
        if (viewHolder == null || getBaseActivity() == null) {
            throw new IllegalStateException("ViewHolder or BaseActivity is null at onPause point");
        }
        onPause(viewHolder, getBaseActivity());
        super.onPause();
    }

    protected void onPause(@NonNull TViewHolder viewHolder, @NonNull BaseActivity baseActivity) {
    }

    @Deprecated
    @Override
    public void onStop() {
        if (viewHolder == null || getBaseActivity() == null) {
            throw new IllegalStateException("ViewHolder or BaseActivity is null at onStop point");
        }
        onStop(viewHolder, getBaseActivity());
        super.onStop();
    }

    protected void onStop(@NonNull TViewHolder viewHolder, @NonNull BaseActivity baseActivity) {
    }

    @Deprecated
    @Override
    public void onDestroyView() {
        if (viewHolder == null) {
            throw new IllegalStateException("ViewHolder is null at onStop point");
        }
        onDestroyView(viewHolder);
        super.onDestroyView();
        this.viewHolder = null;
    }

    protected void onDestroyView(@NonNull TViewHolder viewHolder) {
        viewHolder.onDestroy();
    }

    public class ViewHolder {

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

        public ViewHolder(@NonNull View view){
            context = view.getContext();
        }

        protected void onDestroy() {
            postHandler.removeCallbacksAndMessages(null);
        }

    }

}
