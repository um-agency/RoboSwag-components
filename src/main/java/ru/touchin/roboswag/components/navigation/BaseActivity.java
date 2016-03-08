package ru.touchin.roboswag.components.navigation;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Gavriil Sitnikov on 08/03/2016.
 * TODO: fill description
 */
public class BaseActivity extends AppCompatActivity {

    /**
     * Hides device keyboard that is showing over {@link Activity}.
     * Do not use it if keyboard is over {@link android.app.Dialog} - it won't work as they have different {@link Activity#getWindow()}.
     */
    public void hideSoftInput() {
        if (getCurrentFocus() == null) {
            return;
        }
        final InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        getWindow().getDecorView().requestFocus();
    }

    /**
     * Shows device keyboard over {@link Activity} and focuses {@link View}.
     * Do not use it if keyboard is over {@link android.app.Dialog} - it won't work as they have different {@link Activity#getWindow()}.
     * @param view View to get focus for input from keyboard.
     */
    public void showSoftInput(@NonNull final View view) {
        view.requestFocus();
        final InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

}
