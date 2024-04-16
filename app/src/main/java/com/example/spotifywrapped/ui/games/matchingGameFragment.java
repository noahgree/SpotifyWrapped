package com.example.spotifywrapped.ui.games;

import static android.content.ContentValues.TAG;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentMatchingGameBinding;
import com.example.spotifywrapped.ui.gallery.AddWrapFragment;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link matchingGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class matchingGameFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FragmentMatchingGameBinding binding;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public static Context context;

    ImageView[] albumTiles = new ImageView[16];

    int[] albumTileIds = new int[16];

    int firstTileIndex = -1;
    int secondTileIndex = -1;

    public matchingGameFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment matchingGameFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static matchingGameFragment newInstance(String param1, String param2) {
        matchingGameFragment fragment = new matchingGameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        context = MainActivity.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMatchingGameBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        List<String> imageUrls = getArguments().getStringArrayList("imageUrls");


        for (int i = 0; i < albumTiles.length; i++) {
            String imageViewId = "albumtile" + (i + 1);
            int resId = getResources().getIdentifier(imageViewId, "id", requireActivity().getPackageName());
            albumTiles[i] = root.findViewById(resId);
            albumTiles[i].setOnClickListener(this);
        }

        if (imageUrls != null) {
            setTileImages(imageUrls);
        } else {
            Log.e(TAG, "No image URLs provided.");
        }

        // Inflate the layout for this fragment
        return root;
    }

    public void onClick(View v) {
        for (int i = 0; i < albumTiles.length; i++) {
            if (v == albumTiles[i]) {
                if (firstTileIndex == -1) {
                    firstTileIndex = i;
                    flipTile(albumTiles[i], i);
                } else if (secondTileIndex == -1) {
                    secondTileIndex = i;
                    flipTile(albumTiles[i], i);
                    checkTiles();
                }
            }
        }
    }

    private void checkTiles() {
        String firstImageUrl = getImageUrl(firstTileIndex);
        String secondImageUrl = getImageUrl(secondTileIndex);

        if (firstImageUrl != null && firstImageUrl.equals(secondImageUrl)) {
            Toast.makeText(requireContext(), "Match!", Toast.LENGTH_SHORT).show();
        } else {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                flipTile(albumTiles[firstTileIndex], firstTileIndex);
                flipTile(albumTiles[secondTileIndex], secondTileIndex);
            }, 1000);
        }

        firstTileIndex = -1;
        secondTileIndex = -1;
    }

    private String getImageUrl(int tileIndex) {
        if (tileIndex >= 0 && tileIndex < albumTiles.length) {
            if (albumTileIds[tileIndex] != 0) {
                return String.valueOf(albumTileIds[tileIndex]);
            }
        }
        return null;
    }

    private void flipTile(ImageView imageView, int tileIndex) {
        ObjectAnimator flipOut = ObjectAnimator.ofFloat(imageView, "rotationY", 0f, 90f);
        flipOut.setDuration(200);
        flipOut.start();

        flipOut.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                String imageUrl = getImageUrl(tileIndex);
                if (imageUrl != null) {
                    Glide.with(requireContext())
                            .load(imageUrl)
                            .into(imageView);
                }

                ObjectAnimator flipIn = ObjectAnimator.ofFloat(imageView, "rotationY", -90f, 0f);
                flipIn.setDuration(200);
                flipIn.start();
            }
        });
    }

    public void setTileImages(List<String> imageUrls) {
        Collections.shuffle(imageUrls);

        for (int i = 0; i < albumTiles.length && i < imageUrls.size(); i++) {
            Glide.with(requireContext())
                    .load(imageUrls.get(i))
                    .into(albumTiles[i]);
            albumTileIds[i] = imageUrls.get(i).hashCode();
        }
    }


}