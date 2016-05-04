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

package ru.touchin.roboswag.components.storables;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Charsets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import ru.touchin.roboswag.core.data.storable.SafeConverter;
import ru.touchin.roboswag.core.data.storable.SameTypesConverter;
import ru.touchin.roboswag.core.data.storable.Storable;
import ru.touchin.roboswag.core.data.storable.concrete.NonNullSafeStorable;
import ru.touchin.roboswag.core.data.storable.concrete.SafeStorable;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 03/05/2016.
 * TODO: fill description
 */
public final class PreferenceStorables {

    @NonNull
    public static SafeStorable<String, String, String> stringStorable(@NonNull final String name, @NonNull final SharedPreferences preferences) {
        return new Storable.Builder<String, String, String>(name, String.class, false)
                .setSafeStore(String.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .build();
    }

    @NonNull
    public static NonNullSafeStorable<String, String, String> stringStorable(@NonNull final String name,
                                                                             @NonNull final SharedPreferences preferences,
                                                                             @NonNull final String defaultValue) {
        return new Storable.Builder<String, String, String>(name, String.class, false)
                .setSafeStore(String.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .setDefaultValue(defaultValue)
                .build();
    }

    @NonNull
    public static SafeStorable<String, Long, Long> longStorable(@NonNull final String name, @NonNull final SharedPreferences preferences) {
        return new Storable.Builder<String, Long, Long>(name, Long.class, false)
                .setSafeStore(Long.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .build();
    }

    @NonNull
    public static NonNullSafeStorable<String, Long, Long> longStorable(@NonNull final String name,
                                                                       @NonNull final SharedPreferences preferences,
                                                                       final long defaultValue) {
        return new Storable.Builder<String, Long, Long>(name, Long.class, false)
                .setSafeStore(Long.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .setDefaultValue(defaultValue)
                .build();
    }

    @NonNull
    public static SafeStorable<String, Boolean, Boolean> booleanStorable(@NonNull final String name, @NonNull final SharedPreferences preferences) {
        return new Storable.Builder<String, Boolean, Boolean>(name, Boolean.class, false)
                .setSafeStore(Boolean.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .build();
    }

    @NonNull
    public static NonNullSafeStorable<String, Boolean, Boolean> booleanStorable(@NonNull final String name,
                                                                                @NonNull final SharedPreferences preferences,
                                                                                final boolean defaultValue) {
        return new Storable.Builder<String, Boolean, Boolean>(name, Boolean.class, false)
                .setSafeStore(Boolean.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .setDefaultValue(defaultValue)
                .build();
    }

    @NonNull
    public static SafeStorable<String, Integer, Integer> integerStorable(@NonNull final String name, @NonNull final SharedPreferences preferences) {
        return new Storable.Builder<String, Integer, Integer>(name, Integer.class, false)
                .setSafeStore(Integer.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .build();
    }

    @NonNull
    public static NonNullSafeStorable<String, Integer, Integer> integerStorable(@NonNull final String name,
                                                                                @NonNull final SharedPreferences preferences,
                                                                                final int defaultValue) {
        return new Storable.Builder<String, Integer, Integer>(name, Integer.class, false)
                .setSafeStore(Integer.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .setDefaultValue(defaultValue)
                .build();
    }

    @NonNull
    public static SafeStorable<String, Float, Float> floatStorable(@NonNull final String name, @NonNull final SharedPreferences preferences) {
        return new Storable.Builder<String, Float, Float>(name, Float.class, false)
                .setSafeStore(Float.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .build();
    }

    @NonNull
    public static NonNullSafeStorable<String, Float, Float> floatStorable(@NonNull final String name,
                                                                          @NonNull final SharedPreferences preferences,
                                                                          final float defaultValue) {
        return new Storable.Builder<String, Float, Float>(name, Float.class, false)
                .setSafeStore(Float.class, new PreferenceStore<>(preferences), new SameTypesConverter<>())
                .setDefaultValue(defaultValue)
                .build();
    }

    @NonNull
    public static <T extends Enum<T>> SafeStorable<String, T, String> enumStorable(@NonNull final String name,
                                                                                   @NonNull final Class<T> enumClass,
                                                                                   @NonNull final SharedPreferences preferences) {
        return new Storable.Builder<String, T, String>(name, enumClass, false)
                .setSafeStore(String.class, new PreferenceStore<>(preferences), new EnumToStringConverter<>())
                .build();
    }

    @NonNull
    public static <T extends Enum<T>> NonNullSafeStorable<String, T, String> enumStorable(@NonNull final String name,
                                                                                          @NonNull final Class<T> enumClass,
                                                                                          @NonNull final SharedPreferences preferences,
                                                                                          final T defaultValue) {
        return new Storable.Builder<String, T, String>(name, enumClass, false)
                .setSafeStore(String.class, new PreferenceStore<>(preferences), new EnumToStringConverter<>())
                .setDefaultValue(defaultValue)
                .build();
    }

    @NonNull
    public static <T> SafeStorable<String, T, String> jsonStorable(@NonNull final String name,
                                                                   @NonNull final Class<T> jsonClass,
                                                                   @NonNull final SharedPreferences preferences) {
        return new Storable.Builder<String, T, String>(name, jsonClass, false)
                .setSafeStore(String.class, new PreferenceStore<>(preferences), new JsonConverter<>())
                .build();
    }

    @NonNull
    public static <T> NonNullSafeStorable<String, T, String> jsonStorable(@NonNull final String name,
                                                                          @NonNull final Class<T> jsonClass,
                                                                          @NonNull final SharedPreferences preferences,
                                                                          final T defaultValue) {
        return new Storable.Builder<String, T, String>(name, jsonClass, false)
                .setSafeStore(String.class, new PreferenceStore<>(preferences), new JsonConverter<>())
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

    private static class JsonConverter<T> implements SafeConverter<T, String> {

        private static final JsonFactory DEFAULT_JSON_FACTORY = new JacksonFactory();

        @Nullable
        @Override
        public String toStoreObject(@NonNull final Class<T> objectClass,
                                    @NonNull final Class<String> stringClass,
                                    @Nullable final T object) {
            if (object == null) {
                return null;
            }

            final StringWriter stringWriter = new StringWriter();
            JsonGenerator generator = null;
            try {
                generator = DEFAULT_JSON_FACTORY.createJsonGenerator(stringWriter);
                generator.serialize(object);
                generator.flush();
                return stringWriter.toString();
            } catch (final IOException exception) {
                throw new ShouldNotHappenException(exception);
            } finally {
                try {
                    if (generator != null) {
                        generator.close();
                    }
                } catch (final IOException exception) {
                    Lc.assertion(exception);
                }
            }
        }

        @Nullable
        @Override
        public T toObject(@NonNull final Class<T> objectClass, @NonNull final Class<String> stringClass, @Nullable final String source) {
            if (source == null) {
                return null;
            }
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(source.getBytes(Charsets.UTF_8));
            try {
                return DEFAULT_JSON_FACTORY.createJsonObjectParser().parseAndClose(byteArrayInputStream, Charsets.UTF_8, objectClass);
            } catch (final Exception exception) {
                throw new ShouldNotHappenException(exception);
            }
        }

    }

    private PreferenceStorables() {
    }

}
