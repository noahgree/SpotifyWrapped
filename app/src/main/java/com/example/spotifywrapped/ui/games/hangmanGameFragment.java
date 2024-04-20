package com.example.spotifywrapped.ui.games;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Random;
import com.example.spotifywrapped.R;

public class hangmanGameFragment extends Fragment {

    private ImageView imageViewHangman;
    private TextView textViewWordToGuess;
    private EditText editTextGuess;
    private Button buttonSubmitGuess;

    private String[] words = {"ANDROID", "STUDIO", "GRADLE", "HANGMAN", "SPOTIFY"};
    private String wordToGuess;
    private StringBuilder guessedWord;
    private int maxWrongGuesses = 6;
    private int wrongGuessCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hangman_game, container, false);

        imageViewHangman = view.findViewById(R.id.imageViewHangman);
        textViewWordToGuess = view.findViewById(R.id.textViewWordToGuess);
        editTextGuess = view.findViewById(R.id.editTextGuess);
        buttonSubmitGuess = view.findViewById(R.id.buttonSubmitGuess);

        // Initialize the game or restore saved state
        if (savedInstanceState != null) {
            wordToGuess = savedInstanceState.getString("wordToGuess");
            guessedWord = new StringBuilder(savedInstanceState.getString("guessedWord"));
            wrongGuessCount = savedInstanceState.getInt("wrongGuessCount");
            updateGameUI();
            updateHangmanImage();
        } else {
            initializeGame();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("wordToGuess", wordToGuess);
        outState.putString("guessedWord", guessedWord.toString());
        outState.putInt("wrongGuessCount", wrongGuessCount);
    }

    public void submitGuess(View view) {
        String guess = editTextGuess.getText().toString().toUpperCase();

        if (guess.length() != 1 || !Character.isLetter(guess.charAt(0))) {
            Toast.makeText(getActivity(), "Please enter a single letter.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean correctGuess = false;
        for (int i = 0; i < wordToGuess.length(); i++) {
            if (wordToGuess.charAt(i) == guess.charAt(0)) {
                guessedWord.setCharAt(i, guess.charAt(0));
                correctGuess = true;
            }
        }

        updateGameUI();

        if (guessedWord.toString().equals(wordToGuess)) {
            Toast.makeText(getActivity(), "Congratulations! You've won!", Toast.LENGTH_SHORT).show();
            initializeGame();
            return;
        }

        if (!correctGuess) {
            wrongGuessCount++;
            updateHangmanImage();
            if (wrongGuessCount == maxWrongGuesses) {
                Toast.makeText(getActivity(), "Game Over! The word was: " + wordToGuess, Toast.LENGTH_SHORT).show();
                initializeGame();
            }
        }

        editTextGuess.getText().clear();
    }

    private void initializeGame() {
        Random random = new Random();
        int randomIndex = random.nextInt(words.length);
        wordToGuess = words[randomIndex];
        guessedWord = new StringBuilder();
        for (int i = 0; i < wordToGuess.length(); i++) {
            guessedWord.append("-");
        }
        updateGameUI();
    }

    private void updateGameUI() {
        textViewWordToGuess.setText(guessedWord.toString());
    }

    private void updateHangmanImage() {
        int drawableId = getResources().getIdentifier("hangman_" + wrongGuessCount, "drawable", getActivity().getPackageName());
        if (drawableId != 0) {
            imageViewHangman.setImageResource(drawableId);
        }
    }
}
