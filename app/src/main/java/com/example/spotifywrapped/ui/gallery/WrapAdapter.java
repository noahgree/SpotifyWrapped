package com.example.spotifywrapped.ui.gallery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.widget.TextView;

import com.example.spotifywrapped.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WrapAdapter extends RecyclerView.Adapter<WrapAdapter.WrapViewHolder> {
    private Context context;
    private ArrayList<WrapObject> wraps;

    public WrapAdapter(Context context, ArrayList<WrapObject> wraps) {
        this.context = context;
        this.wraps = wraps;
    }

    @Override
    public WrapViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_wrap, parent, false);
        return new WrapViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(WrapViewHolder holder, int position) {
        WrapObject wrap = wraps.get(position);
        holder.nameTextView.setText(wrap.getName());
        // Assume images are loaded somehow, possibly with an image loading library
    }

    @Override
    public int getItemCount() {
        return wraps.size();
    }

    public static class WrapViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ImageView artistImageView, songImageView;

        public WrapViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.wrapName);
            artistImageView = itemView.findViewById(R.id.album1);
            songImageView = itemView.findViewById(R.id.album2);
        }
    }
}
