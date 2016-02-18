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
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * Created by Gavriil Sitnikov on 18/07/2014.
 * TextView that supports fonts from Typefaces class
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity",
        "PMD.AvoidDeeplyNestedIfStmts"})
public class TypefacedTextView extends TextView implements TypefacedView {

    private static final int UNSPECIFIED_MEASURE_SPEC = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

    private boolean isScalable;

    public TypefacedTextView(final Context context) {
        super(context);
        TypefacedViewHelper.initialize(this, context, null);
    }

    public TypefacedTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        TypefacedViewHelper.initialize(this, context, attrs);
    }

    public TypefacedTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        TypefacedViewHelper.initialize(this, context, attrs);
    }

    @Override
    public void setTypeface(@NonNull final String name, final int style) {
        TypefacedViewHelper.setTypeface(this, getContext(), name, style);
    }

    @Override
    public void setTypeface(@NonNull final String name) {
        TypefacedViewHelper.setTypeface(this, getContext(), name);
    }

    public void setIsScalable(final boolean isScalable) {
        this.isScalable = isScalable;
        requestLayout();
    }

    @SuppressWarnings("checkstyle:methodlength")
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        if (isScalable) {
            final int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
            final int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
            if (maxWidth > 0 || maxHeight > 0) {
                float difference = getTextSize();
                ScaleAction scaleAction = ScaleAction.DO_NOTHING;
                do {
                    switch (scaleAction) {
                        case SCALE_DOWN:
                            difference /= 2;
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize() - difference);
                            break;
                        case SCALE_UP:
                            difference /= 2;
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize() + difference);
                            break;
                        case DO_NOTHING:
                        default:
                            break;
                    }
                    super.onMeasure(UNSPECIFIED_MEASURE_SPEC, UNSPECIFIED_MEASURE_SPEC);

                    scaleAction = ScaleAction.DO_NOTHING;

                    if (maxWidth > 0) {
                        if (maxWidth < getMeasuredWidth()) {
                            scaleAction = ScaleAction.SCALE_DOWN;
                        } else if (maxWidth > getMeasuredWidth()) {
                            scaleAction = ScaleAction.SCALE_UP;
                        }
                    }

                    if (maxHeight > 0) {
                        if (maxHeight < getMeasuredHeight()) {
                            scaleAction = ScaleAction.SCALE_DOWN;
                        } else if (maxHeight > getMeasuredHeight() && scaleAction != ScaleAction.SCALE_DOWN) {
                            scaleAction = ScaleAction.SCALE_UP;
                        }
                    }

                } while (difference >= 1 && scaleAction != ScaleAction.DO_NOTHING);

                if (scaleAction == ScaleAction.SCALE_DOWN) {
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize() - 1);
                }
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private enum ScaleAction {
        SCALE_DOWN,
        SCALE_UP,
        DO_NOTHING
    }

}
