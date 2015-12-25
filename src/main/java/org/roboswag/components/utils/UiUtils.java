package org.roboswag.components.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import org.roboswag.components.navigation.AbstractBaseFragment;

import rx.functions.Func1;

/**
 * Created by Gavriil Sitnikov on 13/11/2015.
 * TODO: fill description
 */
public final class UiUtils {

    // to enable ripple effect on tap
    public static final long RIPPLE_EFFECT_DELAY = 250;
    private static final int MAX_METRICS_TRIES_COUNT = 5;

    @NonNull
    public static DisplayMetrics getDisplayMetrics(@NonNull final Context context) {
        DisplayMetrics result = context.getResources().getDisplayMetrics();
        // it is needed to avoid bug with invalid metrics when user restore application from other application
        int metricsTryNumber = 0;
        while (metricsTryNumber < MAX_METRICS_TRIES_COUNT && (result.heightPixels <= 0 || result.widthPixels <= 0)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return result;
            }
            result = context.getResources().getDisplayMetrics();
            metricsTryNumber++;
        }
        return result;
    }

    public static int getActionBarHeight(@NonNull final Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getDisplayMetrics(context));
    }

    public static int getStatusBarHeight(@NonNull final Context context) {
        final int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? context.getResources().getDimensionPixelSize(resourceId) : 0;
    }

    public static int getNavigationBarHeight(@NonNull final Context context) {
        final int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return resourceId > 0 ? context.getResources().getDimensionPixelSize(resourceId) : 0;
    }

    @NonNull
    public static View inflate(@LayoutRes final int layoutId, @NonNull final ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }

    @NonNull
    public static View inflateAndAdd(@LayoutRes final int layoutId, @NonNull final ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, true);
    }

    public static boolean tryForeachFragment(@NonNull final FragmentManager fragmentManager,
                                             @NonNull final Func1<AbstractBaseFragment, Boolean> actionOnChild,
                                             final boolean onlyForResumed) {
        if (fragmentManager.getFragments() == null) {
            return false;
        }

        boolean result = false;
        for (final Fragment fragment : fragmentManager.getFragments()) {
            if (fragment != null
                    && (!onlyForResumed || fragment.isResumed())
                    && fragment instanceof AbstractBaseFragment) {
                result = result || actionOnChild.call((AbstractBaseFragment) fragment);
            }
        }
        return result;
    }

    public static boolean isIntentAbleToHandle(@NonNull final Context context, @NonNull final Intent intent) {
        return !context.getPackageManager().queryIntentActivities(intent, 0).isEmpty();
    }

    //http://stackoverflow.com/questions/14853039/how-to-tell-whether-an-android-device-has-hard-keys/14871974#14871974
    public static boolean hasSoftKeys(@NonNull final Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final Display display = activity.getWindowManager().getDefaultDisplay();

            final DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            display.getRealMetrics(realDisplayMetrics);

            final DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);

            return (realDisplayMetrics.widthPixels - displayMetrics.widthPixels) > 0
                    || (realDisplayMetrics.heightPixels - displayMetrics.heightPixels) > 0;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            final boolean hasMenuKey = ViewConfiguration.get(activity).hasPermanentMenuKey();
            final boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            return !hasMenuKey && !hasBackKey;
        }
        return false;
    }

    private UiUtils() {
    }

}
