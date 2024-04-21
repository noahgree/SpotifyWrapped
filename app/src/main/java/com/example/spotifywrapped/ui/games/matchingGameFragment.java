package com.example.spotifywrapped.ui.games;

import static android.content.ContentValues.TAG;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
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
    private FragmentMatchingGameBinding binding;
    private Context context;

    private ImageView[] albumTiles = new ImageView[16];
    private String[] albumTileIds = new String[16];

    private TextView scoreTextView;

    private int firstTileIndex = -1;
    private int secondTileIndex = -1;

    private int matchedPairs = 0;
    public static int score = 0;
    public static boolean hasPlayed = false;

    private TextView timerTextView;
    private int seconds = 0;
    public static String time = "00:00";
    private boolean running = true; // Set this to false if you want the timer to be stopped initially

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int minutes = (seconds % 3600) / 60;
            int secs = seconds % 60;
            time = String.format("%02d:%02d", minutes, secs);
            timerTextView.setText(time);
            if (running) {
                seconds++;
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.getInstance();
        setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
        setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_slide_left));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMatchingGameBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // set insets
        FrameLayout mainLayout = root.findViewById(R.id.mgMainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            if (insets.bottom > 0) {
                mlp.bottomMargin = insets.bottom;
                v.setLayoutParams(mlp);
            }

            return WindowInsetsCompat.CONSUMED;
        });

        List<String> imageUrls = getArguments().getStringArrayList("imageUrls");
        scoreTextView = root.findViewById(R.id.scoreValue);

        FrameLayout matchTextBG = binding.getRoot().findViewById(R.id.matchTextBG);
        matchTextBG.setVisibility(View.INVISIBLE);

        for (int i = 0; i < albumTiles.length; i++) {
            String imageViewId = "albumtile" + (i + 1);
            int resId = getResources().getIdentifier(imageViewId, "id", requireActivity().getPackageName());
            albumTiles[i] = root.findViewById(resId);
            albumTiles[i].setOnClickListener(this);
        }
        time = "00:00";
        score = 0;

        setTileIds(imageUrls);
        scoreTextView.setText(String.valueOf(score));

        timerTextView = root.findViewById(R.id.timerMG);
        handler.post(runnable);
        running = true;

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
        FrameLayout matchTextBG = binding.getRoot().findViewById(R.id.matchTextBG);
        matchTextBG.setVisibility(View.VISIBLE);

        if (firstTileIndex != -1 && secondTileIndex != -1) {
            String firstImageUrl = getImageUrl(firstTileIndex);
            String secondImageUrl = getImageUrl(secondTileIndex);

            if (firstImageUrl != null && firstImageUrl.equals(secondImageUrl)) {
                // animations
                pulsateTile(albumTiles[firstTileIndex]);
                pulsateTile(albumTiles[secondTileIndex]);

                // score update
                score += 150;
                scoreTextView.setText(String.valueOf(score));
                TextView matchText = binding.getRoot().findViewById(R.id.matchgametext);
                matchText.setText("Match");
                matchText.setTextColor(ContextCompat.getColor(context, R.color.spotify_lighter_green));
                matchTextBG.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.spotify_light_green)));
                animatePointsAdded();

                // prevent clicking on tiles after matched
                albumTiles[firstTileIndex].setOnClickListener(null);
                albumTiles[secondTileIndex].setOnClickListener(null);

                matchedPairs++;

                if (matchedPairs == albumTiles.length / 2) {
                    gameCompletion();
                }

                // reset indices
                firstTileIndex = -1;
                secondTileIndex = -1;
            } else {
                TextView matchText = binding.getRoot().findViewById(R.id.matchgametext);
                matchTextBG = binding.getRoot().findViewById(R.id.matchTextBG);
                matchText.setText("No Match");
                matchText.setTextColor(ContextCompat.getColor(context, R.color.spotify_light_red));
                matchTextBG.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.spotify_red)));
                animatePointsDeducted();

                if (score > 0) {
                    score -= 50;
                    scoreTextView.setText(String.valueOf(score));
                }

                new CountDownTimer(750, 750) {
                    public void onTick(long millisUntilFinished) {
                        // Not used but required
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
                            .load((imageUrl.equals("default")) ? R.drawable.no_image : imageUrl)
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

                tile.setImageResource(R.drawable.matching_placeholder);

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
            scoreTextView.setTextColor(ContextCompat.getColor(context, R.color.spotify_black));

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
            scoreTextView.setTextColor(ContextCompat.getColor(context, R.color.spotify_red));

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
                    scoreTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.spotify_black));
                }
            });
        }
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
        hasPlayed = true;
        running = false;

        new CountDownTimer(2500, 1000) {
            public void onTick(long millisUntilFinished) {
                //required
            }

            public void onFinish() { // When the delay timer ends
                matchingHomeFragment.publishResults();
                NavController navController = Navigation.findNavController(requireView());
                navController.popBackStack(R.id.matchingHomeFragment, false);
            }
        }.start();
    }

    public static int getScore() {
        return score;
    }

    public static boolean getHasPlayed() {
        return hasPlayed;
    }

    public static String getTime() {
        return time;
    }
}
