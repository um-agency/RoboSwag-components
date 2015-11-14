package org.roboswag.components.savestate;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import org.roboswag.components.R;

/**
 * Created by Gavriil Sitnikov on 14/11/2015.
 * TODO: fill description
 */
public class RecyclerViewSavedStateController extends AbstractSavedStateController {

    @NonNull
    private final RecyclerView recyclerView;

    public RecyclerViewSavedStateController(@NonNull final RecyclerView recyclerView) {
        this(recyclerView.getId(), recyclerView);
    }

    public RecyclerViewSavedStateController(final int id, @NonNull final RecyclerView recyclerView) {
        super(id);
        this.recyclerView = recyclerView;
    }

    @Override
    protected int getTypeId() {
        return R.id.RECYCLER_VIEW_SAVED_STATE;
    }

    @NonNull
    @Override
    public Parcelable getState() {
        return recyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void restoreState(@NonNull final Parcelable savedState) {
        recyclerView.getLayoutManager().onRestoreInstanceState(savedState);
    }

}
