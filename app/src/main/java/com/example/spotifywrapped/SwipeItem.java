package com.example.spotifywrapped;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifywrapped.ui.gallery.WrapAdapter;

public class SwipeItem extends ItemTouchHelper.SimpleCallback {

    WrapAdapter mItemAdapter;

    SwipeItem(WrapAdapter mItemAdapter) {
        super(0, ItemTouchHelper.LEFT);
        this.mItemAdapter = mItemAdapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
//        mItemAdapter.deleteItem(position);
    }
}
