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

package ru.touchin.roboswag.components.navigation.activities;

import android.support.annotation.NonNull;
import android.view.Menu;

import ru.touchin.roboswag.components.utils.Logic;

/**
 * Created by Gavriil Sitnikov on 07/03/2016.
 * Activity which is containing specific {@link Logic}
 * to support navigation based on {@link ru.touchin.roboswag.components.navigation.ViewController}s.
 *
 * @param <TLogic> Type of application's {@link Logic}.
 */
public abstract class ViewControllerActivity<TLogic extends Logic> extends BaseActivity {

    //it is needed to hold strong reference to logic
    private TLogic reference;

    /**
     * It should return specific class where all logic will be.
     *
     * @return Returns class of specific {@link Logic}.
     */
    @NonNull
    protected abstract Class<TLogic> getLogicClass();

    /**
     * Returns (and creates if needed) application's logic.
     *
     * @return Object which represents application's logic.
     */
    public TLogic getLogic() {
        synchronized (ViewControllerActivity.class) {
            if (reference == null) {
                reference = Logic.getInstance(this, getLogicClass());
            }
        }
        return reference;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        onConfigureNavigation(menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Calls when activity configuring ActionBar, Toolbar, Sidebar, AppBar etc.
     * It is calling before it's {@link ru.touchin.roboswag.components.navigation.ViewController}'s.
     *
     * @param menu The options menu in which you place your menu items.
     */
    public void onConfigureNavigation(@NonNull final Menu menu) {
        // do nothing
    }

}
