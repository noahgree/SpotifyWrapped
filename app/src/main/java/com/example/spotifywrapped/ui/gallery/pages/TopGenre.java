package com.example.spotifywrapped.ui.gallery.pages;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentTop5ArtistsBinding;
import com.example.spotifywrapped.databinding.FragmentTopGenreBinding;
import com.example.spotifywrapped.ui.CustomSnackbar;
import com.example.spotifywrapped.user.User;
import com.google.android.material.snackbar.Snackbar;
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
public class TopGenre extends Fragment {

    private static FragmentTopGenreBinding binding;
    public static Context context;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
        setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
    }

    private User loadUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString("CurrentUser", null);
        Log.d("SharedPreferences", "Loaded token: " + userJson);
        return gson.fromJson(userJson, User.class);
    }

    public static void setNameonTitle(String username) {
        TextView titlesWithName = binding.getRoot().findViewById(R.id.topSongIntro);
        if (!username.isEmpty()) {
            username = username.substring(0, username.indexOf(" "));
        } else {
            username = "User";
        }
        username = username + "'s ";
        titlesWithName.setText(username + titlesWithName.getText());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTopGenreBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = MainActivity.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // set insets
        FrameLayout mainLayout = root.findViewById(R.id.topGenreLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            if (insets.bottom > 0) {
                mlp.bottomMargin = insets.bottom;
                v.setLayoutParams(mlp);
            }

            return WindowInsetsCompat.CONSUMED;
        });

        // Assuming you have the current user's ID stored (e.g., as a field in the User object)
        FirebaseUser user = mAuth.getCurrentUser();

        if (!WrappedSummary.isPublicWrap()) {
            // Reference to the user's document in Firestore
            DocumentReference userRef = db.collection("Accounts").document(user.getUid());

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<Map<String, Object>> wrapList = (List<Map<String, Object>>) documentSnapshot.get("wraps");
                    if (wrapList != null) {
                        Map<String, Object> wrapData = wrapList.get(WrappedSummary.getPrivateWrapIndex());
                        if (wrapData != null) {
                            Map<String, Object> wrap = wrapList.get(WrappedSummary.getPrivateWrapIndex());
                            Log.d("testW", (wrap).toString());
                            String genre = ((ArrayList<String>) wrap.get("artistsgenre")).get(0);
                            String image = ((ArrayList<String>) wrap.get("artistsimage")).get(0);
                            String username = (String) wrapData.get("username");
                            TextView artistName = root.findViewById(R.id.topgenre);
                            genre = capitalizeWords(genre);
                            artistName.setText(genre);
                            ImageView topartistimage = root.findViewById(R.id.genreimage);
                            Glide.with(context)
                                    .load(image)
                                    .into(topartistimage);
                            setNameonTitle(username);
                        }
                    }
                } else {
                    Log.d("FIRESTORE", "No such document");
                }
            }).addOnFailureListener(e -> Log.d("FIRESTORE", "Error getting document", e));
        } else {
            DocumentReference userRef = db.collection("Accounts").document("vGLXVzArF0OObsE5bJT4jNpdOy33");

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<Map<String, Object>> wrapList = (List<Map<String, Object>>) documentSnapshot.get("wraps");
                    if (wrapList != null) {
                        Map<String, Object> wrapData = wrapList.get(WrappedSummary.getPublicWrapIndex());
                        if (wrapData != null) {
                            Map<String, Object> wrap = wrapList.get(WrappedSummary.getPublicWrapIndex());
                            String genre = ((ArrayList<String>) wrap.get("artistsgenre")).get(0);
                            String image = ((ArrayList<String>) wrap.get("artistsimage")).get(0);
                            String username = (String) wrapData.get("username");
                            TextView artistName = root.findViewById(R.id.topgenre);
                            genre = capitalizeWords(genre);
                            artistName.setText(genre);
                            ImageView topartistimage = root.findViewById(R.id.genreimage);
                            Glide.with(context)
                                    .load(image)
                                    .into(topartistimage);
                            setNameonTitle(username);
                        }
                    }
                } else {
                    Log.d("FIRESTORE", "No such document");
                }
            }).addOnFailureListener(e -> Log.d("FIRESTORE", "Error getting document", e));
        }

        // Set the click listener for the button
        binding.topgenrenext.setOnClickListener(v -> {
            setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_slide_left));
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_wrappedSummary);
        });
        binding.topgenreback.setOnClickListener(v -> {
            setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_slide_right));
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_top5Artists);
        });
        binding.topgenreexit.setOnClickListener(v -> {
            TransitionSet exitTransitionSet = new TransitionSet();
            exitTransitionSet.addTransition(new Slide(Gravity.END));
            exitTransitionSet.addTransition(new Fade());
            exitTransitionSet.setDuration(300);
            setExitTransition(exitTransitionSet);

            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_gallery);

            // Show the toolbar with animation
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
                int resId = getResources().getIdentifier("action_bar_container", "id", "android");


                // Load the fade-in animation
                Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);

                View actionBarContainer = getActivity().findViewById(resId);
                if (actionBarContainer != null) {
                    actionBarContainer.startAnimation(fadeIn);
                }

                // Animate the ImageView
                ImageView imageView = getActivity().findViewById(R.id.currentPageIcon);
                imageView.setVisibility(View.VISIBLE);
                imageView.startAnimation(fadeIn);
            }
        });


        binding.TGSaveImage.setOnClickListener(v -> takeAndSaveScreenShot());

        MainActivity.updateForHoliday(binding);

        return root;
    }

    public void takeAndSaveScreenShot() {
        if (getActivity() != null) {
            final Activity currentActivity = getActivity();

            // hide elements we don't want in the image
            currentActivity.findViewById(R.id.topgenreexit).setVisibility(View.GONE);
            currentActivity.findViewById(R.id.TGbottombar).setVisibility(View.GONE);

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
                currentActivity.findViewById(R.id.topgenreexit).setVisibility(View.VISIBLE);
                currentActivity.findViewById(R.id.TGbottombar).setVisibility(View.VISIBLE);
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

            ViewGroup rootView = (ViewGroup) ((ViewGroup) binding.getRoot().findViewById(R.id.topGenreLayout)).getChildAt(0);

            CustomSnackbar.showCustomSnackbar(getActivity(), rootView, "Image Saved");
        }
    }

    public static String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder capitalized = new StringBuilder();
        boolean isWordStart = true; // Flag to identify the start of a word

        for (char ch : str.toCharArray()) {
            if (Character.isLetter(ch) && isWordStart) {
                capitalized.append(Character.toUpperCase(ch));
                isWordStart = false;
            } else {
                capitalized.append(ch);
            }

            // Reset flag if current character is not a letter
            if (!Character.isLetter(ch)) {
                isWordStart = true;
            }
        }

        return capitalized.toString();
    }

    private void showActionBar() {
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
            if (appCompatActivity.getSupportActionBar() != null) {
                appCompatActivity.getSupportActionBar().show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}