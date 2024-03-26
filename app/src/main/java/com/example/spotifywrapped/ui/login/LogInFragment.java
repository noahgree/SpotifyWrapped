package com.example.spotifywrapped.ui.login;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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


public class LogInFragment extends Fragment {
    // TODO: Rename and change types and number of parameters

    private FragmentLogInBinding binding;
    private FirebaseAuth mAuth;
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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLogInBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button myButton = root.findViewById(R.id.loginButton);
        myButton.setBackgroundResource(R.drawable.rounded_button);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = String.valueOf(((EditText) root.findViewById(R.id.emailInput)).getText());
                String password = String.valueOf(((EditText) root.findViewById(R.id.passwordInput)).getText());
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
                            String passwordf = document.getString("password");

                            Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                            // You can use the retrieved data here (name, email, password)
                            currentUser[0] = new User(email, passwordf, userId, name);
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
                NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.nav_gallery);
            }
        } else {
            Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}