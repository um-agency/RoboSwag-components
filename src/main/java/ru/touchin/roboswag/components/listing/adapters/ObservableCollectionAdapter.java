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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

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
import rx.Subscription;
import rx.functions.Actions;

/**
 * Created by Gavriil Sitnikov on 20/11/2015.
 * TODO: fill description
 */
public abstract class ObservableCollectionAdapter<TItem, TViewHolder extends ObservableCollectionAdapter.ViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int PRE_LOADING_COUNT = 10;

    private static final int LOADED_ITEM_TYPE = R.id.LOADED_ITEM_TYPE;

    @NonNull
    private final UiBindable uiBindable;
    @Nullable
    private OnItemClickListener<TItem> onItemClickListener;
    @Nullable
    private ObservableCollection<TItem> observableCollection;
    @Nullable
    private Subscription itemsProviderSubscription;

    @NonNull
    public UiBindable getUiBindable() {
        return uiBindable;
    }

    public ObservableCollectionAdapter(@NonNull final UiBindable uiBindable) {
        super();
        this.uiBindable = uiBindable;
    }

    protected long getItemClickDelay() {
        return UiUtils.RIPPLE_EFFECT_DELAY;
    }

    public void setItems(@NonNull final List<TItem> items) {
        setObservableCollection(new ObservableList<>(items));
    }

    @Nullable
    public ObservableCollection<TItem> getObservableCollection() {
        return observableCollection;
    }

    protected int itemsOffset() {
        return 0;
    }

    public void setObservableCollection(@Nullable final ObservableCollection<TItem> observableCollection) {
        if (itemsProviderSubscription != null) {
            itemsProviderSubscription.unsubscribe();
            itemsProviderSubscription = null;
        }
        this.observableCollection = observableCollection;
        notifyDataSetChanged();
        if (this.observableCollection != null) {
            itemsProviderSubscription = uiBindable.bind(this.observableCollection.observeChanges())
                    .subscribe(this::onItemsChanged);
        }
    }

    protected void onItemsChanged(@NonNull final Collection<Change> changes) {
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
        notifyDataSetChanged();
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
        if (observableCollection == null) {
            Lc.assertion(new ShouldNotHappenException());
            return;
        }

        final TItem item = getItem(position - itemsOffset());
        onBindItemToViewHolder((TViewHolder) holder, position, item);
        ((TViewHolder) holder).bindPosition(uiBindable, observableCollection, position);
        if (onItemClickListener != null && !isOnClickListenerDisabled(item)) {
            UiUtils.setOnRippleClickListener(holder.itemView, () -> onItemClickListener.onItemClicked(item, position), getItemClickDelay());
        }
    }

    protected abstract void onBindItemToViewHolder(@NonNull final TViewHolder holder, final int position, @NonNull TItem item);

    @NonNull
    public TItem getItem(final int position) {
        if (observableCollection == null) {
            throw new ShouldNotHappenException();
        }
        return observableCollection.get(position);
    }

    @Override
    public int getItemCount() {
        return observableCollection != null ? observableCollection.size() : 0;
    }

    public boolean isOnClickListenerDisabled(@NonNull final TItem item) {
        return false;
    }

    public interface OnItemClickListener<TItem> {

        void onItemClicked(@NonNull TItem item, int position);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Nullable
        private Subscription preLoadingSubscription;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
        }

        public void bindPosition(@NonNull final UiBindable uiBindable,
                                 @NonNull final ObservableCollection<?> observableCollection,
                                 final int position) {
            if (preLoadingSubscription != null) {
                preLoadingSubscription.unsubscribe();
            }
            preLoadingSubscription = uiBindable
                    .untilStop(observableCollection
                            .loadRange(Math.max(0, position - PRE_LOADING_COUNT), position + PRE_LOADING_COUNT)
                            .first())
                    .subscribe(Actions.empty(), Actions.empty());
        }

    }

}
