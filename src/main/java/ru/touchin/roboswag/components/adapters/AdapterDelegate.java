package ru.touchin.roboswag.components.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import java.util.List;

import ru.touchin.roboswag.components.utils.LifecycleBindable;

public abstract class AdapterDelegate<TViewHolder extends BindableViewHolder, TItem> {

    @NonNull
    private final LifecycleBindable lifecycleBindable;

    public AdapterDelegate(@NonNull final LifecycleBindable lifecycleBindable) {
        this.lifecycleBindable = lifecycleBindable;
    }

    public abstract int getItemViewType();

    public abstract boolean isForViewType(@NonNull final Object item, final int adapterPosition, final int itemCollectionPosition);

    public long getItemId(@NonNull final TItem item, final int adapterPosition, final int itemCollectionPosition) {
        return 0;
    }

    @NonNull
    public abstract TViewHolder onCreateViewHolder(@NonNull final ViewGroup parent);

    public abstract void onBindViewHolder(@NonNull final TViewHolder holder, @NonNull final TItem item,
                                          final int adapterPosition, final int itemCollectionPosition);

    public void onBindViewHolder(@NonNull final TViewHolder holder, @NonNull final TItem item, @NonNull final List<Object> payloads,
                                 final int adapterPosition, final int itemCollectionPosition) {
        //do nothing by default
    }

    @NonNull
    protected LifecycleBindable getLifecycleBindable() {
        return lifecycleBindable;
    }

}
