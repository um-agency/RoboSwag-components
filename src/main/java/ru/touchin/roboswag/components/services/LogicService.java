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

package ru.touchin.roboswag.components.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

import ru.touchin.roboswag.core.utils.android.ServiceBinder;

/**
 * Created by Gavriil Sitnikov on 10/01/2016.
 * Service which holds interface to all application's logic objects and methods.
 * Any part of application should interact with some part of logic via this interface.
 * If it is Service, Activity, Fragment or view then it should bind itself to that service first then get service and logic bridge from IBinder.
 * If it is BroadcastReceiver then it should start service which can bind to that service or just send some intent to that service.
 * [phase 1]
 */
public abstract class LogicService<TLogicBridge> extends IntentService {

    private final TLogicBridge logicBridge;

    protected LogicService() {
        super("LogicService");
        this.logicBridge = createLogicBridge();
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        // do nothing
    }

    /**
     * Creates object which will provide all logic methods and objects of application.
     * Any other activity, fragment or service should bind to that service to get logic bridge and start interact with application's logic.
     * Do not initialize massive objects during creation as this operation is calling on UI thread.
     *
     * @return Returns instantiated logic bridge.
     */
    protected abstract TLogicBridge createLogicBridge();

    /**
     * Returns interface to application's logic.
     *
     * @return Returns logic bridge object.
     */
    public TLogicBridge getLogicBridge() {
        return logicBridge;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return new ServiceBinder<>(this);
    }

}
