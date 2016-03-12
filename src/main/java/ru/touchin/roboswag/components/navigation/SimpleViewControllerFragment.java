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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by Gavriil Sitnikov on 07/03/2016.
 * TODO: fill description
 */
public class SimpleViewControllerFragment<TState extends Serializable, TLogicBridge, TActivity extends ViewControllerActivity<TLogicBridge>>
        extends ViewControllerFragment<TState, TLogicBridge, TActivity> {

    private static final String VIEW_CONTROLLER_CLASS_EXTRA = "VIEW_CONTROLLER_CLASS_EXTRA";

    /**
     * Creates {@link Bundle} which will store state and {@link ViewController}'s class.
     *
     * @param viewControllerClass Class of {@link ViewController} which will be instantiated inside this fragment.
     * @param state               State to use into {@link ViewController}.
     * @return Returns {@link Bundle} with state inside.
     */
    @NonNull
    public static Bundle createState(@NonNull Class<? extends ViewController> viewControllerClass,
                                     @Nullable final Serializable state) {
        final Bundle result = ViewControllerFragment.createState(state);
        result.putSerializable(VIEW_CONTROLLER_CLASS_EXTRA, viewControllerClass);
        return result;
    }

    private Class<? extends ViewController<TLogicBridge, TActivity,
            ? extends ViewControllerFragment<TState, TLogicBridge, TActivity>>> viewControllerClass;

    @NonNull
    @Override
    public Class<? extends ViewController<TLogicBridge, TActivity,
            ? extends ViewControllerFragment<TState, TLogicBridge, TActivity>>> getViewControllerClass() {
        return viewControllerClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewControllerClass = (Class<? extends ViewController<TLogicBridge, TActivity,
                ? extends ViewControllerFragment<TState, TLogicBridge, TActivity>>>) getArguments().getSerializable(VIEW_CONTROLLER_CLASS_EXTRA);
    }

}
