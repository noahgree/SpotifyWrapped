package com.example.spotifywrapped.ui.gallery.pages;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.opengl.Visibility;
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
import android.view.WindowMetrics;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentTopGenreBinding;
import com.example.spotifywrapped.databinding.FragmentWrappedSummaryBinding;
import com.example.spotifywrapped.user.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class WrappedSummary extends Fragment {

    private User loadUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString("CurrentUser", null);
        Log.d("SharedPreferences", "Loaded token: " + userJson);
        return gson.fromJson(userJson, User.class);
    }

    private FragmentWrappedSummaryBinding binding;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWrappedSummaryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Assuming you have the current user's ID stored (e.g., as a field in the User object)
        FirebaseUser user = mAuth.getCurrentUser();


        // Reference to the user's document in Firestore
        DocumentReference userRef = db.collection("Accounts").document(user.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> wrapList = (List<Map<String, Object>>) documentSnapshot.get("wraps");
                if (wrapList != null) {
                    Map<String, Object> wrapData = wrapList.get(wrapList.size() - 1);
                    if (wrapData != null) {
                        Map<String, Object> wrap = wrapList.get(wrapList.size() - 1);

                        // top song stuff
                        String name1 = (String) ((ArrayList<String>) wrap.get("tracks")).get(0);
                        String image1 = (String) ((ArrayList<String>) wrap.get("tracksimage")).get(0);
                        String name2 = (String) ((ArrayList<String>) wrap.get("tracks")).get(1);
                        String name3 = (String) ((ArrayList<String>) wrap.get("tracks")).get(2);
                        String name4 = (String) ((ArrayList<String>) wrap.get("tracks")).get(3);
                        String name5 = (String) ((ArrayList<String>) wrap.get("tracks")).get(4);
                        TextView songName = (TextView) root.findViewById(R.id.ttext1);
                        songName.setText(name1);
                        songName = (TextView) root.findViewById(R.id.ttext2);
                        songName.setText(name2);
                        songName = (TextView) root.findViewById(R.id.ttext3);
                        songName.setText(name3);
                        songName = (TextView) root.findViewById(R.id.ttext4);
                        songName.setText(name4);
                        songName = (TextView) root.findViewById(R.id.ttext5);
                        songName.setText(name5);
                        ImageView topsongimage = (ImageView) root.findViewById(R.id.leftImage1);
                        Glide.with(context)
                                .load(image1)
                                .into(topsongimage);

                        // top artist stuff
                        String Aname1 = (String) ((ArrayList<String>) wrap.get("artists")).get(0);
                        String Aimage1 = (String) ((ArrayList<String>) wrap.get("artistsimage")).get(0);
                        String Aname2 = (String) ((ArrayList<String>) wrap.get("artists")).get(1);
                        String Aname3 = (String) ((ArrayList<String>) wrap.get("artists")).get(2);
                        String Aname4 = (String) ((ArrayList<String>) wrap.get("artists")).get(3);
                        String Aname5 = (String) ((ArrayList<String>) wrap.get("artists")).get(4);

                        songName = (TextView) root.findViewById(R.id.btext1);
                        songName.setText(Aname1);
                        songName = (TextView) root.findViewById(R.id.btext2);
                        songName.setText(Aname2);
                        songName = (TextView) root.findViewById(R.id.btext3);
                        songName.setText(Aname3);
                        songName = (TextView) root.findViewById(R.id.btext4);
                        songName.setText(Aname4);
                        songName = (TextView) root.findViewById(R.id.btext5);
                        songName.setText(Aname5);
                        ImageView topartistimage = (ImageView) root.findViewById(R.id.leftImage2);
                        Glide.with(context)
                                .load(Aimage1)
                                .into(topartistimage);
                        setNameonTitle();
                    }
                }
            } else {
                Log.d("FIRESTORE", "No such document");
            }
        }).addOnFailureListener(e -> Log.d("FIRESTORE", "Error getting document", e));

        binding.wrappedsummaryback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_topGenre);
            }
        });
        binding.wrappedsummaryexit.setOnClickListener(new View.OnClickListener() {
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

        binding.wrappedSaveImage.setOnClickListener(new View.OnClickListener() {
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
            currentActivity.findViewById(R.id.wrappedsummaryexit).setVisibility(View.GONE);
            currentActivity.findViewById(R.id.wrappedSummaryTitle).setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            // show date text
            Date c = Calendar.getInstance().getTime();
            System.out.println("Current time => " + c);

            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
            TextView dateText = (TextView) binding.getRoot().findViewById(R.id.invisDateForSS);
            String dateString = currentDate.format(formatter);
            dateText.setText(dateString);
            currentActivity.findViewById(R.id.invisDateForSS).setVisibility(View.VISIBLE);

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
                currentActivity.findViewById(R.id.wrappedsummaryexit).setVisibility(View.VISIBLE);
                currentActivity.findViewById(R.id.invisDateForSS).setVisibility(View.GONE);
                currentActivity.findViewById(R.id.wrappedSummaryTitle).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
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

    private void setNameonTitle() {
        TextView titlesWithName = (TextView) binding.getRoot().findViewById(R.id.wrappedSummaryTitle);
        String userName = (String) MainActivity.getCurrentUser().getName();
        if (userName != null && !userName.isEmpty()) {
            userName = userName.substring(0, userName.indexOf(" "));
        } else {
            userName = "User";
        }
        userName = userName + "'s ";
        titlesWithName.setText(userName + titlesWithName.getText());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}