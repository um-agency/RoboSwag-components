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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.roboswag.components.utils.UiUtils;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * TODO: fill description
 */
public abstract class AbstractBaseActivity extends AppCompatActivity
        implements FragmentManager.OnBackStackChangedListener,
        OnFragmentStartedListener {

    private static final String TOP_FRAGMENT_TAG_MARK = "TOP_FRAGMENT";

    private boolean isPaused;

    /* Returns id of main fragments container where navigation-node fragments should be */
    protected int getFragmentContainerId() {
        throw new UnsupportedOperationException("Implement getFragmentContainerId method to use fragment managing");
    }

    /* Returns if last fragment in stack is top (added by setFragment) like fragment from sidebar menu */
    public boolean isCurrentFragmentTop() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 0) {
            return true;
        }

        final String topFragmentTag = fragmentManager
                .getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1)
                .getName();
        return topFragmentTag != null && topFragmentTag.contains(TOP_FRAGMENT_TAG_MARK);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    protected void onResume() {
        isPaused = false;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isPaused = true;
        super.onPause();
    }

    @Override
    public void onFragmentStarted(@NonNull final AbstractBaseFragment fragment) {
        hideSoftInput();
    }

    /* Raises when back stack changes */
    @Override
    public void onBackStackChanged() {
        //do nothing
    }

    /* Setting fragment of special class as first in stack */
    public <T extends AbstractBaseFragment> T setFirstFragment(@NonNull final Class<T> fragmentClass) {
        return setFirstFragment(fragmentClass, null);
    }

    /* Setting fragment of special class as first in stack with args */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends AbstractBaseFragment> T setFirstFragment(@NonNull final Class<T> fragmentClass,
                                                               @Nullable final Bundle args) {
        if (isPaused) {
            //TODO: log
            return null;
        }

        final FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        final T fragment;
        try {
            fragment = (T) Fragment.instantiate(this, fragmentClass.getName(), args);
        } catch (Exception ex) {
            //TODO: log
            return null;
        }
        fragmentManager.beginTransaction()
                .replace(getFragmentContainerId(), fragment, null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        return fragment;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T extends AbstractBaseFragment> T addFragmentToStack(@NonNull final Class<T> fragmentClass,
                                                                  @Nullable final Bundle args,
                                                                  @Nullable final String backStackTag) {
        if (isPaused) {
            //TODO: log
            return null;
        }

        final T fragment;
        try {
            fragment = (T) Fragment.instantiate(this, fragmentClass.getName(), args);
        } catch (Exception ex) {
            //TODO: log
            return null;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(getFragmentContainerId(), fragment, backStackTag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(backStackTag)
                .commit();
        return (T) fragment;
    }

    /* Setting fragment of special class as top */
    public <T extends AbstractBaseFragment> T setFragment(@NonNull final Class<T> fragmentClass) {
        return setFragment(fragmentClass, null);
    }

    /* Setting fragment of special class as top with args */
    public <T extends AbstractBaseFragment> T setFragment(@NonNull final Class<T> fragmentClass, @Nullable final Bundle args) {
        return addFragmentToStack(fragmentClass, args, fragmentClass.getName() + ' ' + TOP_FRAGMENT_TAG_MARK);
    }

    /* Pushing fragment of special class to fragments stack */
    public <T extends AbstractBaseFragment> T pushFragment(@NonNull final Class<T> fragmentClass) {
        return pushFragment(fragmentClass, null);
    }

    /* Pushing fragment of special class with args to fragments stack */
    public <T extends AbstractBaseFragment> T pushFragment(@NonNull final Class<T> fragmentClass, @Nullable final Bundle args) {
        return addFragmentToStack(fragmentClass, args, fragmentClass.getName());
    }

    /* Raises when device back button pressed */
    @Override
    public void onBackPressed() {
        if (!UiUtils.tryForeachFragment(getSupportFragmentManager(), AbstractBaseFragment::onBackPressed)) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                final FragmentManager fragmentManager = getSupportFragmentManager();

                if (UiUtils.tryForeachFragment(fragmentManager, AbstractBaseFragment::onHomePressed)) {
                    return true;
                }

                final int stackSize = fragmentManager.getBackStackEntryCount();

                switch (stackSize) {
                    case 0:
                        return false;
                    case 1:
                        getSupportFragmentManager().popBackStack();
                        return true;
                    default:
                        findTopFragmentAndPopBackStackToIt(fragmentManager, stackSize);
                        return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void findTopFragmentAndPopBackStackToIt(@NonNull final FragmentManager fragmentManager, final int stackSize) {
        String lastFragmentName = fragmentManager.getBackStackEntryAt(stackSize - 1).getName();
        for (int i = stackSize - 2; i >= 0; i--) {
            final String currentFragmentName = fragmentManager.getBackStackEntryAt(i).getName();
            if (currentFragmentName == null || !currentFragmentName.equals(lastFragmentName)) {
                fragmentManager.popBackStackImmediate(currentFragmentName, 0);
                break;
            } else if (i == 0) {
                fragmentManager.popBackStackImmediate(currentFragmentName, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else {
                lastFragmentName = currentFragmentName;
            }
        }
    }

    /* Hides device keyboard */
    public void hideSoftInput() {
        if (getCurrentFocus() != null) {
            final InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            final View mainFragmentContainer = findViewById(getFragmentContainerId());
            if (mainFragmentContainer != null) {
                mainFragmentContainer.requestFocus();
            }
        }
    }

    /* Shows device keyboard */
    public void showSoftInput(@NonNull final View view) {
        final InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

}
