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
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.android.RxAndroidUtils;
import rx.Observable;
import rx.Scheduler;

/**
 * Created by Gavriil Sitnikov on 07/12/2015.
 * TODO: fill description
 */
public class SimplePagingProvider<T> extends ItemsProvider<T> {

    public static final int DEFAULT_PAGE_SIZE = 25;

    private final Scheduler scheduler = RxAndroidUtils.createLooperScheduler();
    private final Object lock = new Object();
    private final int pageSize;
    private final SparseArray<List<T>> loadedPages = new SparseArray<>();
    @Nullable
    private Integer maxLoadedPage;
    private boolean isLastPageLoaded;
    private final SparseArray<Observable<List<T>>> loadingPages = new SparseArray<>();
    @NonNull
    private final PagesProvider<T> pagesProvider;

    public SimplePagingProvider(@NonNull final PagesProvider<T> pagesProvider) {
        this(pagesProvider, DEFAULT_PAGE_SIZE);
    }

    public SimplePagingProvider(@NonNull final PagesProvider<T> pagesProvider, final int pageSize) {
        super();
        this.pagesProvider = pagesProvider;
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    @Override
    public int getSize() {
        synchronized (lock) {
            return (maxLoadedPage != null ? maxLoadedPage * pageSize + loadedPages.get(maxLoadedPage).size() : 0)
                    + (isLastPageLoaded ? 0 : 1);
        }
    }

    private int pageIndexOf(final int position) {
        return position / pageSize;
    }

    private int indexOnPage(final int position) {
        return position % pageSize;
    }

    @Nullable
    @Override
    public T getItem(final int position) {
        synchronized (lock) {
            final List<T> page = loadedPages.get(pageIndexOf(position));
            final int indexOnPage = indexOnPage(position);
            return page != null && page.size() > indexOnPage ? page.get(indexOnPage) : null;
        }
    }

    @Override
    @NonNull
    public Observable<T> loadItem(final int position) {
        final int indexOfPage = pageIndexOf(position);
        final int indexOnPage = indexOnPage(position);
        return Observable.<T>create(subscriber -> {
            final List<T> page = loadedPages.get(indexOfPage);
            subscriber.onNext(page != null && page.size() > indexOnPage ? page.get(indexOnPage) : null);
            subscriber.onCompleted();
        }).switchMap(item -> {
            if (item != null || (isLastPageLoaded && maxLoadedPage != null && maxLoadedPage <= indexOfPage)) {
                return Observable.just(item);
            }
            Observable<List<T>> loadingPage = loadingPages.get(indexOfPage);
            if (loadingPage == null) {
                loadingPage = createPageLoadingObservable(indexOfPage);
                loadingPages.put(indexOfPage, loadingPage);
            }
            return loadingPage.map(page -> {
                int index = 0;
                for (final T nextItem : page) {
                    if (index == indexOnPage) {
                        return nextItem;
                    }
                    index++;
                }
                return null;
            });
        }).subscribeOn(scheduler);
    }

    private boolean shouldReplaceMaxLoaded(@NonNull final List<T> pageItems, final int indexOfPage) {
        return (indexOfPage == 0 || !pageItems.isEmpty()) && (maxLoadedPage == null || maxLoadedPage < indexOfPage);
    }

    @NonNull
    private Observable<List<T>> createPageLoadingObservable(final int indexOfPage) {
        return pagesProvider
                .loadPage(indexOfPage * pageSize, pageSize)
                .observeOn(scheduler)
                .map(this::validatePageItems)
                .doOnNext(pageItems -> {
                    synchronized (lock) {
                        final int oldSize = getSize();
                        final boolean oldIsLastPageLoaded = isLastPageLoaded;
                        if (pageItems.size() < pageSize) {
                            if (maxLoadedPage != null && maxLoadedPage > indexOfPage) {
                                maxLoadedPage = indexOfPage == 0 || !pageItems.isEmpty() ? indexOfPage : null;
                                downgradeMaxLoadedPages(indexOfPage);
                                isLastPageLoaded = false;
                            }
                            if (shouldReplaceMaxLoaded(pageItems, indexOfPage)) {
                                maxLoadedPage = indexOfPage;
                            }
                            isLastPageLoaded = isLastPageLoaded
                                    || (maxLoadedPage != null
                                    && (maxLoadedPage == indexOfPage || maxLoadedPage == indexOfPage - 1));
                        } else if (shouldReplaceMaxLoaded(pageItems, indexOfPage)) {
                            maxLoadedPage = indexOfPage;
                        }
                        if (indexOfPage == 0 || !pageItems.isEmpty()) {
                            loadedPages.put(indexOfPage, pageItems);
                        }
                        loadingPages.remove(indexOfPage);
                        updateItemsChanges(indexOfPage, pageItems, oldSize, oldIsLastPageLoaded);
                    }
                }).replay(1)
                .refCount();
    }

    private void downgradeMaxLoadedPages(final int maximum) {
        for (int i = 0; i <= loadedPages.size(); i++) {
            final int key = loadedPages.keyAt(i);
            if (key > maximum || loadedPages.get(key).size() < pageSize) {
                loadedPages.remove(key);
            } else if (maxLoadedPage == null || maxLoadedPage < key) {
                maxLoadedPage = key;
            }
        }
    }

    private void updateItemsChanges(final int indexOfPage, @NonNull final List<T> pageItems, final int oldSize, final boolean oldIsLastPageLoaded) {
        final List<Change> changes = new ArrayList<>();
        final int size = getSize();

        if (size == oldSize) {
            if (!pageItems.isEmpty()) {
                changes.add(new Change(Change.Type.CHANGED, indexOfPage * pageSize, pageItems.size()));
            }
        } else if (size > oldSize) {
            if (!oldIsLastPageLoaded) {
                changes.add(new Change(Change.Type.CHANGED, oldSize - 1, 1));
            }
            changes.add(new Change(Change.Type.INSERTED, oldSize, size - oldSize));
        } else {
            changes.add(new Change(Change.Type.REMOVED, size, oldSize - size));
            if (!isLastPageLoaded) {
                changes.add(new Change(Change.Type.CHANGED, size - 1, 1));
            }
        }
        if (!changes.isEmpty()) {
            notifyChanges(changes);
        }
    }

    @NonNull
    private List<T> validatePageItems(@NonNull final Collection<T> pageItems) {
        if (pageItems.size() > pageSize) {
            Lc.assertion("Unexpectedly big pageItems: " + pageItems.size() + '/' + pageSize);
            return Collections.unmodifiableList(new ArrayList<>(pageItems).subList(0, pageSize));
        } else {
            return Collections.unmodifiableList(new ArrayList<>(pageItems));
        }
    }

    public interface PagesProvider<T> {

        @NonNull
        Observable<Collection<T>> loadPage(int offset, int limit);

    }

}
