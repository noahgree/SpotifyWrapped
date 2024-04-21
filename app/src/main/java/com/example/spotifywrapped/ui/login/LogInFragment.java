package com.example.spotifywrapped.ui.login;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.InputType;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentLogInBinding;
import com.example.spotifywrapped.user.User;
import com.example.spotifywrapped.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class LogInFragment extends Fragment {

    private static FragmentLogInBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public static String passedBackEmail = "";
    public static String passedBackPW = "";

    public static void setPassedBackEmail(String incomingEmail) {
        passedBackEmail = incomingEmail;
    }
    public static void setPassedBackPW(String incomingPW) {
        passedBackPW = incomingPW;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
        setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_slide_left));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MainActivity mainActivity = (MainActivity) getActivity();
        binding = FragmentLogInBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (mainActivity != null) {
            mainActivity.setupNavigationAndToolbar();
            hideActionBar();
        }

        EditText emailInput = root.findViewById(R.id.emailInput);
        EditText passwordInput = root.findViewById(R.id.passwordInput);
        emailInput.setText(passedBackEmail);
        passwordInput.setText(passedBackPW);
        passedBackPW = "";

        // Login Button
        Button myButton = root.findViewById(R.id.loginButton);
        myButton.setBackgroundResource(R.drawable.rounded_button);

        // Signup Button
        Button signupButton = root.findViewById(R.id.signupButton);
        signupButton.setBackgroundResource(R.drawable.rounded_button);

        // Login button response
        myButton.setOnClickListener(v -> {
            String email = String.valueOf(((EditText) root.findViewById(R.id.emailInput)).getText());
            String password = String.valueOf(((EditText) root.findViewById(R.id.passwordInput)).getText());
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(root.getContext(), "Authentication Failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener((Activity) root.getContext(), task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            solidifyNewUser(user, password);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(root.getContext(), "Authentication Failed",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    });
            }
        });

        // Sign Up Button
        signupButton.setOnClickListener(v -> {
            String email = String.valueOf(((EditText) root.findViewById(R.id.emailInput)).getText());
            String password = String.valueOf(((EditText) root.findViewById(R.id.passwordInput)).getText());
            if (email.isEmpty() || password.isEmpty()) {
                // If sign in fails, display a message to the user.
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_signup);
                SignUpFragment.setPassedEmail(email);
                SignUpFragment.setPassedPW(password);
            } else {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener((Activity) root.getContext(), task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                solidifyNewUser(user, password);
                                updateUI(user);
                            }
                        });
            }
        });

        return root;
    }

    private void solidifyNewUser(FirebaseUser user, String password) {
        // this looks for userdata on firebase, this will only happen if the user exists
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = user.getUid(); // The user ID to search for, which matches the document ID
        final User[] currentUser = new User[1];
        db.collection("Accounts").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String name = document.getString("name");
                            String email = document.getString("email");
                            ArrayList<Map<String, Object>> wraps = (ArrayList<Map<String, Object>>) document.get("wraps");
                            String passwordf = document.getString("password");
                            String token = document.getString("token");
                            String totalPoints = document.getString("totalpoints");
                            Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                            // You can use the retrieved data here (name, email, points)
                            currentUser[0] = new User(email, passwordf, userId, name, wraps, totalPoints);
                            MainActivity.setCurrentUser(currentUser[0]);
                            // Use this method in MainActivity to update any values with the name and email and points
                            MainActivity.onLoginSuccess(name, email, totalPoints);
                        } else {
                            // The document does not exist, do nothing
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                });
        MainActivity.setCurrentUser(currentUser[0]);
    }

    // When the user logs in the keyboard will auto close
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // Changes to the gallery screen of the app when logging in
    public void updateUI(FirebaseUser account){
        View root = binding.getRoot();
        if (account != null) {
            Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_LONG).show();
            MainActivity mainActivity = (MainActivity) getActivity();
            hideKeyboard(root);

            if (mainActivity != null) {
                // Setup main interface and navigate to the gallery fragment
                mainActivity.setupNavigationAndToolbar();
                hideActionBar();
                NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.spotifyLoginFragment);
            }
        } else {
            Toast.makeText(getContext(), "Authentication Failed", Toast.LENGTH_LONG).show();
        }
    }

    private void hideActionBar() {
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
            if (appCompatActivity.getSupportActionBar() != null) {
                appCompatActivity.getSupportActionBar().hide();
                ImageView imageView = getActivity().findViewById(R.id.currentPageIcon);
                imageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}