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

package ru.touchin.roboswag.components.socket;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.StringReader;

/**
 * Created by Ilia Kurtov on 22.01.2016.
 */
public class SocketEvent<T> {

    private static final JsonFactory DEFAULT_JSON_FACTORY = new JacksonFactory();

    @NonNull
    private final String name;
    @NonNull
    private final Class<T> clz;
    @Nullable
    private final SocketMessageHandler<T> eventDataHandler;

    public SocketEvent(@NonNull final String name, @NonNull final Class<T> clz, @Nullable final SocketMessageHandler<T> eventDataHandler) {
        this.name = name;
        this.clz = clz;
        this.eventDataHandler = eventDataHandler;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public SocketMessageHandler<T> getEventDataHandler() {
        return eventDataHandler;
    }

    @NonNull
    public T parse(@NonNull final String source) throws Exception {
        return DEFAULT_JSON_FACTORY.createJsonObjectParser().parseAndClose(new StringReader(source), clz);
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof SocketEvent
                && ((SocketEvent) object).name.equals(name)
                && ((SocketEvent) object).clz.equals(clz)
                && ((SocketEvent) object).eventDataHandler == eventDataHandler;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + clz.hashCode();
    }

}
