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
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.InputType;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    // TODO: Rename and change types and number of parameters

    private FragmentLogInBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth = FirebaseAuth.getInstance();
//        if(currentUser != null){
//            finish();
//            startActivity(getIntent());
//        }
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

        if (mainActivity != null) {
            mainActivity.setupNavigationAndToolbar();
            hideActionBar();
        }
        // Inflate the layout for this fragment
        binding = FragmentLogInBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //Login Button
        Button myButton = root.findViewById(R.id.loginButton);
        myButton.setBackgroundResource(R.drawable.rounded_button);
        //Signup Button
        Button signupButton = root.findViewById(R.id.signupButton);
        signupButton.setBackgroundResource(R.drawable.rounded_button);

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = String.valueOf(((EditText) root.findViewById(R.id.emailInput)).getText());
                String password = String.valueOf(((EditText) root.findViewById(R.id.passwordInput)).getText());
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(root.getContext(), "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener((Activity) root.getContext(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        solidifyNewUser(user, password);
                                        updateUI(user);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(root.getContext(), "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        updateUI(null);
                                    }
                                }
                            });
                }
            }
        });
        //Sign Up Button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = String.valueOf(((EditText) root.findViewById(R.id.emailInput)).getText());
                String password = String.valueOf(((EditText) root.findViewById(R.id.passwordInput)).getText());
                if(email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(root.getContext(), "Sign up failed.",
                            Toast.LENGTH_SHORT).show();
                } else if (password.length() < 6) {
                    Toast.makeText(root.getContext(), "Password entered is too short. Please use at least 6 characters.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Create an AlertDialog.Builder to get the user's name
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Please enter your full name");

                    // Set up the input fields
                    final EditText input = new EditText(getActivity());
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                    input.setHint("First and Last Name");
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String fullName = input.getText().toString();
                            // Splitting the fullName into first and last name parts if needed
                            // Proceed with the rest of the sign-up process...
                            String email = String.valueOf(((EditText) root.findViewById(R.id.emailInput)).getText());
                            String password = String.valueOf(((EditText) root.findViewById(R.id.passwordInput)).getText());

                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener((Activity) root.getContext(), task -> {
                                        if (task.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            if (user != null) {
                                                Map<String, Object> userAccount = new HashMap<>();
                                                userAccount.put("name", fullName);
                                                userAccount.put("email", email);
                                                userAccount.put("wraps", new ArrayList<Map<String, Object>>());
                                                // Avoid storing plain passwords
                                                db.collection("Accounts").document(user.getUid())
                                                        .set(userAccount)
                                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                                                        .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                                            }
                                            solidifyNewUser(user, password);
                                            updateUI(user);
                                        } else {
                                            Toast.makeText(root.getContext(), "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                            updateUI(null);
                                        }
                                    });
                        }
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                    builder.show();
                }
            }
        });
        return root;
    }

    private void solidifyNewUser(FirebaseUser user, String password) {
        //this looks for userdata on firebase, this will only happen if the user exists
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();;
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
                            Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                            // You can use the retrieved data here (name, email, password)
                            currentUser[0] = new User(email, passwordf, userId, name, wraps);
                            MainActivity.setCurrentUser(currentUser[0]);
                            //Use this method in MainActivity to update any values with the name and email
                            MainActivity.onLoginSuccess(name, email);
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

    //When the user logs in the keyboard will auto close.
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //Changes to the homescreen of the app when logging in
    public void updateUI(FirebaseUser account){
        View root = binding.getRoot();
        if (account != null) {
            Toast.makeText(getContext(), "You Signed In successfully", Toast.LENGTH_LONG).show();
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
            Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_LONG).show();
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