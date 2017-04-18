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
import java.util.concurrent.TimeUnit;

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
import rx.Subscription;
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
public abstract class ObservableCollectionAdapter<TItem, TItemViewHolder extends BindableViewHolder>
        extends RecyclerView.Adapter<BindableViewHolder> {

    //it is needed to avoid massive requests on initial view holders attaching (like if we will add 10 items they all will try to load history)
    private static final long DELAY_BEFORE_LOADING_HISTORY = TimeUnit.SECONDS.toMillis(1);

    private static final int PRE_LOADING_COUNT = 10;

    @NonNull
    private final BehaviorSubject<Optional<ObservableCollection<TItem>>> observableCollectionSubject
            = BehaviorSubject.create(new Optional<>(null));
    @NonNull
    private final LifecycleBindable lifecycleBindable;
    @Nullable
    private OnItemClickListener<TItem> onItemClickListener;
    private int lastUpdatedChangeNumber = -1;
    @NonNull
    private final Observable<?> historyPreLoadingObservable;
    @Nullable
    private Subscription historyPreLoadingSubscription;

    @NonNull
    private final ObservableList<TItem> innerCollection = new ObservableList<>();
    private boolean anyChangeApplied;
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
        historyPreLoadingObservable = observableCollectionSubject
                .switchMap(optional -> {
                    final ObservableCollection<TItem> collection = optional.get();
                    if (!(collection instanceof LoadingMoreList)) {
                        return Observable.empty();
                    }
                    final int size = collection.size();
                    return ((LoadingMoreList) collection).loadRange(size, size + PRE_LOADING_COUNT);
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

    @NonNull
    public List<AdapterDelegate<TItemViewHolder, TItem>> getDelegates() {
        return Collections.unmodifiableList(delegates);
    }

    @SuppressWarnings("unchecked")
    public void addDelegate(@NonNull final AdapterDelegate<? extends TItemViewHolder, ? extends TItem> delegate) {
        delegates.add((AdapterDelegate<TItemViewHolder, TItem>) delegate);
    }

    @SuppressWarnings("unchecked")
    public void removeDelegate(@NonNull final AdapterDelegate<? extends TItemViewHolder, ? extends TItem> delegate) {
        delegates.remove((AdapterDelegate<TItemViewHolder, TItem>) delegate);
    }

    @Override
    public int getItemViewType(final int adapterPosition) {
        final int itemCollectionPosition = adapterPosition - getHeadersCount();
        if (itemCollectionPosition < 0 || itemCollectionPosition >= innerCollection.size()) {
            return super.getItemViewType(adapterPosition);
        }
        final TItem item = innerCollection.get(itemCollectionPosition);
        for (final AdapterDelegate<?, TItem> delegate : delegates) {
            if (delegate.isForViewType(item, adapterPosition, itemCollectionPosition)) {
                return delegate.getItemViewType();
            }
        }
        return super.getItemViewType(adapterPosition);
    }

    @Override
    public long getItemId(final int adapterPosition) {
        final int itemCollectionPosition = adapterPosition - getHeadersCount();
        if (itemCollectionPosition < 0 || itemCollectionPosition >= innerCollection.size()) {
            return super.getItemId(adapterPosition);
        }

        final int itemViewType = getItemViewType(adapterPosition);
        final TItem item = innerCollection.get(itemCollectionPosition);
        for (final AdapterDelegate<?, TItem> delegate : delegates) {
            if (delegate.getItemViewType() == itemViewType) {
                return delegate.getItemId(item, adapterPosition, itemCollectionPosition);
            }
        }
        return super.getItemId(adapterPosition);
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
    public void onBindViewHolder(@NonNull final BindableViewHolder holder, final int adapterPosition) {
        lastUpdatedChangeNumber = innerCollection.getChangesCount();

        final int itemCollectionPosition = adapterPosition - getHeadersCount();
        if (itemCollectionPosition < 0 || itemCollectionPosition >= innerCollection.size()) {
            return;
        }

        updateHistoryLoadingSubscription(adapterPosition);
        bindItemViewHolder(holder, null, adapterPosition, itemCollectionPosition);
    }

    @Override
    public void onBindViewHolder(@NonNull final BindableViewHolder holder, final int adapterPosition, @NonNull final List<Object> payloads) {
        super.onBindViewHolder(holder, adapterPosition, payloads);
        final int itemCollectionPosition = adapterPosition - getHeadersCount();
        if (itemCollectionPosition < 0 || itemCollectionPosition >= innerCollection.size()) {
            return;
        }
        bindItemViewHolder(holder, payloads, adapterPosition, itemCollectionPosition);
    }

    @SuppressWarnings("unchecked")
    private void bindItemViewHolder(@NonNull final BindableViewHolder holder, @Nullable final List<Object> payloads,
                                    final int adapterPosition, final int itemCollectionPosition) {
        final TItemViewHolder itemViewHolder;
        try {
            itemViewHolder = (TItemViewHolder) holder;
        } catch (final ClassCastException exception) {
            Lc.assertion(exception);
            return;
        }
        final TItem item = innerCollection.get(itemCollectionPosition);
        final int itemViewType = getItemViewType(adapterPosition);

        if (onItemClickListener != null && !isOnClickListenerDisabled(item)) {
            UiUtils.setOnRippleClickListener(holder.itemView, () -> onItemClickListener.onItemClicked(item, adapterPosition), getItemClickDelay());
        }

        for (final AdapterDelegate<TItemViewHolder, TItem> delegate : delegates) {
            if (itemViewType == delegate.getItemViewType()) {
                if (payloads == null) {
                    delegate.onBindViewHolder(itemViewHolder, item, adapterPosition, itemCollectionPosition);
                } else {
                    delegate.onBindViewHolder(itemViewHolder, item, payloads, adapterPosition, itemCollectionPosition);
                }
                return;
            }
        }
        if (payloads == null) {
            onBindItemToViewHolder(itemViewHolder, adapterPosition, item);
        } else {
            onBindItemToViewHolder(itemViewHolder, adapterPosition, item, payloads);
        }
    }

    private void updateHistoryLoadingSubscription(final int position) {
        if ((historyPreLoadingSubscription != null && !historyPreLoadingSubscription.isUnsubscribed())
                || position - getHeadersCount() > innerCollection.size() - PRE_LOADING_COUNT) {
            return;
        }
        historyPreLoadingSubscription = lifecycleBindable
                .untilDestroy(historyPreLoadingObservable
                        .delaySubscription(DELAY_BEFORE_LOADING_HISTORY, TimeUnit.MILLISECONDS)
                        .onErrorResumeNext(Observable.empty()));
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

    /**
     * Method to bind item (from {@link #getObservableCollection()}) to item-specific ViewHolder with payloads.
     * It is not calling for headers and footer which counts are returned by {@link #getHeadersCount()} and @link #getFootersCount()}.
     *
     * @param holder   ViewHolder to bind item to;
     * @param position Position of ViewHolder (NOT item!);
     * @param item     Item returned by position (WITH HEADER OFFSET!);
     * @param payloads Payloads;
     */
    protected void onBindItemToViewHolder(@NonNull final TItemViewHolder holder, final int position, @NonNull final TItem item,
                                          @NonNull final List<Object> payloads) {
        // do nothing by default
    }

    @Nullable
    public TItem getItem(final int position) {
        final int positionInList = position - getHeadersCount();
        return positionInList < 0 || positionInList >= innerCollection.size() ? null : innerCollection.get(positionInList);
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

}
