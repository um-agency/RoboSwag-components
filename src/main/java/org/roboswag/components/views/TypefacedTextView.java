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
import android.util.AttributeSet;
import android.widget.TextView;

import org.roboswag.components.utils.Typefaces;

/**
 * Created by Gavriil Sitnikov on 18/07/2014.
 * TextView that supports fonts from Typefaces class
 */
public class TypefacedTextView extends TextView implements TypefacedText {

    public TypefacedTextView(final Context context) {
        super(context);
        Typefaces.initialize(this, context, null);
    }

    public TypefacedTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        Typefaces.initialize(this, context, attrs);
    }

    public TypefacedTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        Typefaces.initialize(this, context, attrs);
    }

    @Override
    public void setTypeface(final String name, final int style) {
        Typefaces.setTypeface(this, getContext(), name, style);
    }

    @Override
    public void setTypeface(final String name) {
        Typefaces.setTypeface(this, getContext(), name);
    }

}
