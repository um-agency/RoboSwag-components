package ru.touchin.roboswag.components.navigation;

import android.support.annotation.NonNull;

import ru.touchin.roboswag.components.utils.Logic;

/**
 * Created by Gavriil Sitnikov on 07/03/2016.
 * TODO: fill description
 */
public abstract class ViewControllerActivity<TLogic extends Logic> extends BaseActivity {

    /**
     * It should return specific class where from all logic will be.
     *
     * @return Returns class of specific Logic.
     */
    @NonNull
    protected abstract Class<TLogic> getLogicClass();

    /**
     * Returns or creates application's logic.
     *
     * @return Object which represents application's logic.
     */
    public TLogic getLogic() {
        return Logic.getInstance(this, getLogicClass());
    }

}
