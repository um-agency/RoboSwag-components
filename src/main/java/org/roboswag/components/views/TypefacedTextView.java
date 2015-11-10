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

package org.roboswag.components.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import org.roboswag.components.R;

import org.roboswag.components.utils.Typefaces;

/**
 * Created by Gavriil Sitnikov on 18/07/2014.
 * TextView that supports fonts from Typefaces class
 */
public class TypefacedTextView extends TextView {

    public TypefacedTextView(Context context) {
        super(context);
        initialize(context, null);
    }

    public TypefacedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public TypefacedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    public void setTypeface(String name, int style) {
        setTypeface(Typefaces.getByName(getContext(), name), style);
    }

    public void setTypeface(String name) {
        setTypeface(name, Typeface.NORMAL);
    }

    private void initialize(Context context, AttributeSet attrs) {
        String customTypeface = null;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TypefacedTextView);
            customTypeface = a.getString(R.styleable.TypefacedTextView_customTypeface);
            a.recycle();
        }

        if (customTypeface != null && !isInEditMode()) {
            Typeface typeface = getTypeface();
            setTypeface(customTypeface, typeface != null ? typeface.getStyle() : Typeface.NORMAL);
        }
    }

}
