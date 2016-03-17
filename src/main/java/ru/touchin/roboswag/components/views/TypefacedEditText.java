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

package ru.touchin.roboswag.components.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by Gavriil Sitnikov on 18/07/2014.
 * TextView that supports fonts from Typefaces class
 */
public class TypefacedEditText extends EditText implements TypefacedView {

    @Nullable
    private OnTextChangedListener onTextChangedListener;

    public TypefacedEditText(final Context context) {
        super(context);
        TypefacedViewHelper.initialize(this, context, null);
        initialize();
    }

    public TypefacedEditText(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        TypefacedViewHelper.initialize(this, context, attrs);
        initialize();
    }

    public TypefacedEditText(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        TypefacedViewHelper.initialize(this, context, attrs);
        initialize();
    }

    private void initialize() {
        addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(final CharSequence oldText, final int start, final int count, final int after) {
                //do nothing
            }

            @Override
            public void onTextChanged(final CharSequence inputText, final int start, final int before, final int count) {
                if (onTextChangedListener != null) {
                    onTextChangedListener.onTextChanged(inputText);
                }
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                //do nothing
            }

        });
    }

    public void setOnTextChangedListener(@Nullable final OnTextChangedListener onTextChangedListener) {
        this.onTextChangedListener = onTextChangedListener;
    }

    @Override
    public void setTypeface(@NonNull final String name) {
        TypefacedViewHelper.setTypeface(this, getContext(), name);
    }

    @Override
    public void setTypeface(@NonNull final String name, final int style) {
        TypefacedViewHelper.setTypeface(this, getContext(), name, style);
    }

    public interface OnTextChangedListener {

        void onTextChanged(@NonNull CharSequence text);

    }

}
