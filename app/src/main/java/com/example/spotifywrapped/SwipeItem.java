package com.example.spotifywrapped;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifywrapped.ui.gallery.WrapAdapter;

public class SwipeItem extends ItemTouchHelper.SimpleCallback {

    private WrapAdapter mItemAdapter;
    private Drawable icon;
    private Paint paint;

    public SwipeItem(WrapAdapter mItemAdapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.mItemAdapter = mItemAdapter;
        paint = new Paint();
        paint.setColor(Color.RED);
    }

    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();

        int swipeDirs = super.getSwipeDirs(recyclerView, viewHolder);
        boolean canSwipeLeft = mItemAdapter.isItemSwipable(position, 0);
        boolean canSwipeRight = mItemAdapter.isItemSwipable(position, 1);

        if (!canSwipeLeft && !canSwipeRight) {
            return 0; // Disable swiping in both directions
        } else if (!canSwipeLeft) {
            return ItemTouchHelper.RIGHT; // Enable only right swiping
        } else if (!canSwipeRight) {
            return ItemTouchHelper.LEFT; // Enable only left swiping
        }
        return swipeDirs; // Return the original directions if both are allowed
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        if (direction == ItemTouchHelper.LEFT) {
            mItemAdapter.deleteWrap(position);
        } else if (direction == ItemTouchHelper.RIGHT) {
            mItemAdapter.moveItemToTop(position);
        }
        mItemAdapter.notifyItemRangeChanged(0, mItemAdapter.getItemCount() - 1);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setTranslationX(0); // Reset translation to zero
        // Optionally, you can also reset other visual changes if needed
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        if (isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, true);
            icon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.trash);

            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20;

            int iconWidth = itemView.getHeight() / 3;
            int iconHeight = itemView.getHeight() / 3;
            int iconMargin = (itemView.getHeight() - iconHeight) / 2;

            int iconTop = itemView.getTop() + (itemView.getHeight() - iconHeight) / 2;
            int iconBottom = iconTop + iconHeight;

            if (dX < 0) { // Swiping to the left
                icon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.trash);
                paint.setColor(Color.RED);
                int iconLeft = itemView.getRight() - iconMargin - iconWidth;
                int iconRight = itemView.getRight() - iconMargin;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                float right = itemView.getRight() + (int) dX;
                float left = itemView.getRight() + dX - backgroundCornerOffset;

                if (left > right) {
                    left = right;
                }

                RectF background = new RectF(left - 100, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                c.drawRoundRect(background, 50, 50, paint);
            } else if (dX > 0) { // Swiping to the right
                icon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.top_top);
                paint.setColor(ContextCompat.getColor(recyclerView.getContext(), R.color.spotify_blue));
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = itemView.getLeft() + iconMargin + iconWidth;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                float left = itemView.getLeft() + (int) dX;
                float right = itemView.getLeft() + dX + backgroundCornerOffset;

                if (right < left) {
                    right = left;
                }

                RectF background = new RectF(itemView.getLeft(), itemView.getTop(), right + 100, itemView.getBottom());
                c.drawRoundRect(background, 50, 50, paint);
            }

            icon.draw(c);
        } else {
            viewHolder.itemView.setTranslationX(0);
            super.onChildDraw(c, recyclerView, viewHolder, 0, dY, actionState, false);
        }
    }
}