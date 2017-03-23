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

package ru.touchin.roboswag.components.adapters;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ru.touchin.roboswag.components.utils.LifecycleBindable;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Gavriil Sitnikov on 12/8/2016.
 * ViewHolder that implements {@link LifecycleBindable} and uses parent bindable object as bridge (Activity, ViewController etc.).
 */
public class BindableViewHolder extends RecyclerView.ViewHolder implements LifecycleBindable {

    @NonNull
    private final LifecycleBindable baseLifecycleBindable;

    public BindableViewHolder(@NonNull final LifecycleBindable baseLifecycleBindable, @NonNull final View itemView) {
        super(itemView);
        this.baseLifecycleBindable = baseLifecycleBindable;
    }

    /**
     * Look for a child view with the given id.  If this view has the given id, return this view.
     *
     * @param id The id to search for;
     * @return The view that has the given id in the hierarchy.
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public <T extends View> T findViewById(@IdRes final int id) {
        final T viewById = (T) itemView.findViewById(id);
        if (viewById == null) {
            throw new ShouldNotHappenException("No view for id=" + itemView.getResources().getResourceName(id));
        }
        return viewById;
    }

    /**
     * Return the string value associated with a particular resource ID.  It
     * will be stripped of any styled text information.
     *
     * @param resId The resource id to search for data;
     * @return String The string data associated with the resource.
     */
    @NonNull
    public String getString(@StringRes final int resId) {
        return itemView.getResources().getString(resId);
    }

    /**
     * Return the string value associated with a particular resource ID.  It
     * will be stripped of any styled text information.
     *
     * @param resId      The resource id to search for data;
     * @param formatArgs The format arguments that will be used for substitution.
     * @return String The string data associated with the resource.
     */
    @NonNull
    public String getString(@StringRes final int resId, @Nullable final Object... formatArgs) {
        return itemView.getResources().getString(resId, formatArgs);
    }

    /**
     * Return the color value associated with a particular resource ID.
     * Starting in {@link android.os.Build.VERSION_CODES#M}, the returned
     * color will be styled for the specified Context's theme.
     *
     * @param resId The resource id to search for data;
     * @return int A single color value in the form 0xAARRGGBB.
     */
    @ColorInt
    public int getColor(@ColorRes final int resId) {
        return ContextCompat.getColor(itemView.getContext(), resId);
    }

    /**
     * Returns a drawable object associated with a particular resource ID.
     * Starting in {@link android.os.Build.VERSION_CODES#LOLLIPOP}, the
     * returned drawable will be styled for the specified Context's theme.
     *
     * @param resId The resource id to search for data;
     * @return Drawable An object that can be used to draw this resource.
     */
    @NonNull
    public Drawable getDrawable(@DrawableRes final int resId) {
        return ContextCompat.getDrawable(itemView.getContext(), resId);
    }

    @NonNull
    @Override
    public <T> Subscription bind(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        return baseLifecycleBindable.bind(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable) {
        return baseLifecycleBindable.untilStop(observable);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        return baseLifecycleBindable.untilStop(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable,
                                      @NonNull final Action1<T> onNextAction,
                                      @NonNull final Action1<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilStop(observable, onNextAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable,
                                      @NonNull final Action1<T> onNextAction,
                                      @NonNull final Action1<Throwable> onErrorAction,
                                      @NonNull final Action0 onCompletedAction) {
        return baseLifecycleBindable.untilStop(observable, onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable) {
        return baseLifecycleBindable.untilDestroy(observable);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        return baseLifecycleBindable.untilDestroy(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilDestroy(observable, onNextAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction,
                                         @NonNull final Action0 onCompletedAction) {
        return baseLifecycleBindable.untilDestroy(observable, onNextAction, onErrorAction, onCompletedAction);
    }

}
