package org.roboswag.components.savestate;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.roboswag.core.log.Lc;
import org.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 14/11/2015.
 * TODO: fill description
 */
public abstract class AbstractSavedStateController {

    private final int itemId;

    protected AbstractSavedStateController(final int itemId) {
        if (itemId == 0) {
            Lc.fatalException(new ShouldNotHappenException("ItemId = 0 deprecated"));
        }
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
