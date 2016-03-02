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

import android.graphics.Point;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.widget.ScrollView;

import ru.touchin.roboswag.components.R;

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
        scrollView.scrollTo(((Point) savedState).x, ((Point) savedState).y);
    }

}
