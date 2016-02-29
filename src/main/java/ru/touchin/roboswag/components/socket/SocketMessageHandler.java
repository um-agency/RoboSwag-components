package ru.touchin.roboswag.components.socket;

import android.support.annotation.NonNull;

/**
 * Created by Gavriil Sitnikov on 29/02/16.
 * TODO: description
 */
public interface SocketMessageHandler<T> {

    T handleMessage(@NonNull T message) throws Exception;

}
