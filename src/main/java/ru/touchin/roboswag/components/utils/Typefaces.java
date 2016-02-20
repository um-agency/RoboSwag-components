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

package ru.touchin.roboswag.components.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gavriil Sitnikov on 18/07/2014.
 * Manager for typefaces stored in assets 'fonts' folder.
 */
public final class Typefaces {

    private static final Map<String, Typeface> TYPEFACES_MAP = new HashMap<>();

    /**
     * Returns typeface by name from assets 'fonts' folder.
     *
     * @param context Context of assets where typeface file stored in.
     * @param name    Full name of typeface (without extension).
     * @return Typeface from assets.
     */
    @NonNull
    public static Typeface getByName(@NonNull final Context context, @NonNull final String name) {
        synchronized (TYPEFACES_MAP) {
            Typeface result = TYPEFACES_MAP.get(name);
            if (result == null) {
                final AssetManager assetManager = context.getAssets();
                result = Typeface.DEFAULT;
                try {
                    final List<String> fonts = Arrays.asList(assetManager.list("fonts"));
                    if (fonts.contains(name + ".ttf")) {
                        result = Typeface.createFromAsset(assetManager, "fonts/" + name + ".ttf");
                    } else if (fonts.contains(name + ".otf")) {
                        result = Typeface.createFromAsset(assetManager, "fonts/" + name + ".otf");
                    } else {
                        Lc.assertion("Can't find .otf or .ttf file in assets folder 'fonts' with name: " + name);
                    }
                } catch (final IOException exception) {
                    Lc.assertion(new ShouldNotHappenException("Can't get font " + name + '.'
                            + "Did you forget to create assets folder named 'fonts'?", exception));
                }
                TYPEFACES_MAP.put(name, result);
            }
            return result;
        }
    }

    private Typefaces() {
    }

}