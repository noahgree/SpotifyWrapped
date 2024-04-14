package com.example.spotifywrapped.ui.gallery;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.spotifywrapped.MainActivity;
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
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("WRAP", "Existing wrapped clicked");
            }
        });
        return new WrapViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(WrapViewHolder holder, int position) {
        WrapObject wrap = wraps.get(position);
        holder.nameTextView.setText(wrap.getName());
        holder.artistTextView.setText(wrap.getArtistName());
        holder.songTextView.setText(wrap.getSongName());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionBar actionBar = ((AppCompatActivity) MainActivity.getInstance()).getSupportActionBar();
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                if (actionBar != null) {
                    actionBar.hide();
                    ImageView imageView = activity.findViewById(R.id.currentPageIcon);
                    imageView.setVisibility(View.GONE);
                }
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_topSong);
            }
        });

        Glide.with(context).load(wrap.getArtistImage()).into(holder.artistImageView);
        Glide.with(context).load(wrap.getSongImage()).into(holder.songImageView);
        // Assume images are loaded somehow, possibly with an image loading library
    }

    public void addWrap(WrapObject newWrap) {
        wraps.add(newWrap);  // `wraps` is the list in the adapter
        notifyItemInserted(wraps.size() - 1);
    }

    @Override
    public int getItemCount() {
        return wraps.size();
    }

    public static class WrapViewHolder extends RecyclerView.ViewHolder {
        public View cardView;
        public TextView nameTextView, artistTextView, songTextView;
        public ImageView artistImageView, songImageView;

        public WrapViewHolder(View itemView) {
            super(itemView);
            cardView = itemView;
            nameTextView = itemView.findViewById(R.id.wrapName);
            artistImageView = itemView.findViewById(R.id.album1);
            songImageView = itemView.findViewById(R.id.album2);
            artistTextView = itemView.findViewById(R.id.albumName1);
            songTextView = itemView.findViewById(R.id.albumName2);
        }
    }
}
