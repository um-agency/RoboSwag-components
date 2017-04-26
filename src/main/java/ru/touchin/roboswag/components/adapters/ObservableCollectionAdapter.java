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
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ru.touchin.roboswag.components.utils.LifecycleBindable;
import ru.touchin.roboswag.components.utils.UiUtils;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.observables.collections.Change;
import ru.touchin.roboswag.core.observables.collections.ObservableCollection;
import ru.touchin.roboswag.core.observables.collections.ObservableList;
import ru.touchin.roboswag.core.observables.collections.loadable.LoadingMoreList;
import ru.touchin.roboswag.core.utils.Optional;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 20/11/2015.
 * Adapter based on {@link ObservableCollection} and providing some useful features like:
 * - item-based binding method;
 * - delegates by {@link AdapterDelegate} over itemViewType logic;
 * - item click listener setup by {@link #setOnItemClickListener(OnItemClickListener)};
 * - allows to inform about footers/headers by overriding base create/bind methods and {@link #getHeadersCount()} plus {@link #getFootersCount()};
 * - by default it is pre-loading items for collections like {@link ru.touchin.roboswag.core.observables.collections.loadable.LoadingMoreList}.
 *
 * @param <TItem>           Type of items to bind to ViewHolders;
 * @param <TItemViewHolder> Type of ViewHolders to show items.
 */
@SuppressWarnings("unchecked")
public abstract class ObservableCollectionAdapter<TItem, TItemViewHolder extends BindableViewHolder>
        extends RecyclerView.Adapter<BindableViewHolder> {

    private static final int PRE_LOADING_COUNT = 20;

    @NonNull
    private final BehaviorSubject<Optional<ObservableCollection<TItem>>> observableCollectionSubject
            = BehaviorSubject.create(new Optional<>(null));
    @NonNull
    private final BehaviorSubject<Boolean> moreAutoLoadingRequested = BehaviorSubject.create();
    @NonNull
    private final LifecycleBindable lifecycleBindable;
    @Nullable
    private Object onItemClickListener;
    private int lastUpdatedChangeNumber = -1;

    @NonNull
    private final ObservableList<TItem> innerCollection = new ObservableList<>();
    private boolean anyChangeApplied;
    private long itemClickDelayMillis;
    @NonNull
    private final List<RecyclerView> attachedRecyclerViews = new LinkedList<>();
    @NonNull
    private final List<AdapterDelegate<TItemViewHolder, TItem>> delegates = new ArrayList<>();

    public ObservableCollectionAdapter(@NonNull final LifecycleBindable lifecycleBindable) {
        super();
        this.lifecycleBindable = lifecycleBindable;
        innerCollection.observeChanges().subscribe(this::onItemsChanged);
        lifecycleBindable.untilDestroy(observableCollectionSubject
                .switchMap(optional -> {
                    final ObservableCollection<TItem> collection = optional.get();
                    if (collection == null) {
                        innerCollection.clear();
                        return Observable.empty();
                    }
                    innerCollection.set(collection.getItems());
                    return collection.observeChanges().observeOn(AndroidSchedulers.mainThread());
                }), this::onApplyChanges);
        lifecycleBindable.untilDestroy(createMoreAutoLoadingObservable());
    }

    @NonNull
    private Observable createMoreAutoLoadingObservable() {
        return observableCollectionSubject
                .switchMap(collectionOptional -> {
                    final ObservableCollection<TItem> collection = collectionOptional.get();
                    if (!(collection instanceof LoadingMoreList)) {
                        return Observable.empty();
                    }
                    return moreAutoLoadingRequested
                            .distinctUntilChanged()
                            .switchMap(requested -> {
                                if (!requested) {
                                    return Observable.empty();
                                }
                                final int size = collection.size();
                                return ((LoadingMoreList<?, ?, ?>) collection)
                                        .loadRange(size, size + PRE_LOADING_COUNT)
                                        .onErrorResumeNext(Observable.empty())
                                        .doOnCompleted(() -> moreAutoLoadingRequested.onNext(false));
                            });
                });
    }

    private void onApplyChanges(@NonNull final ObservableCollection.CollectionChange<TItem> changes) {
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
    }

    /**
     * Returns if any change of source collection applied to adapter.
     * It's important to not show some footers or headers before first change have applied.
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
        return observableCollectionSubject.getValue().get();
    }

    /**
     * Method to observe {@link ObservableCollection} which provides items and it's changes.
     *
     * @return Observable of inner {@link ObservableCollection}.
     */
    @NonNull
    public Observable<Optional<ObservableCollection<TItem>>> observeObservableCollection() {
        return observableCollectionSubject;
    }

    /**
     * Sets {@link ObservableCollection} which will provide items and it's changes.
     *
     * @param observableCollection Inner {@link ObservableCollection}.
     */
    public void setObservableCollection(@Nullable final ObservableCollection<TItem> observableCollection) {
        this.observableCollectionSubject.onNext(new Optional<>(observableCollection));
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

    /**
     * Returns list of added delegates.
     *
     * @return List of {@link AdapterDelegate}.
     */
    @NonNull
    public List<AdapterDelegate<TItemViewHolder, TItem>> getDelegates() {
        return Collections.unmodifiableList(delegates);
    }

    /**
     * Adds {@link AdapterDelegate} to adapter.
     *
     * @param delegate Delegate to add.
     */
    public void addDelegate(@NonNull final AdapterDelegate<? extends TItemViewHolder, ? extends TItem> delegate) {
        for (final AdapterDelegate addedDelegate : delegates) {
            if (addedDelegate.getItemViewType() == delegate.getItemViewType()) {
                Lc.assertion("AdapterDelegate with viewType=" + delegate.getItemViewType() + " already added");
                return;
            }
        }
        delegates.add((AdapterDelegate<TItemViewHolder, TItem>) delegate);
        notifyDataSetChanged();
    }

    /**
     * Removes {@link AdapterDelegate} from adapter.
     *
     * @param delegate Delegate to remove.
     */
    public void removeDelegate(@NonNull final AdapterDelegate<? extends TItemViewHolder, ? extends TItem> delegate) {
        delegates.remove((AdapterDelegate<TItemViewHolder, TItem>) delegate);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(final int positionInAdapter) {
        final int positionInCollection = positionInAdapter - getHeadersCount();
        if (positionInCollection < 0 || positionInCollection >= innerCollection.size()) {
            return super.getItemViewType(positionInAdapter);
        }
        final TItem item = innerCollection.get(positionInCollection);
        for (final AdapterDelegate<?, TItem> delegate : delegates) {
            if (delegate.isForViewType(item, positionInAdapter, positionInCollection)) {
                return delegate.getItemViewType();
            }
        }
        return super.getItemViewType(positionInAdapter);
    }

    @Override
    public long getItemId(final int positionInAdapter) {
        final int positionInCollection = positionInAdapter - getHeadersCount();
        if (positionInCollection < 0 || positionInCollection >= innerCollection.size()) {
            return super.getItemId(positionInAdapter);
        }

        final int itemViewType = getItemViewType(positionInAdapter);
        final TItem item = innerCollection.get(positionInCollection);
        for (final AdapterDelegate<?, TItem> delegate : delegates) {
            if (delegate.getItemViewType() == itemViewType) {
                return delegate.getItemId(item, positionInAdapter, positionInCollection);
            }
        }
        return super.getItemId(positionInAdapter);
    }

    @Override
    public int getItemCount() {
        return getHeadersCount() + innerCollection.size() + getFootersCount();
    }

    @NonNull
    @Override
    public BindableViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        for (final AdapterDelegate<?, TItem> delegate : delegates) {
            if (delegate.getItemViewType() == viewType) {
                return delegate.onCreateViewHolder(parent);
            }
        }
        throw new ShouldNotHappenException("Add some AdapterDelegate or override this method");
    }

    @Override
    public void onBindViewHolder(@NonNull final BindableViewHolder holder, final int positionInAdapter) {
        lastUpdatedChangeNumber = innerCollection.getChangesCount();

        final int positionInCollection = positionInAdapter - getHeadersCount();
        if (positionInCollection < 0 || positionInCollection >= innerCollection.size()) {
            return;
        }

        updateMoreAutoLoadingRequest(positionInCollection);
        bindItemViewHolder(holder, null, positionInAdapter, positionInCollection);
    }

    @Override
    public void onBindViewHolder(@NonNull final BindableViewHolder holder, final int positionInAdapter, @NonNull final List<Object> payloads) {
        super.onBindViewHolder(holder, positionInAdapter, payloads);
        final int positionInCollection = positionInAdapter - getHeadersCount();
        if (positionInCollection < 0 || positionInCollection >= innerCollection.size()) {
            return;
        }
        bindItemViewHolder(holder, payloads, positionInAdapter, positionInCollection);
    }

    private void bindItemViewHolder(@NonNull final BindableViewHolder holder, @Nullable final List<Object> payloads,
                                    final int positionInAdapter, final int positionInCollection) {
        final TItemViewHolder itemViewHolder;
        try {
            itemViewHolder = (TItemViewHolder) holder;
        } catch (final ClassCastException exception) {
            Lc.assertion(exception);
            return;
        }
        final TItem item = innerCollection.get(positionInCollection);
        final int itemViewType = getItemViewType(positionInAdapter);

        updateClickListener(holder, item, positionInAdapter, positionInCollection);
        for (final AdapterDelegate<TItemViewHolder, TItem> delegate : delegates) {
            if (itemViewType == delegate.getItemViewType()) {
                if (payloads == null) {
                    delegate.onBindViewHolder(itemViewHolder, item, positionInAdapter, positionInCollection);
                } else {
                    delegate.onBindViewHolder(itemViewHolder, item, payloads, positionInAdapter, positionInCollection);
                }
                return;
            }
        }
        if (payloads == null) {
            onBindItemToViewHolder(itemViewHolder, positionInAdapter, item);
        } else {
            onBindItemToViewHolder(itemViewHolder, positionInAdapter, item, payloads);
        }
    }

    private void updateClickListener(@NonNull final BindableViewHolder holder, @NonNull final TItem item,
                                     final int positionInAdapter, final int positionInCollection) {
        if (onItemClickListener != null && !isOnClickListenerDisabled(item, positionInAdapter, positionInCollection)) {
            UiUtils.setOnRippleClickListener(holder.itemView,
                    () -> {
                        if (onItemClickListener instanceof OnItemClickListener) {
                            ((OnItemClickListener) onItemClickListener).onItemClicked(item);
                        } else if (onItemClickListener instanceof OnItemWithPositionClickListener) {
                            ((OnItemWithPositionClickListener) onItemClickListener).onItemClicked(item, positionInAdapter, positionInCollection);
                        } else {
                            Lc.assertion("Unexpected onItemClickListener type " + onItemClickListener);
                        }
                    },
                    itemClickDelayMillis);
        }
    }

    private void updateMoreAutoLoadingRequest(final int positionInCollection) {
        if (positionInCollection > innerCollection.size() - PRE_LOADING_COUNT) {
            return;
        }
        moreAutoLoadingRequested.onNext(true);
    }

    /**
     * Method to bind item (from {@link #getObservableCollection()}) to item-specific ViewHolder.
     * It is not calling for headers and footer which counts are returned by {@link #getHeadersCount()} and @link #getFootersCount()}.
     *
     * @param holder            ViewHolder to bind item to;
     * @param positionInAdapter Position of ViewHolder (NOT item!);
     * @param item              Item returned by position (WITH HEADER OFFSET!).
     */
    protected void onBindItemToViewHolder(@NonNull final TItemViewHolder holder, final int positionInAdapter, @NonNull final TItem item) {
        // do nothing by default - let delegates do it
    }

    /**
     * Method to bind item (from {@link #getObservableCollection()}) to item-specific ViewHolder with payloads.
     * It is not calling for headers and footer which counts are returned by {@link #getHeadersCount()} and @link #getFootersCount()}.
     *
     * @param holder            ViewHolder to bind item to;
     * @param positionInAdapter Position of ViewHolder in adapter (NOT item!);
     * @param item              Item returned by position (WITH HEADER OFFSET!);
     * @param payloads          Payloads.
     */
    protected void onBindItemToViewHolder(@NonNull final TItemViewHolder holder, final int positionInAdapter, @NonNull final TItem item,
                                          @NonNull final List<Object> payloads) {
        // do nothing by default - let delegates do it
    }

    @Nullable
    public TItem getItem(final int positionInAdapter) {
        final int positionInCollection = positionInAdapter - getHeadersCount();
        return positionInCollection < 0 || positionInCollection >= innerCollection.size() ? null : innerCollection.get(positionInCollection);
    }

    /**
     * Sets item click listener.
     *
     * @param onItemClickListener Item click listener.
     */
    public void setOnItemClickListener(@Nullable final OnItemClickListener<TItem> onItemClickListener) {
        this.setOnItemClickListener(onItemClickListener, UiUtils.RIPPLE_EFFECT_DELAY);
    }

    /**
     * Sets item click listener.
     *
     * @param onItemClickListener  Item click listener;
     * @param itemClickDelayMillis Delay of calling click listener.
     */
    public void setOnItemClickListener(@Nullable final OnItemClickListener<TItem> onItemClickListener, final long itemClickDelayMillis) {
        this.onItemClickListener = onItemClickListener;
        this.itemClickDelayMillis = itemClickDelayMillis;
        refreshUpdate();
    }

    /**
     * Sets item click listener.
     *
     * @param onItemClickListener Item click listener.
     */
    public void setOnItemClickListener(@Nullable final OnItemWithPositionClickListener<TItem> onItemClickListener) {
        this.setOnItemClickListener(onItemClickListener, UiUtils.RIPPLE_EFFECT_DELAY);
    }

    /**
     * Sets item click listener.
     *
     * @param onItemClickListener  Item click listener;
     * @param itemClickDelayMillis Delay of calling click listener.
     */
    public void setOnItemClickListener(@Nullable final OnItemWithPositionClickListener<TItem> onItemClickListener, final long itemClickDelayMillis) {
        this.onItemClickListener = onItemClickListener;
        this.itemClickDelayMillis = itemClickDelayMillis;
        refreshUpdate();
    }

    /**
     * Returns if click listening disabled or not for specific item.
     *
     * @param item                 Item to check click availability;
     * @param positionInAdapter    Position of clicked item in adapter (with headers);
     * @param positionInCollection Position of clicked item in inner collection;
     * @return True if click listener enabled for such item.
     */
    public boolean isOnClickListenerDisabled(@NonNull final TItem item, final int positionInAdapter, final int positionInCollection) {
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
         * @param item Clicked item.
         */
        void onItemClicked(@NonNull TItem item);

    }

    /**
     * Interface to simply add item click listener based on item position in adapter and collection.
     *
     * @param <TItem> Type of item
     */
    public interface OnItemWithPositionClickListener<TItem> {

        /**
         * Calls when item have clicked.
         *
         * @param item                 Clicked item;
         * @param positionInAdapter    Position of clicked item in adapter (with headers);
         * @param positionInCollection Position of clicked item in inner collection.
         */
        void onItemClicked(@NonNull TItem item, final int positionInAdapter, final int positionInCollection);

    }

}
