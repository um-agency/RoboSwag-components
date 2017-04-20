package ru.touchin.roboswag.components.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import ru.touchin.roboswag.components.utils.LifecycleBindable;
import ru.touchin.roboswag.components.utils.UiUtils;

/**
 * Objects of such class controls creation and binding of specific type of RecyclerView's ViewHolders.
 * Default {@link #getItemViewType} is generating on construction of object.
 *
 * @param <TViewHolder> Type of {@link BindableViewHolder} of delegate;
 * @param <TItem>       Type of items to bind to {@link BindableViewHolder}s.
 */
@SuppressWarnings("PMD.TooManyMethods")
//TooManyMethods: it's ok as it is LifecycleBindable
public abstract class AdapterDelegate<TViewHolder extends BindableViewHolder, TItem> implements LifecycleBindable {

    @NonNull
    private final LifecycleBindable parentLifecycleBindable;
    private final int defaultItemViewType;

    public AdapterDelegate(@NonNull final LifecycleBindable parentLifecycleBindable) {
        this.parentLifecycleBindable = parentLifecycleBindable;
        this.defaultItemViewType = UiUtils.OfViews.generateViewId();
    }

    /**
     * Returns parent {@link LifecycleBindable} that this delegate created from (e.g. Activity or ViewController).
     *
     * @return Parent {@link LifecycleBindable}.
     */
    @NonNull
    public LifecycleBindable getParentLifecycleBindable() {
        return parentLifecycleBindable;
    }

    /**
     * Unique ID of AdapterDelegate.
     *
     * @return Unique ID.
     */
    public int getItemViewType() {
        return defaultItemViewType;
    }

    /**
     * Returns if object is processable by this delegate.
     * This item will be casted to {@link TItem} and passes to {@link #onBindViewHolder(TViewHolder, TItem, int, int)}.
     *
     * @param item                   Item to check;
     * @param adapterPosition        Position of item in adapter;
     * @param itemCollectionPosition Position of item in collection that contains item;
     * @return True if item is processable by this delegate.
     */
    public abstract boolean isForViewType(@NonNull final Object item, final int adapterPosition, final int itemCollectionPosition);

    /**
     * Returns unique ID of item to support stable ID's logic of RecyclerView's adapter.
     *
     * @param item                   Item to check;
     * @param adapterPosition        Position of item in adapter;
     * @param itemCollectionPosition Position of item in collection that contains item;
     * @return Unique item ID.
     */
    public long getItemId(@NonNull final TItem item, final int adapterPosition, final int itemCollectionPosition) {
        return 0;
    }

    /**
     * Creates ViewHolder to bind item to it later.
     *
     * @param parent Container of ViewHolder's view.
     * @return New ViewHolder.
     */
    @NonNull
    public abstract TViewHolder onCreateViewHolder(@NonNull final ViewGroup parent);

    /**
     * Binds item to created by this object ViewHolder.
     *
     * @param holder                 ViewHolder to bind item to;
     * @param item                   Item to check;
     * @param adapterPosition        Position of item in adapter;
     * @param itemCollectionPosition Position of item in collection that contains item;
     */
    public abstract void onBindViewHolder(@NonNull final TViewHolder holder, @NonNull final TItem item,
                                          final int adapterPosition, final int itemCollectionPosition);

    /**
     * Binds item with payloads to created by this object ViewHolder.
     *
     * @param holder                 ViewHolder to bind item to;
     * @param item                   Item to check;
     * @param payloads               Payloads;
     * @param adapterPosition        Position of item in adapter;
     * @param itemCollectionPosition Position of item in collection that contains item;
     */
    public void onBindViewHolder(@NonNull final TViewHolder holder, @NonNull final TItem item, @NonNull final List<Object> payloads,
                                 final int adapterPosition, final int itemCollectionPosition) {
        //do nothing by default
    }

    @SuppressWarnings("CPD-START")
    //CPD: it is same as in other implementation based on BaseLifecycleBindable
    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Observable<T> observable) {
        return parentLifecycleBindable.untilStop(observable);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Observable<T> observable, @NonNull final Consumer<T> onNextAction) {
        return parentLifecycleBindable.untilStop(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Observable<T> observable,
                                    @NonNull final Consumer<T> onNextAction,
                                    @NonNull final Consumer<Throwable> onErrorAction) {
        return parentLifecycleBindable.untilStop(observable, onNextAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Observable<T> observable,
                                    @NonNull final Consumer<T> onNextAction,
                                    @NonNull final Consumer<Throwable> onErrorAction,
                                    @NonNull final Action onCompletedAction) {
        return parentLifecycleBindable.untilStop(observable, onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Single<T> single) {
        return parentLifecycleBindable.untilStop(single);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Single<T> single, @NonNull final Consumer<T> onSuccessAction) {
        return parentLifecycleBindable.untilStop(single, onSuccessAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Single<T> single,
                                    @NonNull final Consumer<T> onSuccessAction,
                                    @NonNull final Consumer<Throwable> onErrorAction) {
        return parentLifecycleBindable.untilStop(single, onSuccessAction, onErrorAction);
    }

    @NonNull
    @Override
    public Disposable untilStop(@NonNull final Completable completable) {
        return parentLifecycleBindable.untilStop(completable);
    }

    @NonNull
    @Override
    public Disposable untilStop(@NonNull final Completable completable, @NonNull final Action onCompletedAction) {
        return parentLifecycleBindable.untilStop(completable, onCompletedAction);
    }

    @NonNull
    @Override
    public Disposable untilStop(@NonNull final Completable completable,
                                @NonNull final Action onCompletedAction,
                                @NonNull final Consumer<Throwable> onErrorAction) {
        return parentLifecycleBindable.untilStop(completable, onCompletedAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable) {
        return parentLifecycleBindable.untilDestroy(observable);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable, @NonNull final Consumer<T> onNextAction) {
        return parentLifecycleBindable.untilDestroy(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable,
                                       @NonNull final Consumer<T> onNextAction,
                                       @NonNull final Consumer<Throwable> onErrorAction) {
        return parentLifecycleBindable.untilDestroy(observable, onNextAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable,
                                       @NonNull final Consumer<T> onNextAction,
                                       @NonNull final Consumer<Throwable> onErrorAction,
                                       @NonNull final Action onCompletedAction) {
        return parentLifecycleBindable.untilDestroy(observable, onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Single<T> single) {
        return parentLifecycleBindable.untilDestroy(single);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Single<T> single, @NonNull final Consumer<T> onSuccessAction) {
        return parentLifecycleBindable.untilDestroy(single, onSuccessAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Single<T> single,
                                       @NonNull final Consumer<T> onSuccessAction,
                                       @NonNull final Consumer<Throwable> onErrorAction) {
        return parentLifecycleBindable.untilDestroy(single, onSuccessAction, onErrorAction);
    }

    @NonNull
    @Override
    public Disposable untilDestroy(@NonNull final Completable completable) {
        return parentLifecycleBindable.untilDestroy(completable);
    }

    @NonNull
    @Override
    public Disposable untilDestroy(@NonNull final Completable completable, @NonNull final Action onCompletedAction) {
        return parentLifecycleBindable.untilDestroy(completable, onCompletedAction);
    }

    @NonNull
    @Override
    public Disposable untilDestroy(@NonNull final Completable completable,
                                   @NonNull final Action onCompletedAction,
                                   @NonNull final Consumer<Throwable> onErrorAction) {
        return parentLifecycleBindable.untilDestroy(completable, onCompletedAction, onErrorAction);
    }

}
