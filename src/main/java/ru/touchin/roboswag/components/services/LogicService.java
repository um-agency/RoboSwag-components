package ru.touchin.roboswag.components.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

import ru.touchin.roboswag.core.utils.android.ServiceBinder;

/**
 * Created by Gavriil Sitnikov on 10/01/2016.
 * Service which holds interface to all application's logic objects and methods.
 * Any part of application should interact with some part of logic via this interface.
 * If it is Service, Activity, Fragment or iew then it should bind itself to that service first then get service and logic bridge from IBinder.
 * If it is BroadcastReceiver then it should start service which can bind to that service or just send some intent to that service.
 */
public abstract class LogicService<TLogicBridge> extends IntentService {

    private final TLogicBridge logicBridge;

    protected LogicService(final String name) {
        super(name);
        this.logicBridge = createLogicBridge();
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
