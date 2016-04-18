package ru.touchin.roboswag.components.navigation;

import android.support.annotation.NonNull;

import rx.Observable;

/**
 * Created by Gavriil Sitnikov on 15/04/16.
 * TODO: description
 */
public interface UiBindable {

    @NonNull
    <T> Observable<T> bind(@NonNull Observable<T> observable);

    @NonNull
    <T> Observable<T> untilStop(@NonNull Observable<T> observable);

    @NonNull
    <T> Observable<T> untilDestroy(@NonNull Observable<T> observable);

}
