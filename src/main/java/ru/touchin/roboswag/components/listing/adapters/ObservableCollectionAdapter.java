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

package ru.touchin.roboswag.components.listing.adapters;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.touchin.roboswag.components.R;
import ru.touchin.roboswag.components.navigation.UiBindable;
import ru.touchin.roboswag.components.utils.UiUtils;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.observables.collections.Change;
import ru.touchin.roboswag.core.observables.collections.ObservableCollection;
import ru.touchin.roboswag.core.observables.collections.ObservableList;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Actions;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 20/11/2015.
 * TODO: fill description
 */
public abstract class ObservableCollectionAdapter<TItem, TViewHolder extends ObservableCollectionAdapter.ViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int PRE_LOADING_COUNT = 10;

    private static final int LOADED_ITEM_TYPE = R.id.LOADED_ITEM_TYPE;
    private static final int UNKNOWN_UPDATE = -1;

    //TODO: replace with wrapper
    @NonNull
    private final BehaviorSubject<ObservableCollection<TItem>> observableCollectionSubject
            = BehaviorSubject.create((ObservableCollection<TItem>) null);
    @NonNull
    private final UiBindable uiBindable;
    @Nullable
    private OnItemClickListener<TItem> onItemClickListener;
    private int lastUpdatedChangeNumber = UNKNOWN_UPDATE;
    @NonNull
    private final Observable<?> newItemsUpdatingObservable;
    @NonNull
    private final Observable<?> historyPreLoadingObservable;

    private final ObservableList<TItem> innerCollection = new ObservableList<>();

    public ObservableCollectionAdapter(@NonNull final UiBindable uiBindable) {
        super();
        this.uiBindable = uiBindable;
        observableCollectionSubject
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(collection -> innerCollection.set(collection != null ? collection.getItems() : new ArrayList<>()))
                .switchMap(observableCollection -> observableCollection != null
                        ? observableCollection.observeChanges().observeOn(AndroidSchedulers.mainThread())
                        : Observable.empty())
                .subscribe(changes -> {
                    for (final Change<TItem> change : changes.getChanges()) {
                        switch (change.getType()) {
                            case INSERTED:
                                int i = 0;
                                for (TItem item : change.getChangedItems()) {
                                    Lc.d("added " + item + " at " + (change.getStart() + i));
                                    i++;
                                }
                                innerCollection.addAll(change.getStart(), change.getChangedItems());
                                break;
                            case CHANGED:
                                int j = 0;
                                for (TItem item : change.getChangedItems()) {
                                    Lc.d("changed " + item + " at " + (change.getStart() + j));
                                    j++;
                                }
                                innerCollection.update(change.getStart(), change.getChangedItems());
                                break;
                            case REMOVED:
                                int k = 0;
                                for (TItem item : change.getChangedItems()) {
                                    Lc.d("removed " + item + " at " + (change.getStart() + k));
                                    k++;
                                }
                                innerCollection.remove(change.getStart(), change.getCount());
                                break;
                            default:
                                Lc.assertion("Not supported " + change.getType());
                                break;
                        }
                    }
                });
        innerCollection.observeChanges()
                .subscribe(this::onItemsChanged);
        newItemsUpdatingObservable = uiBindable.untilStop(observableCollectionSubject
                .switchMap(observableCollection -> observableCollection != null ? observableCollection.loadItem(0) : Observable.empty()));
        historyPreLoadingObservable = uiBindable.untilStop(observableCollectionSubject
                .switchMap(observableCollection -> {
                    final int size = observableCollection.size();
                    return observableCollection.loadRange(size, size + PRE_LOADING_COUNT);
                }));
    }

    @NonNull
    public UiBindable getUiBindable() {
        return uiBindable;
    }

    protected long getItemClickDelay() {
        return UiUtils.RIPPLE_EFFECT_DELAY;
    }

    public void setItems(@NonNull final List<TItem> items) {
        setObservableCollection(new ObservableList<>(items));
    }

    @Nullable
    public ObservableCollection<TItem> getObservableCollection() {
        return observableCollectionSubject.getValue();
    }

    @NonNull
    public Observable<ObservableCollection<TItem>> observeObservableCollection() {
        return observableCollectionSubject.distinctUntilChanged();
    }

    protected int itemsOffset() {
        return 0;
    }

    private void refreshUpdate() {
        notifyDataSetChanged();
        if (observableCollectionSubject.getValue() != null) {
            lastUpdatedChangeNumber = innerCollection.getChangesCount();
            Lc.d("refresh update " + lastUpdatedChangeNumber);
        } else {
            lastUpdatedChangeNumber = UNKNOWN_UPDATE;
        }
    }

    public void setObservableCollection(@Nullable final ObservableCollection<TItem> observableCollection) {
        this.observableCollectionSubject.onNext(observableCollection);
        refreshUpdate();
    }

    protected void onItemsChanged(@NonNull final ObservableCollection.CollectionChange<TItem> collectionChange) {
        if (observableCollectionSubject.getValue() == null) {
            return;
        }
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Lc.assertion("Items changes called on not main thread");
            return;
        }
        if (collectionChange.getNumber() != innerCollection.getChangesCount()
                || collectionChange.getNumber() != lastUpdatedChangeNumber + 1) {
            if (lastUpdatedChangeNumber < collectionChange.getNumber()) {
                Lc.d("refresh update of inconsistence ");
                refreshUpdate();
            }
            return;
        }
        Lc.d("applied changes start " + lastUpdatedChangeNumber);
        notifyAboutChanges(collectionChange.getChanges());
        lastUpdatedChangeNumber = innerCollection.getChangesCount();
        Lc.d("applied changes end " + lastUpdatedChangeNumber);
    }

    private void notifyAboutChanges(@NonNull final Collection<Change<TItem>> changes) {
        for (final Change change : changes) {
            switch (change.getType()) {
                case INSERTED:
                    notifyItemRangeInserted(change.getStart() + itemsOffset(), change.getCount());
                    break;
                case CHANGED:
                    notifyItemRangeChanged(change.getStart() + itemsOffset(), change.getCount());
                    break;
                case REMOVED:
                    notifyItemRangeRemoved(change.getStart() + itemsOffset(), change.getCount());
                    break;
                default:
                    Lc.assertion("Not supported " + change.getType());
                    break;
            }
        }
    }

    public void setOnItemClickListener(@Nullable final OnItemClickListener<TItem> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        refreshUpdate();
    }

    @Override
    public int getItemViewType(final int position) {
        return LOADED_ITEM_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return onCreateItemViewHolder(parent, viewType);
    }

    public abstract TViewHolder onCreateItemViewHolder(final ViewGroup parent, final int viewType);

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (observableCollectionSubject.getValue() == null) {
            Lc.assertion(new ShouldNotHappenException());
            return;
        }

        lastUpdatedChangeNumber = innerCollection.getChangesCount();
        Lc.d("bind changes " + lastUpdatedChangeNumber);

        final TItem item = getItem(position - itemsOffset());
        onBindItemToViewHolder((TViewHolder) holder, position, item);
        ((TViewHolder) holder).bindPosition(observableCollectionSubject.getValue(), this, position);
        if (onItemClickListener != null && !isOnClickListenerDisabled(item)) {
            UiUtils.setOnRippleClickListener(holder.itemView, () -> onItemClickListener.onItemClicked(item, position), getItemClickDelay());
        }
    }

    protected abstract void onBindItemToViewHolder(@NonNull final TViewHolder holder, final int position, @NonNull TItem item);

    @NonNull
    public TItem getItem(final int position) {
        if (observableCollectionSubject.getValue() == null) {
            throw new ShouldNotHappenException();
        }
        return observableCollectionSubject.getValue().get(position);
    }

    @Override
    public int getItemCount() {
        return observableCollectionSubject.getValue() != null ? observableCollectionSubject.getValue().size() : 0;
    }

    public boolean isOnClickListenerDisabled(@NonNull final TItem item) {
        return false;
    }

    public interface OnItemClickListener<TItem> {

        void onItemClicked(@NonNull TItem item, int position);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Nullable
        private Subscription newItemsUpdatingSubscription;
        @Nullable
        private Subscription historyPreLoadingSubscription;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
        }

        @SuppressWarnings("unchecked")
        public void bindPosition(@NonNull final ObservableCollection<?> observableCollection,
                                 @NonNull final ObservableCollectionAdapter adapter,
                                 final int position) {
            if (newItemsUpdatingSubscription != null) {
                newItemsUpdatingSubscription.unsubscribe();
                newItemsUpdatingSubscription = null;
            }
            if (historyPreLoadingSubscription != null) {
                historyPreLoadingSubscription.unsubscribe();
                historyPreLoadingSubscription = null;
            }
            if (position == adapter.itemsOffset()) {
                newItemsUpdatingSubscription = adapter.newItemsUpdatingObservable.subscribe(Actions.empty(), Actions.empty());
            }
            if (position - adapter.itemsOffset() > observableCollection.size() - PRE_LOADING_COUNT) {
                historyPreLoadingSubscription = adapter.historyPreLoadingObservable.subscribe(Actions.empty(), Actions.empty());
            }
        }

    }

}