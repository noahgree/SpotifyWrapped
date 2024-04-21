package com.example.spotifywrapped.ui.slideshow;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.ScoreEntry;
import com.example.spotifywrapped.databinding.FragmentGamesHomeBinding;
import com.example.spotifywrapped.ui.games.hangmanGameFragment;
import com.example.spotifywrapped.user.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

public class GamesHomeFragment extends Fragment {

    private FragmentGamesHomeBinding binding;
    public static Context context;
    private static User currentUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static String hmScore1 = "0";
    private static String hmScore2 = "0";
    private static String hmScore3 = "0";
    private static String hmPlayer1Var = "@Empty";
    private static String hmPlayer2Var = "@Empty";
    private static String hmPlayer3Var = "@Empty";
    private static String mgScore1 = "0";
    private static String mgScore2 = "0";
    private static String mgScore3 = "0";
    private static String mgPlayer1Var = "@Empty";
    private static String mgPlayer2Var = "@Empty";
    private static String mgPlayer3Var = "@Empty";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
        setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGamesHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FrameLayout mainLayout = root.findViewById(R.id.mainGa);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            if (insets.bottom > 0) {
                mlp.bottomMargin = insets.bottom;
                v.setLayoutParams(mlp);
            }

            return WindowInsetsCompat.CONSUMED;
        });

        binding.matchCard.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.matchingHomeFragment);
        });

        binding.hangCard.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navHangmanHome);
        });

        setUpHMScoreboard();
        setUpMGScoreboard();

        return root;
    }

    private void setUpMGScoreboard() {
        TextView mg1 = binding.getRoot().findViewById(R.id.mg1);
        TextView mg2 = binding.getRoot().findViewById(R.id.mg2);
        TextView mg3 = binding.getRoot().findViewById(R.id.mg3);

        mg1.setText(mgScore1);
        mg2.setText(mgScore2);
        mg3.setText(mgScore3);

        TextView mgPlayer1 = binding.getRoot().findViewById(R.id.mgPlayer1);
        TextView mgPlayer2 = binding.getRoot().findViewById(R.id.mgPlayer2);
        TextView mgPlayer3 = binding.getRoot().findViewById(R.id.mgPlayer3);

        mgPlayer1.setText(mgPlayer1Var);
        mgPlayer2.setText(mgPlayer2Var);
        mgPlayer3.setText(mgPlayer3Var);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Reference to the top score's document in Firestore
            DocumentReference topScoresRef = db.collection("Accounts").document("DbwyyYBNxvx710s0aE26");

            topScoresRef.get().addOnCompleteListener(getScoresTask -> {
                if (getScoresTask.isSuccessful()) {
                    Map<String, Object> scoresFromFirestore = (Map<String, Object>) getScoresTask.getResult().get("topmg");
                    SortedSet<ScoreEntry> sortedEntries = new TreeSet<>();

                    for (String key : scoresFromFirestore.keySet()) {
                        sortedEntries.add(new ScoreEntry(key));
                    }

                    List<ScoreEntry> topThreeEntries = sortedEntries.stream().limit(3).collect(Collectors.toList());
                    Log.d("KEYCHECK", topThreeEntries.toString());

                    for (int i = 0; i < topThreeEntries.size(); i++) {
                        ScoreEntry entry = topThreeEntries.get(i);
                        if (entry != null) {
                            String score = String.valueOf(entry.getScore());
                            String player = "@" + scoresFromFirestore.get(entry.getKey());
                            switch (i) {
                                case 0:
                                    mg1.setText(score);
                                    mgPlayer1.setText(player);
                                    mgScore1 = score;
                                    mgPlayer1Var = player;
                                    break;
                                case 1:
                                    mg2.setText(score);
                                    mgPlayer2.setText(player);
                                    mgScore2 = score;
                                    mgPlayer2Var = player;
                                    break;
                                case 2:
                                    mg3.setText(score);
                                    mgPlayer3.setText(player);
                                    mgScore3 = score;
                                    mgPlayer3Var = player;
                                    break;
                            }
                        }
                    }
                    Log.d("Firestore CHECK", user.getUid());
                } else {
                    Log.d(TAG, "Error getting data from firebase before setting up MG scores: " + getScoresTask.getException().getMessage());
                }
            });
        } else {
            Log.d(TAG, "User was null when trying to get MG scores");
        }
    }

    private void setUpHMScoreboard() {
        TextView hm1 = binding.getRoot().findViewById(R.id.hm1);
        TextView hm2 = binding.getRoot().findViewById(R.id.hm2);
        TextView hm3 = binding.getRoot().findViewById(R.id.hm3);

        hm1.setText(hmScore1);
        hm2.setText(hmScore2);
        hm3.setText(hmScore3);

        TextView hmPlayer1 = binding.getRoot().findViewById(R.id.hmPlayer1);
        TextView hmPlayer2 = binding.getRoot().findViewById(R.id.hmPlayer2);
        TextView hmPlayer3 = binding.getRoot().findViewById(R.id.hmPlayer3);

        hmPlayer1.setText(hmPlayer1Var);
        hmPlayer2.setText(hmPlayer2Var);
        hmPlayer3.setText(hmPlayer3Var);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Reference to the top score's document in Firestore
            DocumentReference topScoresRef = db.collection("Accounts").document("DbwyyYBNxvx710s0aE26");

            topScoresRef.get().addOnCompleteListener(getScoresTask -> {
                if (getScoresTask.isSuccessful()) {
                    Map<String, Object> scoresFromFirestore = (Map<String, Object>) getScoresTask.getResult().get("tophm");
                    SortedSet<ScoreEntry> sortedEntries = new TreeSet<>();

                    for (String key : scoresFromFirestore.keySet()) {
                        sortedEntries.add(new ScoreEntry(key));
                    }

                    List<ScoreEntry> topThreeEntries = sortedEntries.stream().limit(3).collect(Collectors.toList());
                    Log.d("KEYCHECK", topThreeEntries.toString());

                    for (int i = 0; i < topThreeEntries.size(); i++) {
                        ScoreEntry entry = topThreeEntries.get(i);
                        if (entry != null) {
                            String score = String.valueOf(entry.getScore());
                            String player = "@" + scoresFromFirestore.get(entry.getKey());
                            switch (i) {
                                case 0:
                                    hm1.setText(score);
                                    hmPlayer1.setText(player);
                                    hmScore1 = score;
                                    hmPlayer1Var = player;
                                    break;
                                case 1:
                                    hm2.setText(score);
                                    hmPlayer2.setText(player);
                                    hmScore2 = score;
                                    hmPlayer2Var = player;
                                    break;
                                case 2:
                                    hm3.setText(score);
                                    hmPlayer3.setText(player);
                                    hmScore3 = score;
                                    hmPlayer3Var = player;
                                    break;
                            }
                        }
                    }
                    Log.d("Firestore CHECK", user.getUid());
                } else {
                    Log.d(TAG, "Error getting data from firebase before setting up HM scores: " + getScoresTask.getException().getMessage());
                }
            });
        } else {
            Log.d(TAG, "User was null when trying to get HM scores");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}