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

package ru.touchin.roboswag.components.listing;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by Gavriil Sitnikov on 07/12/2015.
 * TODO: fill description
 */
@SuppressWarnings("CPD-START")
public abstract class ItemsProvider<T> {

    private final PublishSubject<List<Change>> listChangesSubject = PublishSubject.create();

    protected void notifyChanges(@NonNull final List<Change> changes) {
        listChangesSubject.onNext(changes);
    }

    @Nullable
    public abstract T getItem(int position);

    public abstract Observable<T> loadItem(int position);

    public abstract int getSize();

    @SuppressWarnings("unchecked")
    public Observable<List<T>> loadRange(final int first, final int last) {
        final List<Observable<T>> itemsRequests = new ArrayList<>();
        for (int i = first; i <= last; i++) {
            itemsRequests.add(loadItem(i));
        }
        return Observable.concatEager(itemsRequests).toList().doOnNext(list -> {
            for (int i = list.size() - 1; i >= 0; i--) {
                if (list.get(i) == null) {
                    list.remove(i);
                }
            }
        });
    }

    @NonNull
    public Observable<List<Change>> observeListChanges() {
        return listChangesSubject;
    }

    public static class Change {

        @NonNull
        private final Type type;
        private final int start;
        private final int count;

        public Change(@NonNull final Type type, final int start, final int count) {
            this.type = type;
            this.start = start;
            this.count = count;
        }

        @NonNull
        public Type getType() {
            return type;
        }

        public int getStart() {
            return start;
        }

        public int getCount() {
            return count;
        }

        public enum Type {
            INSERTED,
            CHANGED,
            REMOVED
        }

    }

}
