package com.example.spotifywrapped.ui.games;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Random;
import com.example.spotifywrapped.R;

public class hangmanGameFragment extends Fragment {

    private TextView textViewWordToGuess;
    private TextView textViewHangman;
    private EditText editTextGuess;
    private Button buttonSubmitGuess;

    private String[] words = {"SWIFT", "DRAKE", "BEYONCE", "BTS", "SPOTIFY"};
    private String wordToGuess;
    private StringBuilder guessedWord;
    private StringBuilder wrongGuesses;

    private int maxWrongGuesses = 6;
    private int wrongGuessCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hangman_game, container, false);

        textViewWordToGuess = view.findViewById(R.id.textViewWordToGuess);
        textViewHangman = view.findViewById(R.id.textViewHangman);
        editTextGuess = view.findViewById(R.id.editTextGuess);
        buttonSubmitGuess = view.findViewById(R.id.buttonSubmitGuess);

        buttonSubmitGuess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitGuess();
            }
        });

        if (savedInstanceState != null) {
            wordToGuess = savedInstanceState.getString("wordToGuess");
            guessedWord = new StringBuilder(savedInstanceState.getString("guessedWord"));
            wrongGuesses = new StringBuilder(savedInstanceState.getString("wrongGuesses"));
            wrongGuessCount = savedInstanceState.getInt("wrongGuessCount");
            updateGameUI();
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
        outState.putString("wrongGuesses", wrongGuesses.toString());
        outState.putInt("wrongGuessCount", wrongGuessCount);
    }

    private void submitGuess() {
        String guess = editTextGuess.getText().toString().toUpperCase();

        if (guess.length() != 1 || !Character.isLetter(guess.charAt(0))) {
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
                Toast.makeText(getActivity(), "Congratulations! You've won!", Toast.LENGTH_SHORT).show();
                initializeGame();
                return;
            }
        } else {
            wrongGuesses.append(guess);
            wrongGuessCount++;
            updateHangmanImage();
            if (wrongGuessCount == maxWrongGuesses) {
                Toast.makeText(getActivity(), "Game Over! The word was: " + wordToGuess, Toast.LENGTH_SHORT).show();
                initializeGame();
                return;
            }
        }

        editTextGuess.getText().clear();
        updateGameUI();
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
        textViewWordToGuess.setText(guessedWord.toString());
        textViewHangman.setText("Wrong guesses: " + wrongGuesses.toString());
    }

    private void updateHangmanImage() {
        // Here you can update the hangman image or representation based on wrongGuessCount
        // Since you don't have hangman images, you can customize this method as needed
    }
}
