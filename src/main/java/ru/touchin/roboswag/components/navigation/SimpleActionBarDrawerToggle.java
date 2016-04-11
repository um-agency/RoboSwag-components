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

import android.animation.ValueAnimator;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;

import ru.touchin.roboswag.components.navigation.activities.BaseActivity;

/**
 * Created by Gavriil Sitnikov on 11/03/16.
 * TODO: descriptions
 */
public class SimpleActionBarDrawerToggle extends ActionBarDrawerToggle
        implements FragmentManager.OnBackStackChangedListener, BaseActivity.OnBackPressedListener {

    @NonNull
    private final BaseActivity activity;
    @NonNull
    private final DrawerLayout drawerLayout;
    @NonNull
    private final View sidebar;

    private boolean isHamburgerShowed;
    private boolean isSidebarDisabled;

    private float slideOffset;
    private float slidePosition;

    @Nullable
    private ValueAnimator hamburgerAnimator;
    private boolean firstAnimation = true;

    public SimpleActionBarDrawerToggle(@NonNull final BaseActivity activity,
                                       @NonNull final DrawerLayout drawerLayout,
                                       @NonNull final View sidebar) {
        super(activity, drawerLayout, 0, 0);
        this.activity = activity;
        this.drawerLayout = drawerLayout;
        this.sidebar = sidebar;

        drawerLayout.addDrawerListener(this);
        activity.getSupportFragmentManager().addOnBackStackChangedListener(this);
        activity.addOnBackPressedListener(this);
    }

    private boolean shouldShowHamburger() {
        return !isHamburgerShowed && !isSidebarDisabled;
    }

    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        return shouldShowHamburger() && super.onOptionsItemSelected(item);
    }

    private void update() {
        setHamburgerState(shouldShowHamburger());
        drawerLayout.setDrawerLockMode(isSidebarDisabled ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void disableSidebar() {
        isSidebarDisabled = true;
        close();
        update();
    }

    public void enableSidebar() {
        isSidebarDisabled = false;
        update();
    }

    public void hideHamburger() {
        syncState();
        isHamburgerShowed = true;
        update();
    }

    public void showHamburger() {
        syncState();
        isHamburgerShowed = false;
        update();
    }

    public void open() {
        if (!drawerLayout.isDrawerOpen(sidebar)) {
            drawerLayout.openDrawer(sidebar);
        }
    }

    public void close() {
        if (drawerLayout.isDrawerOpen(sidebar)) {
            drawerLayout.closeDrawer(sidebar);
        }
    }

    @Override
    public void onBackStackChanged() {
        close();
    }

    @Override
    public boolean onBackPressed() {
        if (drawerLayout.isDrawerOpen(sidebar)) {
            close();
            return true;
        }
        return false;
    }

    private void setHamburgerState(final boolean showHamburger) {
        if (!firstAnimation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            cancelAnimation();
            if (showHamburger) {
                hamburgerAnimator = ValueAnimator.ofFloat(slideOffset, 0f);
            } else {
                hamburgerAnimator = ValueAnimator.ofFloat(slideOffset, 1f);
            }
            hamburgerAnimator.addUpdateListener(animation -> onDrawerSlide(drawerLayout, (Float) animation.getAnimatedValue()));
            hamburgerAnimator.start();
        } else {
            slideOffset = showHamburger ? 0f : 1f;
            onDrawerSlide(drawerLayout, slideOffset);
        }
        slidePosition = showHamburger ? 0f : 1f;
        firstAnimation = false;
    }

    private void cancelAnimation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return;
        }
        if (hamburgerAnimator != null) {
            hamburgerAnimator.cancel();
        }
    }

    @Override
    public void onDrawerClosed(final View view) {
        activity.supportInvalidateOptionsMenu();
    }

    @Override
    public void syncState() {
        cancelAnimation();
        super.syncState();
    }

    @Override
    public void onDrawerOpened(final View drawerView) {
        activity.hideSoftInput();
        activity.supportInvalidateOptionsMenu();
    }

    @Override
    public void onDrawerSlide(final View drawerView, final float slideOffset) {
        if (slideOffset >= this.slideOffset && slideOffset <= this.slidePosition
                || slideOffset <= this.slideOffset && slideOffset >= this.slidePosition) {
            this.slideOffset = slideOffset;
        }
        super.onDrawerSlide(drawerView, this.slideOffset);
    }

}