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

import ru.touchin.roboswag.core.data.storable.Converter;
import ru.touchin.roboswag.core.data.storable.Storeable;
import ru.touchin.roboswag.core.data.exceptions.ConversionException;
import ru.touchin.roboswag.core.log.Lc;

/**
 * Created by Gavriil Sitnikov on 18/03/16.
 * TODO: description
 */
public class JsonPreferenceStoreable<T> extends Storeable<String, T, String> {

    private static final JsonFactory DEFAULT_JSON_FACTORY = new JacksonFactory();

    public JsonPreferenceStoreable(@NonNull final String name,
                                   @NonNull final Class<T> objectClass,
                                   @NonNull final SharedPreferences preferences,
                                   @Nullable final T defaultValue) {
        super(name, name, objectClass, String.class, new PreferenceStore<>(preferences), new JsonConverter<>(),
                false, null, null, defaultValue);
    }

    private static class JsonConverter<T> implements Converter<T, String> {

        @Nullable
        @Override
        public String toStoreObject(@NonNull final Class<T> objectClass,
                                    @NonNull final Class<String> stringClass,
                                    @Nullable final T object)
                throws ConversionException {
            final StringWriter stringWriter = new StringWriter();
            JsonGenerator generator = null;
            try {
                generator = DEFAULT_JSON_FACTORY.createJsonGenerator(stringWriter);
                generator.serialize(object);
                generator.flush();
                return stringWriter.toString();
            } catch (final IOException exception) {
                throw new ConversionException("Object generation error", exception);
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
        public T toObject(@NonNull final Class<T> objectClass, @NonNull final Class<String> stringClass, @Nullable final String source)
                throws ConversionException {
            if (source == null) {
                return null;
            }
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(source.getBytes(Charsets.UTF_8));
            try {
                return DEFAULT_JSON_FACTORY.createJsonObjectParser().parseAndClose(byteArrayInputStream, Charsets.UTF_8, objectClass);
            } catch (final Exception exception) {
                throw new ConversionException("Parsing error", exception);
            }
        }

    }

}
