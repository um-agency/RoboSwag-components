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

package ru.touchin.roboswag.components.savestate;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import ru.touchin.roboswag.components.R;

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
