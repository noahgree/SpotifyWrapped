package com.example.spotifywrapped.ui.games;

import static android.content.ContentValues.TAG;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.os.Handler;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
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

    private TextView scoreTextView;

    private int firstTileIndex = -1;
    private int secondTileIndex = -1;

    private int matchedPairs = 0;
    private int score = 0;

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
        setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
        setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
        context = MainActivity.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMatchingGameBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        List<String> imageUrls = getArguments().getStringArrayList("imageUrls");
        scoreTextView = root.findViewById(R.id.scoreValue);

        for (int i = 0; i < albumTiles.length; i++) {
            String imageViewId = "albumtile" + (i + 1);
            int resId = getResources().getIdentifier(imageViewId, "id", requireActivity().getPackageName());
            albumTiles[i] = root.findViewById(resId);
            albumTiles[i].setOnClickListener(this);
        }

        setTileIds(imageUrls);
        scoreTextView.setText(String.valueOf(score));

        // Inflate the layout for this fragment
        return root;
    }

    public void onClick(View v) {
        for (int i = 0; i < albumTiles.length; i++) {
            if (v == albumTiles[i]) {
                if (firstTileIndex == -1) {
                    firstTileIndex = i;
                    flipTile(albumTiles[i], i);
                } else if (secondTileIndex == -1 && i != firstTileIndex) {
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
                showShortToast(requireContext(), "Match! +150", 250);

                //animations
                pulsateTile(albumTiles[firstTileIndex]);
                pulsateTile(albumTiles[secondTileIndex]);
                albumTiles[firstTileIndex].setBackgroundResource(R.drawable.highlighted_tile);
                albumTiles[secondTileIndex].setBackgroundResource(R.drawable.highlighted_tile);

                //score update
                score += 150;
                scoreTextView.setText(String.valueOf(score));
                animatePointsAdded();

                //prevent clicking on tiles after matched
                albumTiles[firstTileIndex].setOnClickListener(null);
                albumTiles[secondTileIndex].setOnClickListener(null);

                matchedPairs++;

                if (matchedPairs == albumTiles.length / 2) {
                    gameCompletion();
                }

                //reset indices
                firstTileIndex = -1;
                secondTileIndex = -1;
            } else {
                if (score > 0) {
                    showShortToast(requireContext(), "No Match! -50", 250);
                    score -= 50;
                    scoreTextView.setText(String.valueOf(score));
                    animatePointsDeducted();
                } else {
                    showShortToast(requireContext(), "No Match!", 250);
                }


                new CountDownTimer(750, 750) {
                    public void onTick(long millisUntilFinished) {
                        //Not used but required
                    }

                    public void onFinish() {
                        flipTileBack(albumTiles[firstTileIndex]);
                        flipTileBack(albumTiles[secondTileIndex]);

                        firstTileIndex = -1;
                        secondTileIndex = -1;
                    }
                }.start();
            }
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

    private void flipTileBack(ImageView tile) {
        ObjectAnimator flipOut = ObjectAnimator.ofFloat(tile, "rotationY", 0f, 90f);
        flipOut.setDuration(200);
        flipOut.start();

        flipOut.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                tile.setImageResource(R.drawable.tile_back);

                ObjectAnimator flipIn = ObjectAnimator.ofFloat(tile, "rotationY", -90f, 0f);
                flipIn.setDuration(200);
                flipIn.start();
            }
        });
    }

    private void pulsateTile(View view) {
        // Scale up and down animation for pulsating effect
        ObjectAnimator scaleUp = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.1f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.1f));
        scaleUp.setDuration(500);
        scaleUp.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1.1f, 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.1f, 1.0f));
        scaleDown.setDuration(500);
        scaleDown.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet pulse = new AnimatorSet();
        pulse.playSequentially(scaleUp, scaleDown);
        pulse.start();
    }

    private void animatePointsAdded() {

        int originalColor = scoreTextView.getCurrentTextColor();

        if (scoreTextView != null) {
            scoreTextView.setTextColor(Color.parseColor("#1DB954"));

            ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(scoreTextView, "scaleX", 1.2f);
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(scoreTextView, "scaleY", 1.2f);
            AnimatorSet scaleUp = new AnimatorSet();
            scaleUp.playTogether(scaleUpX, scaleUpY);
            scaleUp.setDuration(300);

            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(scoreTextView, "scaleX", 1.0f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(scoreTextView, "scaleY", 1.0f);
            AnimatorSet scaleDown = new AnimatorSet();
            scaleDown.playTogether(scaleDownX, scaleDownY);
            scaleDown.setDuration(300);
            scaleDown.setStartDelay(300);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(scaleUp, scaleDown);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    scoreTextView.setTextColor(originalColor);
                }
            });
            animatorSet.start();
        }
    }

    private void animatePointsDeducted() {
        if (scoreTextView != null) {
            scoreTextView.setTextColor(Color.parseColor("#D70040"));

            ObjectAnimator shakeX = ObjectAnimator.ofFloat(scoreTextView, "translationX", -10, 10);
            shakeX.setRepeatCount(5);
            shakeX.setDuration(100);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(shakeX);
            animatorSet.start();

            shakeX.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    scoreTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                }
            });
        }
    }

    private void showShortToast(Context context, String message, int duration) {
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
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

    private void gameCompletion() {
        Toast.makeText(requireContext(), "Congratulations! You've matched all tiles!", Toast.LENGTH_LONG).show();

        new CountDownTimer(5500, 1000) {
            public void onTick(long millisUntilFinished) {
                //required
            }

            public void onFinish() {
                NavController navController = Navigation.findNavController(requireView());
                navController.popBackStack(R.id.matchingHomeFragment3, false);
            }
        }.start();
    }

}
