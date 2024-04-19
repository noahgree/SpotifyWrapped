package com.example.spotifywrapped.ui.gallery;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.ui.gallery.pages.WrappedSummary;
import com.example.spotifywrapped.user.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WrapAdapter extends RecyclerView.Adapter<WrapAdapter.WrapViewHolder> {
    private Context context;
    private ArrayList<WrapObject> wraps;
    private static User currentUser;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public WrapAdapter(Context context, ArrayList<WrapObject> wraps) {
        this.context = context;
        this.wraps = wraps;
    }

    @Override
    public WrapViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_wrap, parent, false);
        itemView.setOnClickListener(view -> Log.d("WRAP", "Existing wrapped clicked"));
        return new WrapViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(WrapViewHolder holder, int position) {
        WrapObject wrap = wraps.get(position);
        holder.nameTextView.setText(wrap.getName());
        holder.artistTextView.setText(wrap.getArtistName());
        holder.songTextView.setText(wrap.getSongName());
        holder.usernameTextView.setText("@" + wrap.getUsername());
        holder.timeFrameTextView.setText(wrap.getTimeFrame());
        holder.creationDateTextView.setText(wrap.getCreationDate());

        ArrayList<Map<String, Object>> wraps = MainActivity.getCurrentUser().getwraps();

        holder.cardView.setOnClickListener(v -> {
            ActionBar actionBar = MainActivity.getInstance().getSupportActionBar();
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            if (actionBar != null) {
                actionBar.hide();
                ImageView imageView = activity.findViewById(R.id.currentPageIcon);
                imageView.setVisibility(View.GONE);
            }
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_topSong);
            if (wrap.isPublicWrap()) {
                WrappedSummary.setPublicWrap(true);
                WrappedSummary.setPublicWrapIndex(position);
            } else {
                WrappedSummary.setPrivateWrapIndex(position);
                WrappedSummary.setPublicWrap(false);
            }
        });

        Glide.with(context).load(wrap.getArtistImage()).into(holder.artistImageView);
        Glide.with(context).load(wrap.getSongImage()).into(holder.songImageView);
    }


    public void addWrap(WrapObject newWrap) {
        wraps.add(newWrap);  // `wraps` is the list in the adapter
        notifyItemInserted(wraps.size() - 1);
    }

    public boolean isItemSwipable(int position, int type) {
        boolean canBeSwiped = false;
        WrapObject wrap = wraps.get(position);

        if (type == 0) { // for deleting
            canBeSwiped = !wrap.isPublicWrap();
        } else { // for moving  to top
            canBeSwiped = position > 0;
        }
        return canBeSwiped;
    }

    public void moveItemToTop(int position) {
        if (position > 0) {
            WrapObject wrap = wraps.remove(position);
            wraps.add(0, wrap);
            notifyItemMoved(position, 0);

            currentUser = loadUser();
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();

            DocumentReference userRef = db.collection("Accounts").document(user.getUid());

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<Map<String, Object>> wrapList = (List<Map<String, Object>>) documentSnapshot.get("wraps");
                    if (wrapList != null && wrapList.size() > position) {
                        Map<String, Object> itemToMove = wrapList.remove(position);
                        wrapList.add(0, itemToMove);

                        userRef.update("wraps", wrapList)
                                .addOnSuccessListener(aVoid -> Log.d("moving", "Wrap moved to top successfully"))
                                .addOnFailureListener(e -> Log.e("moving", "Error moving wrap to top", e));
                    } else {
                        Log.d("moving", "Position out of bounds or empty wraps array");
                    }
                } else {
                    Log.d("moving", "Document does not exist");
                }
            }).addOnFailureListener(e -> Log.e("moving", "Error fetching document", e));
        }
    }

    public void deleteWrap(int position) {
        wraps.remove(position);
        notifyItemRemoved(position);

        currentUser = loadUser();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // Reference to the user's document in Firestore
        DocumentReference userRef = db.collection("Accounts").document(user.getUid());
        DocumentReference publicRef = db.collection("Accounts").document("vGLXVzArF0OObsE5bJT4jNpdOy33");

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> wrapList = (List<Map<String, Object>>) documentSnapshot.get("wraps");
                if (wrapList != null && wrapList.size() > position) {
                    Object itemToRemove = wrapList.get(position);
                    userRef.update("wraps", FieldValue.arrayRemove(itemToRemove))
                            .addOnSuccessListener(aVoid -> Log.d("removing", "Private wrap removed from array successfully"))
                            .addOnFailureListener(e -> Log.e("removing", "Error removing private wrap from array", e));
                } else {
                    Log.d("removing", "Position out of bounds or empty wraps array");
                }
            } else {
                Log.d("removing", "Document does not exist");
            }
        }).addOnFailureListener(e -> Log.e("removing", "Error fetching document", e));
    }

    private User loadUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString("CurrentUser", null);
        Log.d("SharedPreferences", "Loaded token: " + userJson);
        return gson.fromJson(userJson, User.class);
    }

    @Override
    public int getItemCount() {
        return wraps.size();
    }

    public static class WrapViewHolder extends RecyclerView.ViewHolder {
        public View cardView;
        public TextView nameTextView, artistTextView, songTextView, usernameTextView, creationDateTextView, timeFrameTextView;
        public ImageView artistImageView, songImageView;

        public WrapViewHolder(View itemView) {
            super(itemView);
            cardView = itemView;
            nameTextView = itemView.findViewById(R.id.wrapName);
            artistImageView = itemView.findViewById(R.id.album1);
            songImageView = itemView.findViewById(R.id.album2);
            artistTextView = itemView.findViewById(R.id.albumName1);
            songTextView = itemView.findViewById(R.id.albumName2);
            usernameTextView = itemView.findViewById(R.id.usernameText);
            creationDateTextView = itemView.findViewById(R.id.dateCreatedText);
            timeFrameTextView = itemView.findViewById(R.id.timeFrameText);
        }
    }
}
