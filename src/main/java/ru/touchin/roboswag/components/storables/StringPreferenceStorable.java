package ru.touchin.roboswag.components.storables;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.data.storable.SameTypesConverter;
import ru.touchin.roboswag.core.data.storable.Storable;

/**
 * Created by Gavriil Sitnikov on 12/04/16.
 * TODO: description
 */
public class StringPreferenceStorable extends Storable<String, String, String> {

    public StringPreferenceStorable(@NonNull final String name,
                                    @NonNull final SharedPreferences preferences,
                                    @Nullable final String defaultValue) {
        super(name, String.class, String.class, new PreferenceStore<>(preferences), new SameTypesConverter<>(),
                false, null, defaultValue);
    }

}