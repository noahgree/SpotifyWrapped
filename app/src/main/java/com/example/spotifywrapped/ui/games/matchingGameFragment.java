package com.example.spotifywrapped.ui.games;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentMatchingGameBinding;

import java.util.Arrays;
import java.util.Collections;

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

    ImageView[] albumTiles = new ImageView[16];

    int[] albumTileIds = {R.drawable.logo, R.drawable.logo, R.drawable.logo, R.drawable.logo,
            R.drawable.logo, R.drawable.logo, R.drawable.logo, R.drawable.logo,
            R.drawable.logo, R.drawable.logo, R.drawable.logo, R.drawable.logo,
            R.drawable.logo, R.drawable.logo, R.drawable.logo, R.drawable.logo};

    int firstTileIndex = -1;
    int secondTileIndex = -1;

    static String url;

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMatchingGameBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Collections.shuffle(Arrays.asList(albumTileIds));

        for (int i = 0; i < albumTiles.length; i++) {
            String imageViewId = "albumtile" + (i + 1);
            int resId = getResources().getIdentifier(imageViewId, "id", requireActivity().getPackageName());
            albumTiles[i] = root.findViewById(resId);
            albumTiles[i].setOnClickListener(this);
        }

        // Inflate the layout for this fragment
        return root;
    }

    public void onClick (View v) {
        for (int i = 0; i < albumTiles.length; i++) {
            if (v == albumTiles[i]) {
                if (firstTileIndex == -1) {
                    firstTileIndex = i;
                    flipTile(albumTiles[i], albumTileIds[i]);
                } else if (secondTileIndex == -1) {
                    secondTileIndex = i;
                    albumTiles[i].setImageResource((albumTileIds[i]));
                    flipTile(albumTiles[i], albumTileIds[i]);
                    checkTiles();
                }
            }
        }
    }

    private void checkTiles() {
        if (albumTileIds[firstTileIndex] == albumTileIds[secondTileIndex]) {
            Toast.makeText(requireContext(), "Match!", Toast.LENGTH_SHORT).show();
            firstTileIndex = -1;
            secondTileIndex = -1;
        } else {
            albumTiles[firstTileIndex].postDelayed(() -> {
                albumTiles[firstTileIndex].setImageResource(R.drawable.tile_back);
                albumTiles[secondTileIndex].setImageResource(R.drawable.tile_back);
                firstTileIndex = -1;
                secondTileIndex = -1;
            }, 1000);
        }
    }

    private void flipTile(ImageView imageView, int imageId) {
        ObjectAnimator flipOut = ObjectAnimator.ofFloat(imageView, "rotationY", 0f, 90f);
        flipOut.setDuration(200);
        flipOut.start();

        flipOut.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                imageView.setImageResource(imageId);

                ObjectAnimator flipIn = ObjectAnimator.ofFloat(imageView, "rotationY", -90f, 0f);
                flipIn.setDuration(200);
                flipIn.start();
            }
        });
    }

    private void populateTiles () {

    }
}