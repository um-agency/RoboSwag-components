package ru.touchin.roboswag.components.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import ru.touchin.roboswag.components.R;
import ru.touchin.roboswag.components.utils.Typefaces;
import ru.touchin.roboswag.core.log.Lc;

/**
 * Created by Gavriil Sitnikov on 23/12/2015.
 * TODO: fill description
 */
public final class TypefacedViewHelper {

    private static boolean allowEmptyCustomTypeface = true;

    public static void setAllowEmptyCustomTypeface(final boolean allowDefaultTypefacedText) {
        allowEmptyCustomTypeface = allowDefaultTypefacedText;
    }

    public static <TTypefacedText extends TextView & TypefacedView> void initialize(final TTypefacedText typefacedText,
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
        } else if (attrs != null && !allowEmptyCustomTypeface) {
            Lc.assertion("TypefacedText has no customTypeface attribute: " + typefacedText);
        }
    }

    public static <TTypefacedText extends TextView & TypefacedView> void setTypeface(final TTypefacedText typefacedText,
                                                                                     final Context context, final String name, final int style) {
        typefacedText.setTypeface(Typefaces.getByName(context, name), style);
    }

    public static <TTypefacedText extends TextView & TypefacedView> void setTypeface(final TTypefacedText typefacedText,
                                                                                     final Context context, final String name) {
        setTypeface(typefacedText, context, name, Typeface.NORMAL);
    }

    private TypefacedViewHelper() {
    }

}
