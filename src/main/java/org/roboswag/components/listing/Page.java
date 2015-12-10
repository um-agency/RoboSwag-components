package org.roboswag.components.listing;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;

/**
 * Created by Gavriil Sitnikov on 08/12/2015.
 * TODO: fill description
 */
public interface Page<T> {

    @NonNull
    Collection<T> getItems();

    @Nullable
    Integer getTotalCount();

}
