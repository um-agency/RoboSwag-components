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

package org.roboswag.components.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.TextView;

import org.roboswag.components.R;
import org.roboswag.components.views.TypefacedText;
import org.roboswag.core.log.Lc;
import org.roboswag.core.utils.ShouldNotHappenException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gavriil Sitnikov on 18/07/2014.
 * Typefaces manager
 */
public final class Typefaces {

    private static final Map<String, Typeface> TYPEFACES_MAP = new HashMap<>();

    private static boolean allowEmptyCustomTypeface = true;

    public static void setAllowEmptyCustomTypeface(final boolean allowDefaultTypefacedText) {
        Typefaces.allowEmptyCustomTypeface = allowDefaultTypefacedText;
    }

    /* Returns typeface by name from assets 'fonts' folder */
    @NonNull
    public static Typeface getByName(@NonNull final Context context, @NonNull final String name) {
        synchronized (TYPEFACES_MAP) {
            Typeface result = TYPEFACES_MAP.get(name);
            if (result == null) {
                final AssetManager assetManager = context.getAssets();
                try {
                    final List<String> fonts = Arrays.asList(assetManager.list("fonts"));
                    if (fonts.contains(name + ".ttf")) {
                        result = Typeface.createFromAsset(assetManager, "fonts/" + name + ".ttf");
                    } else if (fonts.contains(name + ".otf")) {
                        result = Typeface.createFromAsset(assetManager, "fonts/" + name + ".otf");
                    } else {
                        throw new IllegalStateException("Can't find .otf or .ttf file in folder 'fonts' with name: " + name);
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Typefaces files should be in folder named 'fonts'", e);
                }
                TYPEFACES_MAP.put(name, result);
            }

            return result;
        }
    }

    public static <TTypefacedText extends TextView & TypefacedText> void initialize(final TTypefacedText typefacedText,
                                                                                    final Context context, final AttributeSet attrs) {
        typefacedText.setIncludeFontPadding(false);
        String customTypeface = null;
        if (attrs != null) {
            final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TypefacedTextView);
            customTypeface = typedArray.getString(R.styleable.TypefacedTextView_customTypeface);
            typedArray.recycle();
        }

        if (customTypeface != null) {
            if (!typefacedText.isInEditMode()) {
                final Typeface typeface = typefacedText.getTypeface();
                typefacedText.setTypeface(customTypeface, typeface == null ? Typeface.NORMAL : typeface.getStyle());
            }
        } else if (!allowEmptyCustomTypeface) {
            Lc.assertion(new ShouldNotHappenException("TypefacedText has no customTypeface attribute: " + typefacedText));
        }
    }

    public static <TTypefacedText extends TextView & TypefacedText> void setTypeface(final TTypefacedText typefacedText,
                                                                                     final Context context, final String name, final int style) {
        typefacedText.setTypeface(Typefaces.getByName(context, name), style);
    }

    public static <TTypefacedText extends TextView & TypefacedText> void setTypeface(final TTypefacedText typefacedText,
                                                                                     final Context context, final String name) {
        setTypeface(typefacedText, context, name, Typeface.NORMAL);
    }

    private Typefaces() {
    }

}