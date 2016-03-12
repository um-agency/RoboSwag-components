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

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.HashMap;
import java.util.Map;

import ru.touchin.roboswag.components.utils.PermissionState;
import ru.touchin.roboswag.components.utils.UiUtils;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * TODO: fill description
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods"})
public abstract class AbstractBaseActivity extends BaseActivity
        implements FragmentManager.OnBackStackChangedListener,
        OnFragmentStartedListener {

    private static final String TOP_FRAGMENT_TAG_MARK = "TOP_FRAGMENT";

    private static final String REQUESTED_PERMISSION_EXTRA = "REQUESTED_PERMISSION_EXTRA";
    private static final int REQUESTED_PERMISSION_REQUEST_CODE = 17;

    private final Map<String, PermissionState> permissionsMap = new HashMap<>();

    private boolean isPaused;
    @Nullable
    private String requestedPermission;
    private final PublishSubject<PermissionState> requestPermissionsEvent = PublishSubject.create();

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

    // https://code.google.com/p/android/issues/detail?id=2373
    // https://github.com/cleverua/android_startup_activity
    // http://stackoverflow.com/questions/4341600/how-to-prevent-multiple-instances-of-an-activity-when-it-is-launched-with-differ
    @SuppressWarnings("deprecation")
    public boolean isLaunchedManyTimes() {
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (final ActivityManager.RunningTaskInfo taskInfo : activityManager.getRunningTasks(Integer.MAX_VALUE)) {
            if (getPackageName().equals(taskInfo.baseActivity.getPackageName())
                    && taskInfo.numActivities > 1
                    && Intent.ACTION_MAIN.equals(getIntent().getAction())) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    public Observable<PermissionState> requestPermission(@NonNull final String permission, final boolean usePreviousRequest) {
        final PermissionState permissionState = permissionsMap.get(permission);
        if (permissionState != null && ((permissionState == PermissionState.GRANTED) || usePreviousRequest)) {
            return Observable.just(permissionState);
        }
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            permissionsMap.put(permission, PermissionState.GRANTED);
            return Observable.just(PermissionState.GRANTED);
        }
        requestedPermission = permission;
        ActivityCompat.requestPermissions(this, new String[]{permission}, REQUESTED_PERMISSION_REQUEST_CODE);
        return requestPermissionsEvent.first();
    }

    @SuppressWarnings("PMD.UseVarargs")
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUESTED_PERMISSION_REQUEST_CODE) {
            final PermissionState permissionState;
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionState = PermissionState.GRANTED;
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                permissionState = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])
                        ? PermissionState.DENIED_THIS_TIME
                        : PermissionState.DENIED_COMPLETELY;
            } else {
                permissionState = PermissionState.DENIED_THIS_TIME;
            }
            permissionsMap.put(requestedPermission, permissionState);
            requestedPermission = null;
            requestPermissionsEvent.onNext(permissionState);
        }
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (savedInstanceState != null) {
            requestedPermission = savedInstanceState.getString(REQUESTED_PERMISSION_EXTRA);
        }
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
    public void onSaveInstanceState(final Bundle stateToSave) {
        super.onSaveInstanceState(stateToSave);
        stateToSave.putString(REQUESTED_PERMISSION_EXTRA, requestedPermission);
    }

    @Override
    public void onFragmentStarted(@NonNull final Fragment fragment) {
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

        return setFragment(fragmentClass, args);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T extends AbstractBaseFragment> T addFragmentToStack(@NonNull final Class<T> fragmentClass,
                                                                  @Nullable final Fragment targetFragment,
                                                                  @Nullable final Bundle args,
                                                                  @Nullable final String backStackTag) {
        if (isPaused) {
            //TODO: log
            return null;
        }

        final T fragment;
        try {
            fragment = (T) Fragment.instantiate(this, fragmentClass.getName(), args);
            if (targetFragment != null) {
                fragment.setTargetFragment(targetFragment, 0);
            }
        } catch (final Exception ex) {
            //TODO: log
            return null;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(getFragmentContainerId(), fragment, backStackTag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(backStackTag)
                .commit();

        return fragment;
    }

    /* Setting fragment of special class as top */
    public <T extends AbstractBaseFragment> T setFragment(@NonNull final Class<T> fragmentClass) {
        return setFragment(fragmentClass, null);
    }

    /* Setting fragment of special class as top with args */
    public <T extends AbstractBaseFragment> T setFragment(@NonNull final Class<T> fragmentClass,
                                                          @Nullable final Bundle args) {
        return addFragmentToStack(fragmentClass, null, args, fragmentClass.getName() + ' ' + TOP_FRAGMENT_TAG_MARK);
    }

    /* Pushing fragment of special class to fragments stack */
    public <T extends AbstractBaseFragment> T pushFragment(@NonNull final Class<T> fragmentClass) {
        return pushFragment(fragmentClass, null);
    }

    /* Pushing fragment of special class with args to fragments stack */
    public <T extends AbstractBaseFragment> T pushFragment(@NonNull final Class<T> fragmentClass,
                                                           @Nullable final Bundle args) {
        return addFragmentToStack(fragmentClass, null, args, fragmentClass.getName());
    }

    /* Pushing fragment of special class with args to fragments stack */
    public <T extends AbstractBaseFragment> T pushFragmentForResult(@NonNull final Class<T> fragmentClass,
                                                                    @NonNull final Fragment targetFragment) {
        return addFragmentToStack(fragmentClass, targetFragment, null, fragmentClass.getName());
    }

    /* Pushing fragment of special class with args to fragments stack */
    public <T extends AbstractBaseFragment> T pushFragmentForResult(@NonNull final Class<T> fragmentClass,
                                                                    @NonNull final Fragment targetFragment,
                                                                    @Nullable final Bundle args) {
        return addFragmentToStack(fragmentClass, targetFragment, args, fragmentClass.getName());
    }

    /* Raises when device back button pressed */
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    //TODO: wait for 1 to be ignored
    @Override
    public void onBackPressed() {
        if (!UiUtils.tryForeachFragment(getSupportFragmentManager(), fragment -> fragment.onBackPressed(this), true)) {
            if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
                supportFinishAfterTransition();
            } else {
                getSupportFragmentManager().popBackStackImmediate();
            }
        }
    }

    @Deprecated
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onActivityResultProcess(requestCode, resultCode, data);
    }

    public boolean onActivityResultProcess(final int requestCode, final int resultCode, final Intent data) {
        return requestCode == REQUESTED_PERMISSION_REQUEST_CODE
                || UiUtils.tryForeachFragment(getSupportFragmentManager(),
                fragment -> fragment.onActivityResultProcess(requestCode, resultCode, data),
                false);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() != android.R.id.home) {
            return super.onOptionsItemSelected(item);
        }

        final FragmentManager fragmentManager = getSupportFragmentManager();

        if (UiUtils.tryForeachFragment(fragmentManager, fragment -> fragment.onHomePressed(this), true)) {
            return true;
        }

        if (fragmentManager.getBackStackEntryCount() != 0) {
            popBackStackToTopFragment();
            return true;
        }
        return false;
    }

    public void popBackStackToTopFragment() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final int stackSize = fragmentManager.getBackStackEntryCount();
        String currentFragmentName = null;
        for (int i = stackSize - 2; i >= 0; i--) {
            currentFragmentName = fragmentManager.getBackStackEntryAt(i).getName();
            if (currentFragmentName.endsWith(TOP_FRAGMENT_TAG_MARK)) {
                break;
            }
        }
        fragmentManager.popBackStackImmediate(currentFragmentName, 0);
    }

    @Nullable
    public Typeface getToolbarTitleTypeface(@NonNull final AbstractBaseActivity activity) {
        return null;
    }

}
