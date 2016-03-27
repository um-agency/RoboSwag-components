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
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;

/**
 * Created by Gavriil Sitnikov on 11/03/16.
 * TODO: descriptions
 */
public class SidebarController implements FragmentManager.OnBackStackChangedListener, BaseActivity.OnBackPressedListener {

    @NonNull
    private final DrawerLayout drawerLayout;
    @NonNull
    private final ActionBarDrawerToggleImpl drawerToggle;
    @NonNull
    private final View sidebar;

    private boolean isHamburgerShowed;
    private boolean isSidebarDisabled;
    @Nullable
    private ValueAnimator hamburgerAnimator;

    public SidebarController(@NonNull final BaseActivity activity,
                             @NonNull final DrawerLayout drawerLayout,
                             @NonNull final View sidebar) {
        this.drawerLayout = drawerLayout;
        this.sidebar = sidebar;
        drawerToggle = new ActionBarDrawerToggleImpl(activity, drawerLayout);

        drawerLayout.addDrawerListener(drawerToggle);
        activity.getSupportFragmentManager().addOnBackStackChangedListener(this);
        activity.addOnBackPressedListener(this);
    }

    private boolean shouldShowHamburger() {
        return !isHamburgerShowed && !isSidebarDisabled;
    }

    public void onPostCreate(@Nullable final Bundle savedInstanceState) {
        drawerToggle.syncState();
    }

    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        return shouldShowHamburger() && drawerToggle.onOptionsItemSelected(item);
    }

    private void update() {
        final boolean showHamburger = shouldShowHamburger();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            drawerToggle.setDrawerIndicatorEnabled(true);
            if (hamburgerAnimator != null) {
                hamburgerAnimator.cancel();
            }
            if (showHamburger) {
                hamburgerAnimator = ValueAnimator.ofFloat(drawerToggle.slideOffset, 0f);
            } else {
                hamburgerAnimator = ValueAnimator.ofFloat(drawerToggle.slideOffset, 1f);
            }
            hamburgerAnimator.addUpdateListener(animation -> drawerToggle.onDrawerSlide(drawerLayout, (Float) animation.getAnimatedValue()));
            hamburgerAnimator.start();
        } else {
            drawerToggle.onDrawerSlide(drawerLayout, showHamburger ? 0f : 1f);
        }
        drawerToggle.slidePosition = showHamburger ? 0f : 1f;
        drawerLayout.setDrawerLockMode(isSidebarDisabled ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void disableSidebar() {
        isSidebarDisabled = true;
        closeSidebar();
        update();
    }

    public void enableSidebar() {
        isSidebarDisabled = false;
        update();
    }

    public void hideHamburger() {
        isHamburgerShowed = true;
        update();
    }

    public void showHamburger() {
        isHamburgerShowed = false;
        update();
    }

    public void closeSidebar() {
        if (drawerLayout.isDrawerOpen(sidebar)) {
            drawerLayout.closeDrawer(sidebar);
        }
    }

    @Override
    public void onBackStackChanged() {
        closeSidebar();
    }

    @Override
    public boolean onBackPressed() {
        if (drawerLayout.isDrawerOpen(sidebar)) {
            closeSidebar();
            return true;
        }
        return false;
    }

    private static class ActionBarDrawerToggleImpl extends ActionBarDrawerToggle {

        private final BaseActivity activity;
        private float slideOffset;
        private float slidePosition;

        public ActionBarDrawerToggleImpl(final BaseActivity activity, final DrawerLayout drawerLayout) {
            super(activity, drawerLayout, 0, 0);
            this.activity = activity;
        }

        @Override
        public void onDrawerClosed(final View view) {
            activity.supportInvalidateOptionsMenu();
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
}