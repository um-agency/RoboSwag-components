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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import ru.touchin.roboswag.core.log.Lc;
import rx.functions.Func1;

/**
 * Created by Gavriil Sitnikov on 07/03/2016.
 * Navigation which is controlling using {@link android.support.v4.app.FragmentManager} as controller.
 */
public class FragmentNavigation {

    protected static final String TOP_FRAGMENT_TAG_MARK = "TOP_FRAGMENT";
    protected static final String WITH_TARGET_FRAGMENT_TAG_MARK = "FRAGMENT_WITH_TARGET";

    @NonNull
    private final Context context;
    @NonNull
    private final FragmentManager fragmentManager;
    @IdRes
    private final int containerViewId;

    public FragmentNavigation(@NonNull final Context context, @NonNull final FragmentManager fragmentManager, @IdRes final int containerViewId) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.containerViewId = containerViewId;
    }

    @NonNull
    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    /**
     * Returns if last fragment in stack is top (added by setFragment) like fragment from sidebar menu.
     *
     * @return True if last fragment on stack has TOP_FRAGMENT_TAG_MARK.
     */
    public boolean isCurrentFragmentTop() {
        if (fragmentManager.getBackStackEntryCount() == 0) {
            return true;
        }

        final String topFragmentTag = fragmentManager
                .getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1)
                .getName();
        return topFragmentTag != null && topFragmentTag.contains(TOP_FRAGMENT_TAG_MARK);
    }

    @SuppressLint("CommitTransaction")
    protected void addToStack(@NonNull final Class<? extends Fragment> fragmentClass,
                              @Nullable final Fragment targetFragment,
                              @Nullable final Bundle args,
                              @Nullable final String backStackTag,
                              @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        if (fragmentManager.isDestroyed()) {
            Lc.assertion("FragmentManager is destroyed");
            return;
        }

        final Fragment fragment = Fragment.instantiate(context, fragmentClass.getName(), args);
        if (targetFragment != null) {
            fragment.setTargetFragment(targetFragment, 0);
        }

        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .replace(containerViewId, fragment, null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(backStackTag);
        if (transactionSetup != null) {
            transactionSetup.call(fragmentTransaction).commit();
        } else {
            fragmentTransaction.commit();
        }
    }

    public boolean back() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            return true;
        }
        return false;
    }

    public boolean backTo(@NonNull final Func1<FragmentManager.BackStackEntry, Boolean> condition) {
        final int stackSize = fragmentManager.getBackStackEntryCount();
        Integer id = null;
        for (int i = stackSize - 2; i >= 0; i--) {
            final FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(i);
            id = backStackEntry.getId();
            if (condition.call(backStackEntry)) {
                break;
            }
        }
        if (id != null) {
            fragmentManager.popBackStackImmediate(id, 0);
            return true;
        }
        return false;
    }

    public boolean up() {
        return backTo(backStackEntry -> backStackEntry.getName().endsWith(TOP_FRAGMENT_TAG_MARK));
    }

    public void push(@NonNull final Class<? extends Fragment> fragmentClass) {
        addToStack(fragmentClass, null, null, null, null);
    }

    public void push(@NonNull final Class<? extends Fragment> fragmentClass,
                     @NonNull final Bundle args) {
        addToStack(fragmentClass, null, args, null, null);
    }

    public void push(@NonNull final Class<? extends Fragment> fragmentClass,
                     @NonNull final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, null, null, null, transactionSetup);
    }

    public void push(@NonNull final Class<? extends Fragment> fragmentClass,
                     @Nullable final Bundle args,
                     @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, null, args, null, transactionSetup);
    }

    public void pushForResult(@NonNull final Class<? extends Fragment> fragmentClass,
                              @NonNull final Fragment targetFragment) {
        addToStack(fragmentClass, targetFragment, null, fragmentClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, null);
    }

    public void pushForResult(@NonNull final Class<? extends Fragment> fragmentClass,
                              @NonNull final Fragment targetFragment,
                              @NonNull final Bundle args) {
        addToStack(fragmentClass, targetFragment, args, fragmentClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, null);
    }

    public void pushForResult(@NonNull final Class<? extends Fragment> fragmentClass,
                              @NonNull final Fragment targetFragment,
                              @NonNull final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, targetFragment, null, fragmentClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, transactionSetup);
    }

    public void pushForResult(@NonNull final Class<? extends Fragment> fragmentClass,
                              @NonNull final Fragment targetFragment,
                              @Nullable final Bundle args,
                              @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, targetFragment, args, fragmentClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, transactionSetup);
    }

    public void setAsTop(@NonNull final Class<? extends Fragment> fragmentClass) {
        addToStack(fragmentClass, null, null, fragmentClass.getName() + ';' + TOP_FRAGMENT_TAG_MARK, null);
    }

    public void setAsTop(@NonNull final Class<? extends Fragment> fragmentClass,
                         @NonNull final Bundle args) {
        addToStack(fragmentClass, null, args, fragmentClass.getName() + ';' + TOP_FRAGMENT_TAG_MARK, null);
    }

    public void setAsTop(@NonNull final Class<? extends Fragment> fragmentClass,
                         @NonNull final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, null, null, fragmentClass.getName() + ';' + TOP_FRAGMENT_TAG_MARK, transactionSetup);
    }

    public void setAsTop(@NonNull final Class<? extends Fragment> fragmentClass,
                         @Nullable final Bundle args,
                         @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, null, args, fragmentClass.getName() + ';' + TOP_FRAGMENT_TAG_MARK, transactionSetup);
    }

    public void setInitial(@NonNull final Class<? extends Fragment> fragmentClass) {
        setInitial(fragmentClass, null, null);
    }

    public void setInitial(@NonNull final Class<? extends Fragment> fragmentClass,
                           @NonNull final Bundle args) {
        setInitial(fragmentClass, args, null);
    }

    public void setInitial(@NonNull final Class<? extends Fragment> fragmentClass,
                           @NonNull final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        setInitial(fragmentClass, null, transactionSetup);
    }

    public void setInitial(@NonNull final Class<? extends Fragment> fragmentClass,
                           @Nullable final Bundle args,
                           @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        beforeSetInitialActions();
        setAsTop(fragmentClass, args, transactionSetup);
    }

    protected void beforeSetInitialActions() {
        if (fragmentManager.isDestroyed()) {
            Lc.assertion("FragmentManager is destroyed");
            return;
        }

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

}
