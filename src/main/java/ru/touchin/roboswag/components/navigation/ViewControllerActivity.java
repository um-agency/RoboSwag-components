package ru.touchin.roboswag.components.navigation;

import android.support.annotation.NonNull;
import android.view.Menu;

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
