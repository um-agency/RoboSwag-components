package ru.touchin.roboswag.components.navigation;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 18/04/16.
 * TODO: description
 */
public class BaseUiBindable implements UiBindable {

    @NonNull
    private final BehaviorSubject<Boolean> isCreatedSubject;
    @NonNull
    private final BehaviorSubject<Boolean> isStartedSubject;

    public BaseUiBindable(@NonNull final BehaviorSubject<Boolean> isCreatedSubject,
                          @NonNull final BehaviorSubject<Boolean> isStartedSubject) {
        this.isCreatedSubject = isCreatedSubject;
        this.isStartedSubject = isStartedSubject;
    }

    @NonNull
    public <T> Observable<T> bind(@NonNull final Observable<T> observable) {
        return isStartedSubject
                .switchMap(isStarted -> isStarted ? observable.observeOn(AndroidSchedulers.mainThread()) : Observable.never())
                .takeUntil(isCreatedSubject.filter(created -> !created));
    }

    @NonNull
    public <T> Observable<T> untilStop(@NonNull final Observable<T> observable) {
        return isCreatedSubject.first()
                .switchMap(isCreated -> isCreated ? observable.observeOn(AndroidSchedulers.mainThread()) : Observable.empty())
                .takeUntil(isStartedSubject.filter(started -> !started));
    }

    @NonNull
    @Override
    public <T> Observable<T> untilDestroy(@NonNull final Observable<T> observable) {
        return isCreatedSubject.first()
                .switchMap(isCreated -> isCreated ? observable.observeOn(AndroidSchedulers.mainThread()) : Observable.empty())
                .takeUntil(isCreatedSubject.filter(created -> !created));
    }

}
