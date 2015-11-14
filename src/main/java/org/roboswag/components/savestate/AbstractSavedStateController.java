package org.roboswag.components.savestate;

import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by Gavriil Sitnikov on 14/11/2015.
 * TODO: fill description
 */
public abstract class AbstractSavedStateController {

    private final int itemId;

    protected AbstractSavedStateController(final int itemId) {
        this.itemId = itemId;
    }

    public long getId() {
        return (((long) getTypeId()) << 32) | (itemId & 0xffffffffL);
    }

    protected abstract int getTypeId();

    @NonNull
    public abstract Parcelable getState();

    public abstract void restoreState(@NonNull Parcelable savedState);

}
