package ru.touchin.roboswag.components.storables;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.data.exceptions.ConversionException;
import ru.touchin.roboswag.core.data.storable.Converter;
import ru.touchin.roboswag.core.data.storable.Storable;

/**
 * Created by Gavriil Sitnikov on 12/04/16.
 * TODO: description
 */
public class EnumPreferenceStorable<T extends Enum<T>> extends Storable<String, T, String> {

    public EnumPreferenceStorable(@NonNull final String name,
                                  @NonNull final Class<T> enumClass,
                                  @NonNull final SharedPreferences preferences,
                                  @Nullable final T defaultValue) {
        super(name, name, enumClass, String.class, new PreferenceStore<>(preferences), new EnumToStringConverter<>(),
                false, null, null, defaultValue);
    }

    private static class EnumToStringConverter<T extends Enum<T>> implements Converter<T, String> {

        @Nullable
        @Override
        public String toStoreObject(@NonNull final Class<T> objectClass, @NonNull final Class<String> stringClass,
                                    @Nullable final T object) throws ConversionException {
            return object != null ? object.name() : null;
        }

        @Nullable
        @Override
        public T toObject(@NonNull final Class<T> objectClass, @NonNull final Class<String> stringClass,
                          @Nullable final String stringObject) throws ConversionException {
            return stringObject != null ? Enum.valueOf(objectClass, stringObject) : null;
        }
    }

}