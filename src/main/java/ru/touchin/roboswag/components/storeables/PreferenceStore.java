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

package ru.touchin.roboswag.components.storeables;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.data.storeable.Store;
import ru.touchin.roboswag.core.data.exceptions.StoreException;

/**
 * Created by Gavriil Sitnikov on 18/03/16.
 * TODO: description
 */
public class PreferenceStore<T> implements Store<String, T> {

    @NonNull
    private final SharedPreferences preferences;

    public PreferenceStore(@NonNull final SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public boolean contains(@NonNull final String key) {
        return preferences.contains(key);
    }

    @Override
    public void storeObject(@NonNull final Class<T> storeObjectClass, @NonNull final String key, @Nullable final T storeObject)
            throws StoreException {
        if (storeObject == null) {
            preferences.edit().remove(key).apply();
            return;
        }

        if (storeObjectClass.equals(Boolean.class)) {
            preferences.edit().putBoolean(key, (Boolean) storeObject).apply();
        } else if (storeObjectClass.equals(String.class)) {
            preferences.edit().putString(key, (String) storeObject).apply();
        } else if (storeObjectClass.equals(Integer.class)) {
            preferences.edit().putInt(key, (Integer) storeObject).apply();
        } else if (storeObjectClass.equals(Long.class)) {
            preferences.edit().putLong(key, (Long) storeObject).apply();
        } else if (storeObjectClass.equals(Float.class)) {
            preferences.edit().putFloat(key, (Float) storeObject).apply();
        } else {
            throw new StoreException("Unsupported type of object " + storeObjectClass);
        }
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public T loadObject(@NonNull final Class<T> storeObjectClass, @NonNull final String key) throws StoreException {
        if (!contains(key)) {
            return null;
        }

        if (storeObjectClass.equals(Boolean.class)) {
            return (T) ((Boolean) preferences.getBoolean(key, false));
        } else if (storeObjectClass.equals(String.class)) {
            return (T) (preferences.getString(key, null));
        } else if (storeObjectClass.equals(Integer.class)) {
            return (T) ((Integer) preferences.getInt(key, 0));
        } else if (storeObjectClass.equals(Long.class)) {
            return (T) ((Long) preferences.getLong(key, 0L));
        } else if (storeObjectClass.equals(Float.class)) {
            return (T) ((Float) preferences.getFloat(key, 0f));
        }
        throw new StoreException("Unsupported type of object " + storeObjectClass);
    }

}
