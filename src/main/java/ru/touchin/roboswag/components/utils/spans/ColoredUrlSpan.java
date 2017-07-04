package ru.touchin.roboswag.components.utils.spans;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.URLSpan;

/**
 * URLSpan that takes custom color and doesn't have the default underline.
 * Don't forget to use
 * textView.setMovementMethod(LinkMovementMethod.getInstance());
 * and
 * textView.setText(spannableString, TextView.BufferType.SPANNABLE);
 */
public class ColoredUrlSpan extends URLSpan {

    @ColorInt
    private final int textColor;

    public ColoredUrlSpan(@ColorInt final int textColor, @NonNull final String url) {
        super(url);
        this.textColor = textColor;
    }

    @Override
    public void updateDrawState(@NonNull final TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        ds.setColor(textColor);
    }

}
