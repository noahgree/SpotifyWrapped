package com.example.spotifywrapped.ui.settings;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentSettingsBinding;
import com.example.spotifywrapped.user.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    public static Context context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.getInstance();
        setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
        setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        FrameLayout mainLayout = root.findViewById(R.id.mainS);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            if (insets.bottom > 0) {
                mlp.bottomMargin = insets.bottom;
                v.setLayoutParams(mlp);
            }

            return WindowInsetsCompat.CONSUMED;
        });

        Button btnNewPW = root.findViewById(R.id.btnNewPW);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String userId = auth.getCurrentUser().getUid();

        EditText newNameField = (EditText) root.findViewById(R.id.newNameInput);
        EditText newPWField = (EditText) root.findViewById(R.id.newPWInput);
        EditText oldPWField = (EditText) root.findViewById(R.id.oldPWForVerification);
        TextView updatedNameConfirmText = root.findViewById(R.id.updatedNameConfirm);
        TextView updatedPWConfirmText = root.findViewById(R.id.updatedPWConfirm);
        TextView oldPWCheckerText = root.findViewById(R.id.oldPWCheckerText);

        oldPWField.setText("");
        newPWField.setText("");

        updatedNameConfirmText.setVisibility(View.GONE);
        updatedPWConfirmText.setVisibility(View.GONE);
        oldPWCheckerText.setVisibility(View.GONE);

        newNameField.setText(loadUser().getName());

        btnNewPW.setOnClickListener(v -> {
            String newFullName = newNameField.getText().toString();
            String newPassword = newPWField.getText().toString();
            Log.d("AUTH CHANGE: NAME", newFullName);
            Log.d("AUTH CHANGE: PW", newPassword);

            updatedNameConfirmText.setText("NAME SAVE FAILED");
            updatedNameConfirmText.setTextColor(context.getColor(R.color.spotify_red));
            updatedNameConfirmText.setVisibility(View.GONE);
            updatedPWConfirmText.setText("NEW PASSWORD SAVE FAILED");
            updatedPWConfirmText.setTextColor(context.getColor(R.color.spotify_red));
            updatedPWConfirmText.setVisibility(View.GONE);
            oldPWCheckerText.setVisibility(View.GONE);

            if (user != null) {
                // re-authenticate before making changes
                if (oldPWField.getText().toString().isEmpty()) {
                    Log.d(TAG, "Nothing changed, old password field empty");
                    Toast.makeText(getActivity(), "Enter Your Old Password First", Toast.LENGTH_SHORT).show();
                    return;
                }
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPWField.getText().toString());
                user.reauthenticate(credential)
                        .addOnCompleteListener(reAuthTask -> {
                            if (reAuthTask.isSuccessful()) {
                                Log.d("AUTH", "User re-authenticated.");
                                oldPWField.setText("");

                                if (!(newFullName.equals(MainActivity.getCurrentUser().getName())) && !(newFullName.isEmpty())) {
                                    db.collection("Accounts").document(user.getUid()).get()
                                            .addOnCompleteListener(collectionGetterTask -> {
                                                if (collectionGetterTask.isSuccessful() && collectionGetterTask.getResult() != null) {
                                                    // Get old data to combine with new name
                                                    DocumentSnapshot documentSnapshot = collectionGetterTask.getResult();
                                                    Map<String, Object> userAccount = new HashMap<>(documentSnapshot.getData());
                                                    List<Map<String, Object>> wraps = (List<Map<String, Object>>) documentSnapshot.get("wraps");
                                                    String name = (String) documentSnapshot.get("name");
                                                    String token = (String) documentSnapshot.get("token");

                                                    userAccount.put("token", token);
                                                    userAccount.put("wraps", wraps);

                                                    userAccount.put("name", newFullName);

                                                    // Now update the Firestore document
                                                    db.collection("Accounts").document(user.getUid())
                                                            .set(userAccount)
                                                            .addOnCompleteListener(nameUpdateInFireStoreTask -> {
                                                                if (nameUpdateInFireStoreTask.isSuccessful()) {
                                                                    Log.d(TAG, "Firestore name update successful");
                                                                    MainActivity.getCurrentUser().setName(newFullName);
                                                                    MainActivity.onLoginSuccess(newFullName, MainActivity.getCurrentUser().getEmail(), MainActivity.getCurrentUser().getTotalPoints());
                                                                    MainActivity.getInstance().saveUser(MainActivity.getCurrentUser());
                                                                    updatedNameConfirmText.setText("NEW NAME SAVED");
                                                                    updatedNameConfirmText.setTextColor(context.getColor(R.color.spotify_blue));
                                                                    updatedNameConfirmText.setVisibility(View.VISIBLE);
                                                                } else {
                                                                    Log.e("AUTH", "Failed to update name in Firestore.", nameUpdateInFireStoreTask.getException());
                                                                    updatedNameConfirmText.setVisibility(View.VISIBLE);
                                                                }
                                                            });
                                                } else {
                                                    Log.e("AUTH", "Failed to fetch document.", collectionGetterTask.getException());
                                                    updatedNameConfirmText.setVisibility(View.VISIBLE);
                                                }
                                            });
                                }
                                // Check valid password (formatting)
                                if (!(newPassword.length() >= 6) && !(newPassword.isEmpty())) {
                                    updatedPWConfirmText.setText("NEW PASSWORD MUST BE AT LEAST 6 CHARACTERS");
                                    updatedPWConfirmText.setVisibility(View.VISIBLE);
                                    return;
                                } else if (newPassword.isEmpty()) {
                                    return;
                                }
                                user.updatePassword(newPassword)
                                        .addOnCompleteListener(passwordUpdateTask -> {
                                            if (passwordUpdateTask.isSuccessful()) { // password update successful
                                                Log.d("AUTH", "User password updated.");
                                                updatedPWConfirmText.setText("NEW PASSWORD SAVED");
                                                updatedPWConfirmText.setTextColor(context.getColor(R.color.spotify_blue));
                                                updatedPWConfirmText.setVisibility(View.VISIBLE);
                                            } else { // password update failed
                                                Log.e("AUTH", "Failed to update password.", passwordUpdateTask.getException());
                                                updatedPWConfirmText.setVisibility(View.VISIBLE);
                                            }
                                        });
                            } else { // re-auth failed
                                if (reAuthTask.getException() instanceof FirebaseAuthException) {
                                    FirebaseAuthException e = (FirebaseAuthException) reAuthTask.getException();
                                    switch (e.getErrorCode()) {
                                        case "ERROR_REQUIRES_RECENT_LOGIN":
                                            Log.d("AUTH", "Changing auth requires new session.");
                                            oldPWField.setText("");
                                            NavController navController = Navigation.findNavController(v);
                                            navController.navigate(R.id.nav_login);
                                            MainActivity context = MainActivity.getInstance();
                                            context.logoutUser(false);
                                            Toast.makeText(getActivity(), "Session expired. Login again to enable editing sensitive information", Toast.LENGTH_LONG).show();
                                            break;
                                        case "ERROR_WRONG_PASSWORD":
                                            Log.d("AUTH", "Wrong password given for credential check.");
                                            oldPWCheckerText.setText("INCORRECT OLD PASSWORD. CHANGES NOT SAVED.");
                                            oldPWCheckerText.setVisibility(View.VISIBLE);
                                            oldPWField.setText("");
                                            break;
                                        default:
                                            Log.d("AUTH", "Some other error on credential check: " + e.getMessage());
                                            oldPWCheckerText.setText("UNKNOWN ERROR. CHANGES NOT SAVED.");
                                            Toast.makeText(getActivity(), "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                } else {
                                    // Handle other exceptions that are not related to Firebase Auth
                                    oldPWCheckerText.setText("DATABASE ERROR. CHANGES NOT SAVED. TRY AGAIN LATER.");
                                    Log.e("AUTH", "Re-authentication failed", reAuthTask.getException());
                                }
                            }
                        });
            } else { // user returned null, stop immediately
                updatedNameConfirmText.setVisibility(View.VISIBLE);
                updatedPWConfirmText.setVisibility(View.VISIBLE);
                Log.e("AUTH", "Current user is null");
            }
        });

        Button btnDeleteAccount = root.findViewById(R.id.btnDeleteAccount);
        btnDeleteAccount.setOnClickListener(view -> deleteAccount());

        return root;
    }

    private void deleteAccount() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String userId = auth.getCurrentUser().getUid();

        if (user != null) {
            db.collection("Accounts").document(user.getUid()).delete().addOnCompleteListener(deleteTask -> {
                if (deleteTask.isSuccessful()) {
                    Log.d("Delete Action", "Deleting account for user: " + user.getUid());
                } else {
                    Log.d("Delete Error", deleteTask.getException().getMessage());
                }
            });
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Account Deleted", Toast.LENGTH_SHORT).show();
                    // Log out the user after account deletion
                    FirebaseAuth.getInstance().signOut();
                    // Navigate back to the login fragment or any other appropriate action
                    hideActionBar();

                    navigateToLoginFragment();
                } else {
                    Toast.makeText(getActivity(), "Failed to Delete Account", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private User loadUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString("CurrentUser", null);
        Log.d("SharedPreferences", "Loaded token: " + userJson);
        return gson.fromJson(userJson, User.class);
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

    private void navigateToLoginFragment() {
        if (getActivity() != null) {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_login);
        }
    }
}
