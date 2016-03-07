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

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 14/11/2015.
 * TODO: fill description
 */
public abstract class AbstractSavedStateController {

    private final int itemId;

    protected AbstractSavedStateController(final int itemId) {
        if (itemId == 0) {
            Lc.assertion(new ShouldNotHappenException("ItemId = 0 deprecated"));
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
