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

package org.roboswag.components.listing.adapters;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.roboswag.components.R;
import org.roboswag.components.listing.ItemsProvider;
import org.roboswag.components.listing.ListProvider;
import org.roboswag.components.utils.UiUtils;
import org.roboswag.core.log.Lc;
import org.roboswag.core.utils.ShouldNotHappenException;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

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

    public void setItems(@NonNull final List<TItem> items) {
        setItemsProvider(new ListProvider<>(items));
    }

    public void setItemsProvider(@NonNull final ItemsProvider<TItem> itemsProvider) {
        this.itemsProvider = itemsProvider;
        notifyDataSetChanged();
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
            return new NotLoadedItemViewHolder(UiUtils.inflate(R.layout.item_not_loaded, parent), this);
        }
        return onCreateItemViewHolder(parent, viewType);
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
            if (item == null) {
                Lc.assertion(new ShouldNotHappenException("Item at" + position + " should not be null"));
                return;
            }
            onBindItemToViewHolder((TViewHolder) holder, position, item);
            if (onItemClickListener != null && !isOnClickListenerDisabled(item)) {
                holder.itemView.setOnClickListener(v -> {
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

    public static class NotLoadedItemViewHolder extends RecyclerView.ViewHolder {

        @NonNull
        private final RecyclerView.Adapter parent;
        private final View progressBar;
        private final View retryButton;
        @Nullable
        private Subscription subscription;

        public NotLoadedItemViewHolder(final View itemView, @NonNull final RecyclerView.Adapter parent) {
            super(itemView);
            this.parent = parent;
            progressBar = itemView.findViewById(R.id.item_not_loaded_progress_bar);
            retryButton = itemView.findViewById(R.id.item_not_loaded_retry_button);
        }

        public void bindItem(final int position, @NonNull final ItemsProvider itemsProvider) {
            loadItem(position, itemsProvider);
            retryButton.setOnClickListener(v -> loadItem(position, itemsProvider));
        }

        @SuppressWarnings("unchecked")
        private void loadItem(final int position, @NonNull final ItemsProvider itemsProvider) {
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
            retryButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            subscription = itemsProvider.loadItem(position)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ignored -> parent.notifyDataSetChanged(),
                            throwable -> {
                                retryButton.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
                            });
        }

    }

    public boolean isOnClickListenerDisabled(@NonNull final TItem item) {
        return false;
    }

    public interface OnItemClickListener<TItem> {

        void onItemClicked(@NonNull TItem item, int position);

    }

}
