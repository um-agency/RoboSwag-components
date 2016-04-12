package ru.touchin.roboswag.components.storeables;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.data.storeable.SameTypesConverter;
import ru.touchin.roboswag.core.data.storeable.Storeable;

/**
 * Created by Gavriil Sitnikov on 12/04/16.
 * TODO: description
 */
public class StringPreferenceStoreable extends Storeable<String, String, String> {

    public StringPreferenceStoreable(@NonNull final String name,
                                     @NonNull final SharedPreferences preferences,
                                     @Nullable final String defaultValue) {
        super(name, name, String.class, String.class, new PreferenceStore<>(preferences), new SameTypesConverter<>(),
                false, null, null, defaultValue);
    }

}