package org.roboswag.components.utils;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

    @NonNull
    public static View inflate(@LayoutRes final int layoutId, @NonNull final ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
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

    private UiUtils() {
    }

}
