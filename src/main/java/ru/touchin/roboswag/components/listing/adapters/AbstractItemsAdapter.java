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

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ru.touchin.roboswag.components.R;
import ru.touchin.roboswag.components.listing.ItemsProvider;
import ru.touchin.roboswag.components.listing.ListProvider;
import ru.touchin.roboswag.components.utils.UiUtils;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Actions;

/**
 * Created by Gavriil Sitnikov on 20/11/2015.
 * TODO: fill description
 */
public abstract class AbstractItemsAdapter<TItem, TViewHolder extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int LOADED_ITEM_TYPE = R.id.LOADED_ITEM_TYPE;
    private static final int NOT_LOADED_ITEM_TYPE = R.id.NOT_LOADED_ITEM_TYPE;

    private final Handler postHandler = new Handler(Looper.getMainLooper());
    @Nullable
    private OnItemClickListener<TItem> onItemClickListener;
    @Nullable
    private ItemsProvider<TItem> itemsProvider;
    @Nullable
    private Subscription itemsProviderSubscription;

    public void setItems(@NonNull final List<TItem> items) {
        setItemsProvider(new ListProvider<>(items));
    }

    protected int itemsOffset() {
        return 0;
    }

    public void setItemsProvider(@NonNull final ItemsProvider<TItem> itemsProvider) {
        if (itemsProviderSubscription != null) {
            itemsProviderSubscription.unsubscribe();
            itemsProviderSubscription = null;
        }
        this.itemsProvider = itemsProvider;
        notifyDataSetChanged();
        if (this.itemsProvider != null) {
            itemsProviderSubscription = itemsProvider.observeListChanges()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onItemsChanged);
        }
    }

    protected void onItemsChanged(@NonNull final List<ItemsProvider.Change> changes) {
        for (final ItemsProvider.Change change : changes) {
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
        return getItem(position) != null ? LOADED_ITEM_TYPE : NOT_LOADED_ITEM_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        if (viewType == NOT_LOADED_ITEM_TYPE) {
            return new NotLoadedItemViewHolder(UiUtils.inflate(getNotLoadedItemLayoutRes(), parent));
        }
        return onCreateItemViewHolder(parent, viewType);
    }

    @LayoutRes
    protected int getNotLoadedItemLayoutRes() {
        return R.layout.item_not_loaded;
    }

    public abstract TViewHolder onCreateItemViewHolder(final ViewGroup parent, final int viewType);

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final TItem item = getItem(position);
        if (holder instanceof NotLoadedItemViewHolder) {
            if (itemsProvider == null) {
                Lc.assertion(new ShouldNotHappenException("This adapter shouldn't work without provider"));
                return;
            }
            ((NotLoadedItemViewHolder) holder).bindItem(position, itemsProvider);
        } else {
            if (item == null || itemsProvider == null) {
                Lc.assertion(new ShouldNotHappenException("Item at" + position + " should not be null"));
                return;
            }
            onBindItemToViewHolder((TViewHolder) holder, position, item);
            itemsProvider.loadRange(Math.max(0, position - itemsProvider.getSize() / 2), position + itemsProvider.getSize() / 2).first()
                    .subscribe(Actions.empty(), Actions.empty());
            if (onItemClickListener != null && !isOnClickListenerDisabled(item)) {
                UiUtils.setOnRippleClickListener(holder.itemView, () -> {
                    //TODO: fix multitap
                    postHandler.removeCallbacksAndMessages(null);
                    postHandler.postDelayed(() -> onItemClickListener.onItemClicked(item, position),
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? UiUtils.RIPPLE_EFFECT_DELAY : 0);
                });
            }
        }
    }

    @Override
    public void onDetachedFromRecyclerView(final RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        postHandler.removeCallbacksAndMessages(null);
    }

    protected abstract void onBindItemToViewHolder(@NonNull final TViewHolder holder, final int position, @NonNull TItem item);

    @Nullable
    public TItem getItem(final int position) {
        return itemsProvider != null ? itemsProvider.getItem(position) : null;
    }

    @Override
    public int getItemCount() {
        return itemsProvider != null ? itemsProvider.getSize() : 0;
    }

    public boolean isOnClickListenerDisabled(@NonNull final TItem item) {
        return false;
    }

    public static class NotLoadedItemViewHolder extends RecyclerView.ViewHolder {

        private final View progressBar;
        private final View retryButton;
        @Nullable
        private Subscription subscription;

        public NotLoadedItemViewHolder(final View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.item_not_loaded_progress_bar);
            retryButton = itemView.findViewById(R.id.item_not_loaded_retry_button);
        }

        public void bindItem(final int position, @NonNull final ItemsProvider itemsProvider) {
            loadItem(position, itemsProvider);
            UiUtils.setOnRippleClickListener(retryButton, () -> loadItem(position, itemsProvider));
        }

        @SuppressWarnings("unchecked")
        private void loadItem(final int position, @NonNull final ItemsProvider itemsProvider) {
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
            retryButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            subscription = itemsProvider
                    .loadRange(Math.max(0, position - itemsProvider.getSize() / 2), position + itemsProvider.getSize() / 2)
                    .first()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Actions.empty(),
                            throwable -> {
                                retryButton.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
                            });
        }

    }

    public interface OnItemClickListener<TItem> {

        void onItemClicked(@NonNull TItem item, int position);

    }

}
