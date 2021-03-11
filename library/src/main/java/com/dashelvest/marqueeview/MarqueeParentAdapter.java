package com.dashelvest.marqueeview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MarqueeParentAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private MarqueeAdapter<VH> adapter;

    protected MarqueeParentAdapter(MarqueeAdapter<VH> adapter){
        this.adapter = adapter;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return adapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        adapter.onBindViewHolder(holder, position % adapter.getItemCount());
    }

    @Override
    public final int getItemCount() {
        return Integer.MAX_VALUE;
    }

    /**
     * Register a new observer to listen for data changes.
     *
     * <p>The adapter may publish a variety of events describing specific changes.
     * Not all adapters may support all change types and some may fall back to a generic
     * {@link RecyclerView.AdapterDataObserver#onChanged()
     * "something changed"} event if more specific data is not available.</p>
     *
     * <p>Components registering observers with an adapter are responsible for
     * {@link #unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver)
     * unregistering} those observers when finished.</p>
     *
     * @param observer Observer to register
     *
     * @see #unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver)
     */
    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        adapter.registerAdapterDataObserver(observer);
    }

    /**
     * Unregister an observer currently listening for data changes.
     *
     * <p>The unregistered observer will no longer receive events about changes
     * to the adapter.</p>
     *
     * @param observer Observer to unregister
     *
     * @see #registerAdapterDataObserver(RecyclerView.AdapterDataObserver)
     */
    public void unregisterAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        adapter.unregisterAdapterDataObserver(observer);
    }



}
