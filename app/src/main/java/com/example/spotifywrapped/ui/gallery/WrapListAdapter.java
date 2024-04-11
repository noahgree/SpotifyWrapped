package com.example.spotifywrapped.ui.gallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifywrapped.R;
import com.example.spotifywrapped.ui.gallery.pages.TopSong;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class WrapListAdapter extends RecyclerView.Adapter<WrapListAdapter.MyViewHolder> {
    Context context;
    private final ArrayList<WrapObject> WrapObjectArrayList;
    public WrapListAdapter(Context context, ArrayList<WrapObject> WrapObjectArrayList) {
        this.context = context;
        this.WrapObjectArrayList = WrapObjectArrayList;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_wrap,parent,false);

        return new MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        WrapObject eventSelected = WrapObjectArrayList.get(position);
        /*holder.selectedDate.setText(eventSelected.getLocation() + " - " + eventSelected.getSelectedTime());
        holder.type.setText(eventSelected.getType());
        holder.className.setText(eventSelected.getClassName());
        //Removing delete button for specific events
        if(eventSelected.isTask() || eventSelected.isClass()) {
            holder.deleteButton.setVisibility(View.GONE);
        } else {
            holder.deleteButton.setVisibility(View.VISIBLE);
        }*/
    }

    @Override
    public int getItemCount() {
        return WrapObjectArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView accountName;
        TextView artistName;
        TextView songName;
        ImageView songImage;
        ImageView artistImage;
        CardView WrapView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            accountName = itemView.findViewById(R.id.accountName);
            //type = itemView.findViewById(R.id.timeFrame);
            artistName = itemView.findViewById(R.id.textView6);
            songName = itemView.findViewById(R.id.textView7);
            songImage = itemView.findViewById(R.id.imageView2);
            artistImage = itemView.findViewById(R.id.imageView3);
            WrapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        WrapObject selectedEvent = WrapObjectArrayList.get(position);
                            // Switching Views
                            TopSong newFragment = TopSong.newInstance(selectedEvent);
                        NavController navController = Navigation.findNavController(v);
                        navController.navigate(R.id.nav_topSong);
                    }
                }
            });
        }
    }
}
