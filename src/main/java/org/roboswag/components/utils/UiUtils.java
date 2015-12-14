package org.roboswag.components.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.roboswag.components.navigation.AbstractBaseFragment;

import rx.functions.Func1;

/**
 * Created by Gavriil Sitnikov on 13/11/2015.
 * TODO: fill description
 */
public final class UiUtils {

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

    @NonNull
    public static View inflate(@LayoutRes final int layoutId, @NonNull final ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }

    @NonNull
    public static View inflateAndAdd(@LayoutRes final int layoutId, @NonNull final ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, true);
    }

    public static boolean tryForeachFragment(@NonNull final FragmentManager fragmentManager,
                                             @NonNull final Func1<AbstractBaseFragment, Boolean> actionOnChild) {
        if (fragmentManager.getFragments() == null) {
            return false;
        }

        boolean result = false;
        for (final Fragment fragment : fragmentManager.getFragments()) {
            if (fragment != null
                    && fragment.isResumed()
                    && fragment instanceof AbstractBaseFragment) {
                result = result || actionOnChild.call((AbstractBaseFragment) fragment);
            }
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    public static int getColor(@NonNull final Context context, @ColorRes final int colorResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(colorResId, context.getTheme());
        } else {
            return context.getResources().getColor(colorResId);
        }
    }

    @SuppressWarnings("deprecation")
    public static Drawable getDrawable(@NonNull final Context context, @DrawableRes final int drawableResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getDrawable(drawableResId, context.getTheme());
        } else {
            return context.getResources().getDrawable(drawableResId);
        }
    }

    private UiUtils() {
    }

}
