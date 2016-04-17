package ru.touchin.roboswag.components.navigation.activities;

import android.support.annotation.NonNull;
import android.view.Menu;

import ru.touchin.roboswag.components.utils.Logic;

/**
 * Created by Gavriil Sitnikov on 07/03/2016.
 * TODO: fill description
 */
public abstract class ViewControllerActivity<TLogic extends Logic> extends BaseActivity {

    //it is needed to hold strong reference to logic
    private TLogic reference;

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
        synchronized (ViewControllerActivity.class) {
            if (reference == null) {
                reference = Logic.getInstance(this, getLogicClass());
            }
        }
        return reference;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        onConfigureNavigation(menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Calls when activity configuring ActionBar, Toolbar, Sidebar etc. Before internal ViewControllers.
     *
     * @param menu The options menu in which you place your items;
     */
    public void onConfigureNavigation(@NonNull final Menu menu) {
        // do nothing
    }

}
