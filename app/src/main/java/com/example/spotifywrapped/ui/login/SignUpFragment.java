package com.example.spotifywrapped.ui.login;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentLogInBinding;
import com.example.spotifywrapped.databinding.FragmentSignUpBinding;
import com.example.spotifywrapped.user.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment {

    private static FragmentSignUpBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public static String passedEmail = "";
    public static String passedPW = "";

    public static void setPassedEmail(String incomingEmail) {
        passedEmail = incomingEmail;
    }
    public static void setPassedPW(String incomingPW) {
        passedPW = incomingPW;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity mainActivity = (MainActivity) getActivity();
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (mainActivity != null) {
            mainActivity.setupNavigationAndToolbar();
            hideActionBar();
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
        setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_slide_left));

        EditText emailInput = root.findViewById(R.id.emailInputSU);
        EditText passwordInput = root.findViewById(R.id.passwordInputSU);
        emailInput.setText(passedEmail);
        passwordInput.setText(passedPW);
        passedPW = "";

        // Back to login screen
        binding.backToLoginBtn.setOnClickListener(v -> {
            setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_slide_right));
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_login);
            String email = String.valueOf(((EditText) root.findViewById(R.id.emailInputSU)).getText());
            String password = String.valueOf(((EditText) root.findViewById(R.id.passwordInputSU)).getText());
            LogInFragment.setPassedBackEmail(email);
            LogInFragment.setPassedBackPW(password);
        });

        // Confirm sign up
        binding.finishSignUpBtn.setOnClickListener(v -> {
            setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_slide_left));
            EditText firstNameInput = root.findViewById(R.id.firstNameInputSU);
            EditText lastNameInput = root.findViewById(R.id.lastNameInputSU);

            if (firstNameInput.getText().length() == 0 || lastNameInput.getText().length() == 0) {
                Toast.makeText(root.getContext(), "First & Last Name Cannot Be Empty", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(emailInput.getText().toString(), passwordInput.getText().toString())
                    .addOnCompleteListener((Activity) root.getContext(), task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Map<String, Object> userAccount = new HashMap<>();
                                userAccount.put("name", firstNameInput.getText().toString() + " " + lastNameInput.getText().toString());
                                userAccount.put("email", emailInput.getText().toString());
                                userAccount.put("wraps", new ArrayList<Map<String, Object>>());
                                userAccount.put("totalpoints", "0");
                                // Avoid storing plain passwords
                                db.collection("Accounts").document(user.getUid())
                                        .set(userAccount)
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                                        .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                            }
                            solidifyNewUser(user, passwordInput.getText().toString());
                            updateUI(user);
                        } else {
                            Toast.makeText(root.getContext(), "Authentication Failed", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    });
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
                            // You can use the retrieved data here (name, email, password)
                            currentUser[0] = new User(email, passwordf, userId, name, wraps, totalPoints);
                            MainActivity.setCurrentUser(currentUser[0]);
                            //Use this method in MainActivity to update any values with the name and email
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

    // Changes to the gallery screen of the app when logging in
    public void updateUI(FirebaseUser account){
        View root = binding.getRoot();
        if (account != null) {
            Toast.makeText(getContext(), "Signup Successful", Toast.LENGTH_LONG).show();
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

    // When the user logs in the keyboard will auto close.
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}