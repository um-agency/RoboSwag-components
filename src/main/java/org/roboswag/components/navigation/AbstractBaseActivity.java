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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * TODO: fill description
 */
public abstract class AbstractBaseActivity extends AppCompatActivity
        implements FragmentManager.OnBackStackChangedListener,
        OnFragmentStartedListener {

    private static final String TOP_FRAGMENT_TAG_MARK = "TOP_FRAGMENT";

    private boolean isPaused = false;

    /* Returns id of main fragments container where navigation-node fragments should be */
    protected int getFragmentContainerId() {
        throw new UnsupportedOperationException("Implement getFragmentContainerId method to use fragment managing");
    }

    /* Returns if last fragment in stack is top (added by setFragment) like fragment from sidebar menu */
    public boolean isCurrentFragmentTop() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 0) {
            return true;
        }

        String topFragmentTag = fragmentManager
                .getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1)
                .getName();
        return topFragmentTag != null && topFragmentTag.contains(TOP_FRAGMENT_TAG_MARK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    public void onFragmentStarted(@NonNull AbstractBaseFragment fragment) {
    }

    /* Raises when back stack changes */
    @Override
    public void onBackStackChanged() {
    }

    /* Setting fragment of special class as first in stack */
    public Fragment setFirstFragment(Class<?> fragmentClass) {
        return setFirstFragment(fragmentClass, null);
    }

    /* Setting fragment of special class as first in stack with args */
    public Fragment setFirstFragment(Class<?> fragmentClass, Bundle args) {
        if (isPaused) {
            //TODO: log
            return null;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        Fragment fragment = Fragment.instantiate(this, fragmentClass.getName(), args);
        fragmentManager.beginTransaction()
                .replace(getFragmentContainerId(), fragment, null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        return fragment;
    }

    private Fragment addFragmentToStack(Class<?> fragmentClass, Bundle args, String backStackTag) {
        if (isPaused) {
            //TODO: log
            return null;
        }

        Fragment fragment = Fragment.instantiate(this, fragmentClass.getName(), args);
        getSupportFragmentManager().beginTransaction()
                .replace(getFragmentContainerId(), fragment, backStackTag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(backStackTag)
                .commit();
        return fragment;
    }

    /* Setting fragment of special class as top */
    public Fragment setFragment(Class fragmentClass) {
        return setFragment(fragmentClass, null);
    }

    /* Setting fragment of special class as top with args */
    public Fragment setFragment(Class fragmentClass, Bundle args) {
        return addFragmentToStack(fragmentClass, args, fragmentClass.getName() + ' ' + TOP_FRAGMENT_TAG_MARK);
    }

    /* Pushing fragment of special class to fragments stack */
    public Fragment pushFragment(Class fragmentClass) {
        return pushFragment(fragmentClass, null);
    }

    /* Pushing fragment of special class with args to fragments stack */
    public Fragment pushFragment(Class fragmentClass, Bundle args) {
        return addFragmentToStack(fragmentClass, args, fragmentClass.getName());
    }

    /* Raises when device back button pressed */
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        boolean backPressResult = false;
        if (fragmentManager.getFragments() != null) {
            for (Fragment fragment : fragmentManager.getFragments()) {
                if (fragment != null && fragment.isResumed() && fragment instanceof AbstractBaseFragment) {
                    backPressResult = backPressResult || ((AbstractBaseFragment) fragment).onBackPressed();
                }
            }
        }

        if (!backPressResult) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fragmentManager = getSupportFragmentManager();

                if (tryHomeOnChildren(fragmentManager)) {
                    return true;
                }

                int stackSize = fragmentManager.getBackStackEntryCount();

                switch (stackSize) {
                    case 0:
                        return false;
                    case 1:
                        fragmentManager.popBackStack();
                        return true;
                    default:
                        String lastFragmentName = fragmentManager.getBackStackEntryAt(stackSize - 1).getName();
                        for (int i = stackSize - 2; i >= 0; i--) {
                            String currentFragmentName = fragmentManager.getBackStackEntryAt(i).getName();
                            if (currentFragmentName == null || !currentFragmentName.equals(lastFragmentName)) {
                                fragmentManager.popBackStackImmediate(currentFragmentName, 0);
                                break;
                            } else if (i == 0) {
                                fragmentManager.popBackStackImmediate(currentFragmentName, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            } else {
                                lastFragmentName = currentFragmentName;
                            }
                        }
                        return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean tryHomeOnChildren(@NonNull FragmentManager fragmentManager) {
        boolean homePressResult = false;
        if (fragmentManager.getFragments() != null) {
            for (Fragment fragment : fragmentManager.getFragments()) {
                if (fragment != null
                        && fragment.isResumed()
                        && fragment instanceof AbstractBaseFragment) {
                    homePressResult = homePressResult || ((AbstractBaseFragment) fragment).onHomePressed();
                }
            }
        }
        return homePressResult;
    }

    /* Hides device keyboard */
    public void hideSoftInput() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            View mainFragmentContainer = findViewById(getFragmentContainerId());
            if (mainFragmentContainer != null) {
                mainFragmentContainer.requestFocus();
            }
        }
    }

    /* Shows device keyboard */
    public void showSoftInput(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

}
