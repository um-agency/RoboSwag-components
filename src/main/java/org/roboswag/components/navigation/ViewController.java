package org.roboswag.components.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * Class to control view of specific fragment, activity and application by logic bridge.
 */
public class ViewController<TLogicBridge,
        TActivity extends AppCompatActivity,
        TFragment extends ViewControllerFragment<TLogicBridge, TActivity>> {

    @NonNull
    private final TLogicBridge logicBridge;
    @NonNull
    private final TActivity activity;
    @NonNull
    private final TFragment fragment;
    @NonNull
    private final View view;

    public ViewController(@NonNull final CreationContext<TLogicBridge, TActivity, TFragment> creationContext,
                          @Nullable final Bundle savedInstanceState) {
        this.logicBridge = creationContext.logicBridge;
        this.activity = creationContext.activity;
        this.fragment = creationContext.fragment;
        this.view = creationContext.view;
    }

    /**
     * Returns logic bridge to use and affect application logic.
     *
     * @return Returns logic bridge object.
     */
    @NonNull
    public TLogicBridge getLogicBridge() {
        return logicBridge;
    }

    /**
     * Returns view's activity.
     *
     * @return Returns activity;
     */
    @NonNull
    public TActivity getActivity() {
        return activity;
    }

    /**
     * Returns view's activity.
     *
     * @return Returns activity;
     */
    @NonNull
    public TFragment getFragment() {
        return fragment;
    }

    /**
     * Returns view instantiated in {@link #getFragment} fragment attached to {@link #getActivity} activity.
     *
     * @return Returns view;
     */
    @NonNull
    public View getView() {
        return view;
    }

    public void onDestroy() {
        //do nothing
    }

    /**
     * Class to simplify constructor override.
     */
    public static class CreationContext<TLogicBridge,
            TActivity extends AppCompatActivity,
            TFragment extends ViewControllerFragment<TLogicBridge, TActivity>> {

        @NonNull
        private final TLogicBridge logicBridge;
        @NonNull
        private final TActivity activity;
        @NonNull
        private final TFragment fragment;
        @NonNull
        private final View view;

        public CreationContext(@NonNull final TLogicBridge logicBridge,
                               @NonNull final TActivity activity,
                               @NonNull final TFragment fragment,
                               @NonNull final View view) {
            this.logicBridge = logicBridge;
            this.activity = activity;
            this.fragment = fragment;
            this.view = view;
        }

    }

}