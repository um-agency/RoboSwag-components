package ru.touchin.roboswag.components.navigation;

import android.support.annotation.NonNull;

import ru.touchin.roboswag.components.services.LogicService;
import ru.touchin.roboswag.core.utils.android.RxAndroidUtils;
import rx.Observable;

/**
 * Created by Gavriil Sitnikov on 07/03/2016.
 * TODO: fill description
 */
public abstract class ViewControllerActivity<TLogicBridge> extends BaseActivity {

    /**
     * It should return specific Service class where from this fragment should get interface to logic.
     *
     * @return Returns class of specific LogicService.
     */
    @NonNull
    protected abstract Class<? extends LogicService<TLogicBridge>> getLogicServiceClass();

    /**
     * Returns {@link Observable} which will connect to {@link LogicService} and get object of {@link TLogicBridge} type from it.
     *
     * @return {@link Observable} which will provide changes of object of type {@link TLogicBridge};
     */
    @NonNull
    public Observable<TLogicBridge> observeLogicBridge() {
        return RxAndroidUtils.observeService(this, getLogicServiceClass())
                .map(service -> service != null ? service.getLogicBridge() : null);
    }

}
