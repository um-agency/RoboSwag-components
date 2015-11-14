package org.roboswag.components.savestate;

import android.graphics.Point;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.widget.ScrollView;

import org.roboswag.components.R;

/**
 * Created by Gavriil Sitnikov on 14/11/2015.
 * TODO: fill description
 */
public class ScrollViewSavedStateController extends AbstractSavedStateController {

    @NonNull
    private final ScrollView scrollView;

    public ScrollViewSavedStateController(@NonNull final ScrollView scrollView) {
        this(scrollView.getId(), scrollView);
    }

    public ScrollViewSavedStateController(final int id, @NonNull final ScrollView scrollView) {
        super(id);
        this.scrollView = scrollView;
    }

    @Override
    protected int getTypeId() {
        return R.id.SCROLL_VIEW_SAVED_STATE;
    }

    @NonNull
    @Override
    public Parcelable getState() {
        return new Point(scrollView.getScrollX(), scrollView.getScrollY());
    }

    @Override
    public void restoreState(@NonNull final Parcelable savedState) {
        if (savedState instanceof Point) {
            scrollView.post(() -> scrollView.scrollTo(((Point) savedState).x, ((Point) savedState).y));
        }
    }

}
