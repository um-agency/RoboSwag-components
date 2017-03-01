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

import java.lang.reflect.Type;

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.observables.storable.Store;
import rx.Completable;
import rx.Single;


/**
 * Created by Gavriil Sitnikov on 18/03/16.
 * Store based on {@link SharedPreferences} for {@link ru.touchin.roboswag.core.observables.storable.Storable}.
 *
 * @param <T> Type of storable. Could be Boolean, Integer, Long, Float or String.
 */
public class PreferenceStore<T> implements Store<String, T> {

    @NonNull
    private final SharedPreferences preferences;

    public PreferenceStore(@NonNull final SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @NonNull
    @Override
    public Single<Boolean> contains(@NonNull final String key) {
        return Single.fromCallable(() -> preferences.contains(key));
    }

    @NonNull
    @Override
    public Completable storeObject(@NonNull final Type storeObjectType, @NonNull final String key, @Nullable final T storeObject) {
        return Completable.fromAction(() -> {
            if (storeObject == null) {
                preferences.edit().remove(key).apply();
                return;
            }

            if (storeObjectType.equals(Boolean.class) || storeObjectType.equals(boolean.class)) {
                preferences.edit().putBoolean(key, (Boolean) storeObject).apply();
            } else if (storeObjectType.equals(String.class)) {
                preferences.edit().putString(key, (String) storeObject).apply();
            } else if (storeObjectType.equals(Integer.class) || storeObjectType.equals(int.class)) {
                preferences.edit().putInt(key, (Integer) storeObject).apply();
            } else if (storeObjectType.equals(Long.class) || storeObjectType.equals(long.class)) {
                preferences.edit().putLong(key, (Long) storeObject).apply();
            } else if (storeObjectType.equals(Float.class) || storeObjectType.equals(float.class)) {
                preferences.edit().putFloat(key, (Float) storeObject).apply();
            } else {
                Lc.assertion("Unsupported type of object " + storeObjectType);
            }
        });
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    //unchecked: we checked class in if-else statements
    public Single<T> loadObject(@NonNull final Type storeObjectType, @NonNull final String key) {
        return Single.fromCallable(() -> {
            if (!preferences.contains(key)) {
                return null;
            }

            if (storeObjectType.equals(Boolean.class) || storeObjectType.equals(boolean.class)) {
                return (T) ((Boolean) preferences.getBoolean(key, false));
            } else if (storeObjectType.equals(String.class)) {
                return (T) (preferences.getString(key, null));
            } else if (storeObjectType.equals(Integer.class) || storeObjectType.equals(int.class)) {
                return (T) ((Integer) preferences.getInt(key, 0));
            } else if (storeObjectType.equals(Long.class) || storeObjectType.equals(long.class)) {
                return (T) ((Long) preferences.getLong(key, 0L));
            } else if (storeObjectType.equals(Float.class) || storeObjectType.equals(float.class)) {
                return (T) ((Float) preferences.getFloat(key, 0f));
            }
            Lc.assertion("Unsupported type of object " + storeObjectType);
            return null;
        });
    }

}
