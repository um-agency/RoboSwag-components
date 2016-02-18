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

package org.roboswag.components.listing;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import rx.Observable;

/**
 * Created by Gavriil Sitnikov on 07/12/2015.
 * TODO: fill description
 */
public class ListProvider<T> extends ItemsProvider<T> {

    private final List<T> items;

    public ListProvider(@NonNull final Collection<T> collection) {
        items = Collections.unmodifiableList(new ArrayList<>(collection));
    }

    @Nullable
    @Override
    public T getItem(final int position) {
        return items.get(position);
    }

    @Override
    public Observable<T> loadItem(final int position) {
        return Observable.just(items.get(position));
    }

    @Override
    public int getSize() {
        return items.size();
    }

}
