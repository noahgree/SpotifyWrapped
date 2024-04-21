package com.example.spotifywrapped.ui.games;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentHangmanGameBinding;
import com.example.spotifywrapped.databinding.FragmentMatchingGameBinding;

public class hangmanGameFragment extends Fragment {
    private FragmentHangmanGameBinding binding;
    private Context context;

    private TextView textViewWordToGuess;
    private TextView textViewHangman;
    private EditText editTextGuess;
    private Button buttonSubmitGuess;

    private String[] words = new String[10];
    private String wordToGuess;
    private StringBuilder guessedWord;
    private StringBuilder wrongGuesses;

    private int maxWrongGuesses = 6;
    private int wrongGuessCount = 0;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHangmanGameBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // set insets
        FrameLayout mainLayout = root.findViewById(R.id.mainHangmanLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            if (insets.bottom > 0) {
                mlp.bottomMargin = insets.bottom;
                v.setLayoutParams(mlp);
            }

            return WindowInsetsCompat.CONSUMED;
        });

        List<String> artists = getArguments().getStringArrayList("artists");
        setWords(artists);

        wrongGuessCount = 0;

        textViewWordToGuess = root.findViewById(R.id.wordToGuessField);
        textViewHangman = root.findViewById(R.id.guessCounterHM);
        editTextGuess = root.findViewById(R.id.guessFieldHM);
        buttonSubmitGuess = root.findViewById(R.id.hangmanSubmitBtn);

        buttonSubmitGuess.setOnClickListener(v -> submitGuess());

        editTextGuess.getText().clear();

        if (savedInstanceState != null) {
            wordToGuess = savedInstanceState.getString("wordToGuess");
            guessedWord = new StringBuilder(savedInstanceState.getString("guessedWord"));
            wrongGuesses = new StringBuilder(savedInstanceState.getString("wrongGuesses"));
            wrongGuessCount = savedInstanceState.getInt("wrongGuessCount");
            updateGameUI();
        } else {
            initializeGame();
        }

        timerTextView = root.findViewById(R.id.timerHM);
        handler.post(runnable);
        running = true;

        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("wordToGuess", wordToGuess);
        outState.putString("guessedWord", guessedWord.toString());
        outState.putString("wrongGuesses", wrongGuesses.toString());
        outState.putInt("wrongGuessCount", wrongGuessCount);
    }

    private void submitGuess() {
        String guess = editTextGuess.getText().toString().toUpperCase();

        if (guess.length() != 1 || (!Character.isAlphabetic(guess.charAt(0)) && !Character.isDigit(guess.charAt(0)))) {
            Toast.makeText(getActivity(), "Please enter a single letter.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (guessedWord.indexOf(guess) >= 0 || wrongGuesses.indexOf(guess) >= 0) {
            Toast.makeText(getActivity(), "You've already guessed this letter.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (wordToGuess.contains(guess)) {
            for (int i = 0; i < wordToGuess.length(); i++) {
                if (wordToGuess.charAt(i) == guess.charAt(0)) {
                    guessedWord.setCharAt(i, guess.charAt(0));
                }
            }
            if (guessedWord.toString().equals(wordToGuess)) {
                completeGame();
                return;
            }
        } else {
            wrongGuessCount++;
            updateHangmanImage();
            if (wrongGuessCount == maxWrongGuesses) {
                wrongGuesses.append(guess);
                Toast.makeText(getActivity(), "Game Over! The word was: " + wordToGuess, Toast.LENGTH_SHORT).show();
                initializeGame();
                endGameInLoss();
                return;
            } else {
                wrongGuesses.append(", " + guess);
            }
        }

        editTextGuess.getText().clear();
        updateGameUI();
    }

    private void completeGame() {
        hasPlayed = true;
        running = false;

        new CountDownTimer(2500, 1000) {
            public void onTick(long millisUntilFinished) {
                //required
            }

            public void onFinish() { // When the delay timer ends
                editTextGuess.getText().clear();
                hangmanHomeFragment.setUpResults();
                initializeGame();
                NavController navController = Navigation.findNavController(requireView());
                navController.popBackStack(R.id.navHangmanHome, false);
                hangmanHomeFragment.setUpResults();
            }
        }.start();
    }

    private void endGameInLoss() {
        hasPlayed = true;
        running = false;

        new CountDownTimer(2500, 1000) {
            public void onTick(long millisUntilFinished) {
                //required
            }

            public void onFinish() { // When the delay timer ends
                NavController navController = Navigation.findNavController(requireView());
                navController.popBackStack(R.id.navHangmanHome, false);
                hangmanHomeFragment.setUpResults();
            }
        }.start();
    }

    private void initializeGame() {
        Random random = new Random();
        int randomIndex = random.nextInt(words.length);
        wordToGuess = words[randomIndex];
        guessedWord = new StringBuilder();
        wrongGuesses = new StringBuilder();
        for (int i = 0; i < wordToGuess.length(); i++) {
            guessedWord.append("_");
        }
        updateGameUI();
    }

    private void updateGameUI() {
        updateHangmanImage();
        textViewWordToGuess.setText(guessedWord.toString());
        textViewHangman.setText("WRONG\nGUESSES:\n\n" + wrongGuesses);
    }

    private void updateHangmanImage() {
        ImageView statusImage = binding.getRoot().findViewById(R.id.hangmanStatusImage);
        if (wrongGuessCount == 0) {
            statusImage.setImageResource(R.color.spotify_black);
        } else if (wrongGuessCount == 1) {
            statusImage.setImageResource(R.drawable.person_phase_1);
        } else if (wrongGuessCount == 2) {
            statusImage.setImageResource(R.drawable.person_phase_2);
        } else if (wrongGuessCount == 3) {
            statusImage.setImageResource(R.drawable.person_phase_3);
        } else if (wrongGuessCount == 4) {
            statusImage.setImageResource(R.drawable.person_phase_4);
        } else if (wrongGuessCount == 5) {
            statusImage.setImageResource(R.drawable.person_phase_5);
        } else if (wrongGuessCount == 6) {
            statusImage.setImageResource(R.drawable.person_phase_6);
        }
    }

    public void setWords(List<String> artists) {
        Collections.shuffle(artists);

        List<String> pairArtists = new ArrayList<>(artists);
        pairArtists.addAll(artists);

        Collections.shuffle(pairArtists);

        for (int i = 0; i < words.length && i < pairArtists.size(); i++) {
            words[i] = pairArtists.get(i).toUpperCase().replaceAll(" ", "");
        }
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
