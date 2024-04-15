package com.example.spotifywrapped.ui.gallery.pages;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentTopArtistBinding;
import com.example.spotifywrapped.databinding.FragmentTopSongBinding;
import com.example.spotifywrapped.user.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class TopArtist extends Fragment {

    private FragmentTopArtistBinding binding;
    public static Context context;

    private static User currentUser;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }


    private User loadUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString("CurrentUser", null);
        Log.d("SharedPreferences", "Loaded token: " + userJson);
        return gson.fromJson(userJson, User.class);
    }

    private void setNameonTitle() {
        TextView titlesWithName = (TextView) binding.getRoot().findViewById(R.id.topSongIntro);
        String userName = (String) MainActivity.getCurrentUser().getName();
        if (userName != null && !userName.isEmpty()) {
            userName = userName.substring(0, userName.indexOf(" "));
        } else {
            userName = "User";
        }
        userName = userName + "'s ";
        titlesWithName.setText(userName + titlesWithName.getText());
    }

    private void setDefaultOnTitle() {
        TextView titlesWithName = (TextView) binding.getRoot().findViewById(R.id.topSongIntro);
        String userName = (String) MainActivity.getCurrentUser().getName();
        userName = "User";
        userName = userName + "'s ";
        titlesWithName.setText(userName + titlesWithName.getText());
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTopArtistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Assuming you have the current user's ID stored (e.g., as a field in the User object)
        FirebaseUser user = mAuth.getCurrentUser();

        if (!WrappedSummary.isPublicWrap()) {
            // Reference to the user's document in Firestore
            DocumentReference userRef = db.collection("Accounts").document(user.getUid());

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<Map<String, Object>> wrapList = (List<Map<String, Object>>) documentSnapshot.get("wraps");
                    if (wrapList != null) {
                        Map<String, Object> wrapData = wrapList.get(wrapList.size() - 1);
                        if (wrapData != null) {
                            Map<String, Object> wrap = wrapList.get(wrapList.size() - 1);
                            String name = (String) ((ArrayList<String>) wrap.get("artists")).get(0);
                            String image = (String) ((ArrayList<String>) wrap.get("artistsimage")).get(0);
                            TextView artistName = (TextView) root.findViewById(R.id.topartist);
                            artistName.setText(name);
                            ImageView topartistimage = (ImageView) root.findViewById(R.id.artistimage);
                            Glide.with(context)
                                    .load(image)
                                    .into(topartistimage);
                            setNameonTitle();
                        }
                    }
                } else {
                    Log.d("FIRESTORE", "No such document");
                }
            }).addOnFailureListener(e -> Log.d("FIRESTORE", "Error getting document", e));
        } else {
            DocumentReference userRef = db.collection("Accounts").document("bIQXuN4oAPUWGUx6ikPoDw1cjx62");

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<Map<String, Object>> wrapList = (List<Map<String, Object>>) documentSnapshot.get("wraps");
                    if (wrapList != null) {
                        Map<String, Object> wrapData = wrapList.get(WrappedSummary.getPublicWrapIndex());
                        if (wrapData != null) {
                            Map<String, Object> wrap = wrapList.get(WrappedSummary.getPublicWrapIndex());
                            String name = (String) ((ArrayList<String>) wrap.get("artists")).get(0);
                            String image = (String) ((ArrayList<String>) wrap.get("artistsimage")).get(0);
                            TextView artistName = (TextView) root.findViewById(R.id.topartist);
                            artistName.setText(name);
                            ImageView topartistimage = (ImageView) root.findViewById(R.id.artistimage);
                            Glide.with(context)
                                    .load(image)
                                    .into(topartistimage);
                            setDefaultOnTitle();
                        }
                    }
                } else {
                    Log.d("FIRESTORE", "No such document");
                }
            }).addOnFailureListener(e -> Log.d("FIRESTORE", "Error getting document", e));
        }

        // Set the click listener for the button
        binding.topartistnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_top5Artists);
            }
        });
        binding.topartistback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_top5Songs);
            }
        });
        binding.topartistexit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_gallery);

                // Show the toolbar
                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.show();
                    ImageView imageView = getActivity().findViewById(R.id.currentPageIcon);
                    imageView.setVisibility(View.VISIBLE);
                }
            }
        });


        binding.TASaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeAndSaveScreenShot();
            }
        });

        return root;
    }

    public void takeAndSaveScreenShot() {
        if (getActivity() != null) {
            final Activity currentActivity = getActivity();

            // hide elements we don't want in the image
            currentActivity.findViewById(R.id.topartistexit).setVisibility(View.GONE);
            currentActivity.findViewById(R.id.TAbottomBar).setVisibility(View.GONE);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                final View mainView = currentActivity.getWindow().getDecorView();
                final Window window = currentActivity.getWindow();

                // Create a bitmap for the part of the screen that needs to be captured
                Point size = new Point();
                int height = currentActivity.getWindow().getWindowManager().getCurrentWindowMetrics().getBounds().height();
                int width = currentActivity.getWindow().getWindowManager().getCurrentWindowMetrics().getBounds().width();
                final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                // Use PixelCopy to copy the screen content to the bitmap
                PixelCopy.request(window, bitmap, (copyResult) -> {
                    if (copyResult == PixelCopy.SUCCESS) {
                        try {
                            // Height to crop from the bottom
                            int heightToCrop = 0; // Set this to the height you want to crop

                            // New height for the cropped image
                            int newHeight = bitmap.getHeight() - heightToCrop;

                            // Create a new cropped Bitmap
                            Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), newHeight);

                            // Save the cropped bitmap to file (optional, only if you need a file copy)
                            File file = saveBitmapToFile(currentActivity, croppedBitmap, "MyWrappedImage", "Image description");

                            // Directly add the cropped Bitmap to the gallery
                            addImageToGallery(currentActivity, croppedBitmap, "MyWrappedImage", "Image description");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Handler(Looper.getMainLooper()));

                // Restore visibility
                currentActivity.findViewById(R.id.topartistexit).setVisibility(View.VISIBLE);
                currentActivity.findViewById(R.id.TAbottomBar).setVisibility(View.VISIBLE);
            }, 100); // Delay in milliseconds
        }
    }

    private File saveBitmapToFile(Context context, Bitmap bitmap, String title, String description) throws IOException {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        File file = new File(path, title + ".png");
        try (OutputStream out = Files.newOutputStream(file.toPath())) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
        }
        return file;
    }

    private void addImageToGallery(Context context, Bitmap bitmap, String title, String description) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri item = context.getContentResolver().insert(collection, values);

        if (item != null) {
            try (OutputStream out = context.getContentResolver().openOutputStream(item)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            context.getContentResolver().update(item, values, null, null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}