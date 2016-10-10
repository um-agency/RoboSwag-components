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

package ru.touchin.roboswag.components.adapters;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.touchin.roboswag.components.utils.LifecycleBindable;
import ru.touchin.roboswag.components.utils.UiUtils;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.observables.collections.Change;
import ru.touchin.roboswag.core.observables.collections.ObservableCollection;
import ru.touchin.roboswag.core.observables.collections.ObservableList;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Actions;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 20/11/2015.
 * Adapter based on {@link ObservableCollection} and providing some useful features like:
 * - item-based binding by {@link #onBindItemToViewHolder(ViewHolder, int, Object)}} method;
 * - item click listener setup by {@link #setOnItemClickListener(OnItemClickListener)};
 * - allows to inform about footers/headers by overriding base create/bind methods and {@link #getHeadersCount()} plus {@link #getFootersCount()};
 * - by default it is pre-loading items for collections like {@link ru.touchin.roboswag.core.observables.collections.loadable.LoadingMoreList}.
 *
 * @param <TItem>           Type of items to bind to ViewHolders;
 * @param <TItemViewHolder> Type of ViewHolders to show items.
 */
public abstract class ObservableCollectionAdapter<TItem, TItemViewHolder extends ObservableCollectionAdapter.ViewHolder>
        extends RecyclerView.Adapter<BindableViewHolder> {

    private static final int PRE_LOADING_COUNT = 10;

    @NonNull
    private final BehaviorSubject<ObservableCollection<TItem>> observableCollectionSubject
            = BehaviorSubject.create((ObservableCollection<TItem>) null);
    @NonNull
    private final LifecycleBindable lifecycleBindable;
    @Nullable
    private OnItemClickListener<TItem> onItemClickListener;
    private int lastUpdatedChangeNumber = -1;
    @NonNull
    private final Observable historyPreLoadingObservable;

    @NonNull
    private final ObservableList<TItem> innerCollection = new ObservableList<>();
    private boolean anyChangeApplied;
    @NonNull
    private final List<RecyclerView> attachedRecyclerViews = new LinkedList<>();

    public ObservableCollectionAdapter(@NonNull final LifecycleBindable lifecycleBindable) {
        super();
        this.lifecycleBindable = lifecycleBindable;
        innerCollection.observeChanges().subscribe(this::onItemsChanged);
        lifecycleBindable.untilDestroy(observableCollectionSubject
                        .doOnNext(collection -> innerCollection.set(collection != null ? collection.getItems() : new ArrayList<>()))
                        .<ObservableCollection.CollectionChange<TItem>>switchMap(observableCollection -> observableCollection != null
                                ? observableCollection.observeChanges().observeOn(AndroidSchedulers.mainThread())
                                : Observable.empty()),
                changes -> {
                    anyChangeApplied = true;
                    for (final Change<TItem> change : changes.getChanges()) {
                        switch (change.getType()) {
                            case INSERTED:
                                innerCollection.addAll(change.getStart(), change.getChangedItems());
                                break;
                            case CHANGED:
                                innerCollection.update(change.getStart(), change.getChangedItems());
                                break;
                            case REMOVED:
                                innerCollection.remove(change.getStart(), change.getCount());
                                break;
                            default:
                                Lc.assertion("Not supported " + change.getType());
                                break;
                        }
                    }
                });
        historyPreLoadingObservable = observableCollectionSubject
                .switchMap(observableCollection -> {
                    final int size = observableCollection.size();
                    return observableCollection.loadRange(size, size + PRE_LOADING_COUNT);
                });
    }

    /**
     * Returns if any change of source collection applied to adapter.
     * It's important to not show some footers or header before first change have applied.
     *
     * @return True id any change applied.
     */
    public boolean isAnyChangeApplied() {
        return anyChangeApplied;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        attachedRecyclerViews.add(recyclerView);
    }

    private boolean anyRecyclerViewShown() {
        for (final RecyclerView recyclerView : attachedRecyclerViews) {
            if (recyclerView.isShown()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull final RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        attachedRecyclerViews.remove(recyclerView);
    }

    /**
     * Returns parent {@link LifecycleBindable} (Activity/ViewController etc.).
     *
     * @return Parent {@link LifecycleBindable}.
     */
    @NonNull
    public LifecycleBindable getLifecycleBindable() {
        return lifecycleBindable;
    }

    /**
     * Returns {@link ObservableCollection} which provides items and it's changes.
     *
     * @return Inner {@link ObservableCollection}.
     */
    @Nullable
    public ObservableCollection<TItem> getObservableCollection() {
        return observableCollectionSubject.getValue();
    }

    /**
     * Method to observe {@link ObservableCollection} which provides items and it's changes.
     *
     * @return Observable of inner {@link ObservableCollection}.
     */
    @NonNull
    public Observable<ObservableCollection<TItem>> observeObservableCollection() {
        return observableCollectionSubject.distinctUntilChanged();
    }

    /**
     * Sets {@link ObservableCollection} which will provide items and it's changes.
     *
     * @param observableCollection Inner {@link ObservableCollection}.
     */
    public void setObservableCollection(@Nullable final ObservableCollection<TItem> observableCollection) {
        this.observableCollectionSubject.onNext(observableCollection);
        refreshUpdate();
    }

    /**
     * Simply sets items.
     *
     * @param items Items to set.
     */
    public void setItems(@NonNull final Collection<TItem> items) {
        setObservableCollection(new ObservableList<>(items));
    }

    /**
     * Calls when collection changes.
     *
     * @param collectionChange Changes of collection.
     */
    protected void onItemsChanged(@NonNull final ObservableCollection.CollectionChange<TItem> collectionChange) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Lc.assertion("Items changes called on not main thread");
            return;
        }
        if (!anyChangeApplied || !anyRecyclerViewShown()) {
            anyChangeApplied = true;
            refreshUpdate();
            return;
        }
        if (collectionChange.getNumber() != innerCollection.getChangesCount()
                || collectionChange.getNumber() != lastUpdatedChangeNumber + 1) {
            if (lastUpdatedChangeNumber < collectionChange.getNumber()) {
                refreshUpdate();
            }
            return;
        }
        notifyAboutChanges(collectionChange.getChanges());
        lastUpdatedChangeNumber = innerCollection.getChangesCount();
    }

    private void refreshUpdate() {
        notifyDataSetChanged();
        lastUpdatedChangeNumber = innerCollection.getChangesCount();
    }

    private void notifyAboutChanges(@NonNull final Collection<Change<TItem>> changes) {
        for (final Change change : changes) {
            switch (change.getType()) {
                case INSERTED:
                    notifyItemRangeInserted(change.getStart() + getHeadersCount(), change.getCount());
                    break;
                case CHANGED:
                    notifyItemRangeChanged(change.getStart() + getHeadersCount(), change.getCount());
                    break;
                case REMOVED:
                    if (getItemCount() - getHeadersCount() == 0) {
                        //TODO: bug of recyclerview?
                        notifyDataSetChanged();
                    } else {
                        notifyItemRangeRemoved(change.getStart() + getHeadersCount(), change.getCount());
                    }
                    break;
                default:
                    Lc.assertion("Not supported " + change.getType());
                    break;
            }
        }
    }

    /**
     * Returns headers count goes before items.
     *
     * @return Headers count.
     */
    protected int getHeadersCount() {
        return 0;
    }

    /**
     * Returns footers count goes after items and headers.
     *
     * @return Footers count.
     */
    protected int getFootersCount() {
        return 0;
    }

    @Override
    public int getItemCount() {
        return getHeadersCount() + innerCollection.size() + getFootersCount();
    }

    @NonNull
    @Override
    public abstract BindableViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType);

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull final BindableViewHolder holder, final int position) {
        lastUpdatedChangeNumber = innerCollection.getChangesCount();

        final int itemPosition = position - getHeadersCount();
        if (itemPosition < 0 || itemPosition >= innerCollection.size()) {
            return;
        }

        final TItemViewHolder itemViewHolder;
        try {
            itemViewHolder = (TItemViewHolder) holder;
        } catch (final ClassCastException exception) {
            Lc.assertion(exception);
            return;
        }
        final TItem item = innerCollection.get(itemPosition);
        itemViewHolder.adapter = this;
        onBindItemToViewHolder(itemViewHolder, position, item);
        itemViewHolder.bindPosition(position);
        if (onItemClickListener != null && !isOnClickListenerDisabled(item)) {
            UiUtils.setOnRippleClickListener(holder.itemView, () -> onItemClickListener.onItemClicked(item, position), getItemClickDelay());
        }
    }

    /**
     * Method to bind item (from {@link #getObservableCollection()}) to item-specific ViewHolder.
     * It is not calling for headers and footer which counts are returned by {@link #getHeadersCount()} and @link #getFootersCount()}.
     *
     * @param holder   ViewHolder to bind item to;
     * @param position Position of ViewHolder (NOT item!);
     * @param item     Item returned by position (WITH HEADER OFFSET!).
     */
    protected abstract void onBindItemToViewHolder(@NonNull TItemViewHolder holder, int position, @NonNull TItem item);

    @Nullable
    public TItem getItem(final int position) {
        final int positionInList = position - getHeadersCount();
        return positionInList < 0 || positionInList >= innerCollection.size() ? null : innerCollection.get(positionInList);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull final BindableViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onAttachedToWindow();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull final BindableViewHolder holder) {
        holder.onDetachedFromWindow();
        super.onViewDetachedFromWindow(holder);
    }

    /**
     * Sets item click listener.
     *
     * @param onItemClickListener Item click listener.
     */
    public void setOnItemClickListener(@Nullable final OnItemClickListener<TItem> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        refreshUpdate();
    }

    /**
     * Returns delay of item click. By default returns delay of ripple effect duration.
     *
     * @return Milliseconds delay of click.
     */
    protected long getItemClickDelay() {
        return UiUtils.RIPPLE_EFFECT_DELAY;
    }

    /**
     * Returns if click listening disabled or not for specific item.
     *
     * @param item Item to check click availability;
     * @return True if click listener enabled for such item.
     */
    public boolean isOnClickListenerDisabled(@NonNull final TItem item) {
        return false;
    }

    /**
     * Interface to simply add item click listener.
     *
     * @param <TItem> Type of item
     */
    public interface OnItemClickListener<TItem> {

        /**
         * Calls when item have clicked.
         *
         * @param item     Clicked item;
         * @param position Position of clicked item.
         */
        void onItemClicked(@NonNull TItem item, int position);

    }

    /**
     * Base item ViewHolder that have included pre-loading logic.
     */
    public static class ViewHolder extends BindableViewHolder {

        //it is needed to avoid massive requests on initial view holders attaching (like if we will add 10 items they all will try to load history)
        private static final long DELAY_BEFORE_LOADING_HISTORY = TimeUnit.SECONDS.toMillis(1);

        @Nullable
        private Subscription historyPreLoadingSubscription;
        @Nullable
        public ObservableCollectionAdapter adapter;

        public ViewHolder(@NonNull final LifecycleBindable baseBindable, @NonNull final View itemView) {
            super(baseBindable, itemView);
        }

        /**
         * Bind position to enable pre-loading for connected {@link ObservableCollection}.
         *
         * @param position Position of ViewHolder.
         */
        @SuppressWarnings("unchecked")
        //unchecked: it's ok, we just need to load something more
        public void bindPosition(final int position) {
            if (historyPreLoadingSubscription != null) {
                historyPreLoadingSubscription.unsubscribe();
                historyPreLoadingSubscription = null;
            }
            if (adapter != null && position - adapter.getHeadersCount() > adapter.innerCollection.size() - PRE_LOADING_COUNT) {
                historyPreLoadingSubscription = bind(adapter.historyPreLoadingObservable
                        .delaySubscription(DELAY_BEFORE_LOADING_HISTORY, TimeUnit.MILLISECONDS)
                        .onErrorResumeNext(Observable.empty()), Actions.empty());
            }
        }

    }

}
