package ru.touchin.roboswag.components.navigation.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

import ru.touchin.roboswag.components.navigation.BaseUiBindable;
import ru.touchin.roboswag.components.navigation.UiBindable;
import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 08/03/2016.
 * TODO: fill description
 */
public abstract class BaseActivity extends AppCompatActivity
        implements UiBindable {

    private final ArrayList<OnBackPressedListener> onBackPressedListeners = new ArrayList<>();
    @NonNull
    private final BehaviorSubject<Boolean> isCreatedSubject = BehaviorSubject.create();
    @NonNull
    private final BehaviorSubject<Boolean> isStartedSubject = BehaviorSubject.create();
    @NonNull
    private final BaseUiBindable baseUiBindable = new BaseUiBindable(isCreatedSubject, isStartedSubject);

    @Override
    public void onCreate(final Bundle savedInstanceState, final PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        isCreatedSubject.onNext(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isStartedSubject.onNext(true);
    }

    @Override
    protected void onStop() {
        isStartedSubject.onNext(false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        isCreatedSubject.onNext(false);
        super.onDestroy();
    }

    @NonNull
    @Override
    public <T> Observable<T> bind(@NonNull final Observable<T> observable) {
        return baseUiBindable.bind(observable);
    }

    @NonNull
    public <T> Observable<T> untilDestroy(@NonNull final Observable<T> observable) {
        return baseUiBindable.untilDestroy(observable);
    }

    @NonNull
    public <T> Observable<T> untilStop(@NonNull final Observable<T> observable) {
        return baseUiBindable.untilStop(observable);
    }

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
     *
     * @param view View to get focus for input from keyboard.
     */
    public void showSoftInput(@NonNull final View view) {
        view.requestFocus();
        final InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public void addOnBackPressedListener(@NonNull final OnBackPressedListener onBackPressedListener) {
        onBackPressedListeners.add(onBackPressedListener);
    }

    public void removeOnBackPressedListener(@NonNull final OnBackPressedListener onBackPressedListener) {
        onBackPressedListeners.remove(onBackPressedListener);
    }

    @Override
    public void onBackPressed() {
        for (final OnBackPressedListener onBackPressedListener : onBackPressedListeners) {
            if (onBackPressedListener.onBackPressed()) {
                return;
            }
        }

        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            supportFinishAfterTransition();
        } else {
            getSupportFragmentManager().popBackStackImmediate();
        }
    }

    public interface OnBackPressedListener {

        boolean onBackPressed();

    }

}
