package ru.touchin.roboswag.components.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gavriil Sitnikov on 24/03/16.
 * TODO: description
 */
public abstract class Logic {

    private final static Map<Class<? extends Logic>, WeakReference<Logic>> LOGIC_INSTANCES = new HashMap<>();

    @SuppressWarnings("unchecked")
    @NonNull
    public static <T extends Logic> T getInstance(@NonNull final Context context, @NonNull final Class<T> logicClass) {
        T result;
        synchronized (LOGIC_INSTANCES) {
            final WeakReference<Logic> reference = LOGIC_INSTANCES.get(logicClass);
            result = reference != null ? (T) reference.get() : null;
            if (result == null) {
                result = constructLogic(context.getApplicationContext(), logicClass);
                LOGIC_INSTANCES.put(logicClass, new WeakReference<>(result));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Logic> T constructLogic(@NonNull final Context context, @NonNull final Class<T> logicClass) {
        if (logicClass.getConstructors().length != 1 || logicClass.getConstructors()[0].getParameterTypes().length != 1) {
            throw new IllegalArgumentException("There should be only one public constructor(Context) for class " + logicClass);
        }
        final Constructor<?> constructor = logicClass.getConstructors()[0];
        try {
            return (T) constructor.newInstance(context);
        } catch (final Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    public Logic(@NonNull final Context context) {
        //do nothing
    }

}
