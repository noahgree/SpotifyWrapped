package com.example.spotifywrapped.ui.games;

import static android.content.ContentValues.TAG;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentMatchingGameBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class matchingGameFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FragmentMatchingGameBinding binding;

    private String mParam1;
    private String mParam2;

    private Context context;

    private ImageView[] albumTiles = new ImageView[16];

    private String[] albumTileIds = new String[16];

    private int firstTileIndex = -1;
    private int secondTileIndex = -1;

    public matchingGameFragment() {
        // Required empty public constructor
    }

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

        setTileIds(imageUrls);

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
        if (firstTileIndex != -1 && secondTileIndex != -1) {
            String firstImageUrl = getImageUrl(firstTileIndex);
            String secondImageUrl = getImageUrl(secondTileIndex);

            if (firstImageUrl != null && firstImageUrl.equals(secondImageUrl)) {
                Toast.makeText(requireContext(), "Match!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "No match!", Toast.LENGTH_SHORT).show();

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (firstTileIndex >= 0 && firstTileIndex < albumTiles.length &&
                            secondTileIndex >= 0 && secondTileIndex < albumTiles.length) {
                        flipTile(albumTiles[firstTileIndex], firstTileIndex);
                        flipTile(albumTiles[secondTileIndex], secondTileIndex);
                    }
                }, 1000);
            }

            firstTileIndex = -1;
            secondTileIndex = -1;
        }
    }




    private String getImageUrl(int tileIndex) {
        if (tileIndex >= 0 && tileIndex < albumTiles.length) {
            if (albumTileIds[tileIndex] != null) {
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

                // Set the image of the tile only when flipped
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

    public void setTileIds(List<String> imageUrls) {
        Collections.shuffle(imageUrls);

        List<String> pairImageUrls = new ArrayList<>(imageUrls);
        pairImageUrls.addAll(imageUrls);

        Collections.shuffle(pairImageUrls);

        for (int i = 0; i < albumTiles.length && i < pairImageUrls.size(); i++) {
            albumTileIds[i] = pairImageUrls.get(i);
        }
    }

}
