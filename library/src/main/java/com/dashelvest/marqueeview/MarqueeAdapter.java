package com.dashelvest.marqueeview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class MarqueeAdapter<VH extends RecyclerView.ViewHolder> {
    private final AdapterDataObservable mObservable = new AdapterDataObservable();

    public abstract VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    public abstract void onBindViewHolder(@NonNull VH holder, int position);

    public final void notifyDataSetChanged() {
        mObservable.notifyChanged();
    }

    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        mObservable.registerObserver(observer);
    }

    public void unregisterAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        mObservable.unregisterObserver(observer);
    }

    public abstract int getItemCount();
}
