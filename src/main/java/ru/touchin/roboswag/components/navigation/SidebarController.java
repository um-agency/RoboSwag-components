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

import android.content.res.Configuration;
import android.support.annotation.NonNull;
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

    private final DrawerLayout drawerLayout;
    private final ActionBarDrawerToggle drawerToggle;
    private final View sidebar;

    private boolean isHamburgerShowed;
    private boolean isSidebarDisabled;

    public SidebarController(@NonNull final BaseActivity activity,
                             @NonNull final DrawerLayout drawerLayout,
                             @NonNull final View sidebar) {
        this.drawerLayout = drawerLayout;
        this.sidebar = sidebar;
        drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, 0, 0) {

            @Override
            public void onDrawerClosed(final View view) {
                activity.supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(final View drawerView) {
                activity.hideSoftInput();
                activity.supportInvalidateOptionsMenu();
            }

        };
        drawerLayout.addDrawerListener(drawerToggle);
        activity.getSupportFragmentManager().addOnBackStackChangedListener(this);
        activity.addOnBackPressedListener(this);
    }

    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item);
    }

    private void update() {
        final boolean showHamburger = !isHamburgerShowed && !isSidebarDisabled;
        drawerToggle.setDrawerIndicatorEnabled(!showHamburger);
        drawerToggle.setDrawerIndicatorEnabled(showHamburger);
        drawerToggle.syncState();
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

}