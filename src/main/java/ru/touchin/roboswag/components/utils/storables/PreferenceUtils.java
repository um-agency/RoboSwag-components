/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.roboswag.components.utils.storables;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.observables.storable.SafeConverter;
import ru.touchin.roboswag.core.observables.storable.SameTypesConverter;
import ru.touchin.roboswag.core.observables.storable.Storable;
import ru.touchin.roboswag.core.observables.storable.concrete.NonNullSafeStorable;
import ru.touchin.roboswag.core.observables.storable.concrete.SafeStorable;

/**
 * Created by Gavriil Sitnikov on 01/09/2016.
 * Utility class to get {@link Storable}s based on {@link SharedPreferences}.
 */
public final class PreferenceUtils {

    /**
     * Creates {@link SafeStorable} that stores string into {@link SharedPreferences}.
     *
     * @param name        Name of preference;
     * @param preferences Preferences to store value;
     * @return {@link Storable} for string.
     */
    @NonNull
    public static SafeStorable<String, String, String> stringStorable(@NonNull final String name, @NonNull final SharedPreferences preferences) {
        return new Storable.Builder<String, String, String>(name, String.class)
                .setSafeStore(String.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .build();
    }

    /**
     * Creates {@link NonNullSafeStorable} that stores string into {@link SharedPreferences} with default value.
     *
     * @param name         Name of preference;
     * @param preferences  Preferences to store value;
     * @param defaultValue Default value;
     * @return {@link Storable} for string.
     */
    @NonNull
    public static NonNullSafeStorable<String, String, String> stringStorable(@NonNull final String name,
                                                                             @NonNull final SharedPreferences preferences,
                                                                             @NonNull final String defaultValue) {
        return new Storable.Builder<String, String, String>(name, String.class)
                .setSafeStore(String.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .setDefaultValue(defaultValue)
                .build();
    }

    /**
     * Creates {@link SafeStorable} that stores long into {@link SharedPreferences}.
     *
     * @param name        Name of preference;
     * @param preferences Preferences to store value;
     * @return {@link Storable} for long.
     */
    @NonNull
    public static SafeStorable<String, Long, Long> longStorable(@NonNull final String name, @NonNull final SharedPreferences preferences) {
        return new Storable.Builder<String, Long, Long>(name, Long.class)
                .setSafeStore(Long.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .build();
    }

    /**
     * Creates {@link NonNullSafeStorable} that stores long into {@link SharedPreferences} with default value.
     *
     * @param name         Name of preference;
     * @param preferences  Preferences to store value;
     * @param defaultValue Default value;
     * @return {@link Storable} for long.
     */
    @NonNull
    public static NonNullSafeStorable<String, Long, Long> longStorable(@NonNull final String name,
                                                                       @NonNull final SharedPreferences preferences,
                                                                       final long defaultValue) {
        return new Storable.Builder<String, Long, Long>(name, Long.class)
                .setSafeStore(Long.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .setDefaultValue(defaultValue)
                .build();
    }

    /**
     * Creates {@link SafeStorable} that stores boolean into {@link SharedPreferences}.
     *
     * @param name        Name of preference;
     * @param preferences Preferences to store value;
     * @return {@link Storable} for boolean.
     */
    @NonNull
    public static SafeStorable<String, Boolean, Boolean> booleanStorable(@NonNull final String name, @NonNull final SharedPreferences preferences) {
        return new Storable.Builder<String, Boolean, Boolean>(name, Boolean.class)
                .setSafeStore(Boolean.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .build();
    }

    /**
     * Creates {@link NonNullSafeStorable} that stores boolean into {@link SharedPreferences} with default value.
     *
     * @param name         Name of preference;
     * @param preferences  Preferences to store value;
     * @param defaultValue Default value;
     * @return {@link Storable} for boolean.
     */
    @NonNull
    public static NonNullSafeStorable<String, Boolean, Boolean> booleanStorable(@NonNull final String name,
                                                                                @NonNull final SharedPreferences preferences,
                                                                                final boolean defaultValue) {
        return new Storable.Builder<String, Boolean, Boolean>(name, Boolean.class)
                .setSafeStore(Boolean.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .setDefaultValue(defaultValue)
                .build();
    }

    /**
     * Creates {@link SafeStorable} that stores integer into {@link SharedPreferences}.
     *
     * @param name        Name of preference;
     * @param preferences Preferences to store value;
     * @return {@link Storable} for integer.
     */
    @NonNull
    public static SafeStorable<String, Integer, Integer> integerStorable(@NonNull final String name, @NonNull final SharedPreferences preferences) {
        return new Storable.Builder<String, Integer, Integer>(name, Integer.class)
                .setSafeStore(Integer.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .build();
    }

    /**
     * Creates {@link NonNullSafeStorable} that stores integer into {@link SharedPreferences} with default value.
     *
     * @param name         Name of preference;
     * @param preferences  Preferences to store value;
     * @param defaultValue Default value;
     * @return {@link Storable} for integer.
     */
    @NonNull
    public static NonNullSafeStorable<String, Integer, Integer> integerStorable(@NonNull final String name,
                                                                                @NonNull final SharedPreferences preferences,
                                                                                final int defaultValue) {
        return new Storable.Builder<String, Integer, Integer>(name, Integer.class)
                .setSafeStore(Integer.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .setDefaultValue(defaultValue)
                .build();
    }

    /**
     * Creates {@link SafeStorable} that stores float into {@link SharedPreferences}.
     *
     * @param name        Name of preference;
     * @param preferences Preferences to store value;
     * @return {@link Storable} for float.
     */
    @NonNull
    public static SafeStorable<String, Float, Float> floatStorable(@NonNull final String name, @NonNull final SharedPreferences preferences) {
        return new Storable.Builder<String, Float, Float>(name, Float.class)
                .setSafeStore(Float.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .build();
    }

    /**
     * Creates {@link NonNullSafeStorable} that stores float into {@link SharedPreferences} with default value.
     *
     * @param name         Name of preference;
     * @param preferences  Preferences to store value;
     * @param defaultValue Default value;
     * @return {@link Storable} for float.
     */
    @NonNull
    public static NonNullSafeStorable<String, Float, Float> floatStorable(@NonNull final String name,
                                                                          @NonNull final SharedPreferences preferences,
                                                                          final float defaultValue) {
        return new Storable.Builder<String, Float, Float>(name, Float.class)
                .setSafeStore(Float.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .setDefaultValue(defaultValue)
                .build();
    }

    /**
     * Creates {@link SafeStorable} that stores enum into {@link SharedPreferences}.
     *
     * @param name        Name of preference;
     * @param preferences Preferences to store value;
     * @return {@link Storable} for enum.
     */
    @NonNull
    public static <T extends Enum<T>> SafeStorable<String, T, String> enumStorable(@NonNull final String name,
                                                                                   @NonNull final Class<T> enumClass,
                                                                                   @NonNull final SharedPreferences preferences) {
        return new Storable.Builder<String, T, String>(name, enumClass)
                .setSafeStore(String.class, new PreferenceStore<>(preferences), new EnumToStringConverter<>())
                .build();
    }

    /**
     * Creates {@link NonNullSafeStorable} that stores enum into {@link SharedPreferences} with default value.
     *
     * @param name         Name of preference;
     * @param preferences  Preferences to store value;
     * @param defaultValue Default value;
     * @return {@link Storable} for enum.
     */
    @NonNull
    public static <T extends Enum<T>> NonNullSafeStorable<String, T, String> enumStorable(@NonNull final String name,
                                                                                          @NonNull final Class<T> enumClass,
                                                                                          @NonNull final SharedPreferences preferences,
                                                                                          final T defaultValue) {
        return new Storable.Builder<String, T, String>(name, enumClass)
                .setSafeStore(String.class, new PreferenceStore<>(preferences), new EnumToStringConverter<>())
                .setDefaultValue(defaultValue)
                .build();
    }

    private static class EnumToStringConverter<T extends Enum<T>> implements SafeConverter<T, String> {

        @Nullable
        @Override
        public String toStoreObject(@NonNull final Class<T> objectClass, @NonNull final Class<String> stringClass, @Nullable final T object) {
            return object != null ? object.name() : null;
        }

        @Nullable
        @Override
        public T toObject(@NonNull final Class<T> objectClass, @NonNull final Class<String> stringClass, @Nullable final String stringObject) {
            return stringObject != null ? Enum.valueOf(objectClass, stringObject) : null;
        }
    }

    private PreferenceUtils() {
    }

}
