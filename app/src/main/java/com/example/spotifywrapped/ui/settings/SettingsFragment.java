package com.example.spotifywrapped.ui.settings;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.example.spotifywrapped.databinding.FragmentTopGenreBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
        setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_fade));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        LinearLayout mainLayout = root.findViewById(R.id.mainS);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            if (insets.bottom > 0) {
                mlp.bottomMargin = insets.bottom;
                v.setLayoutParams(mlp);
            }

            return WindowInsetsCompat.CONSUMED;
        });

        Button btnNewEmail = root.findViewById(R.id.btnSubmitAccountEmail);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String userId = auth.getCurrentUser().getUid();
        btnNewEmail.setOnClickListener(view -> {
            EditText newEmailField = (EditText) root.findViewById(R.id.newEmailInput);
            String newPassword = newEmailField.getText().toString();

            Log.d("AUTH CHANGE", newPassword);

            if (user != null && !newPassword.isEmpty()) {
                if(newPassword.length() >= 6) {
                    user.updatePassword(newPassword)
                            //Changes
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User password updated.");
                                    Toast.makeText(getContext(), "Password changed sucessfully!", Toast.LENGTH_SHORT).show();
                                }
                            });
//                    user.updateEmail(newEmail)
//                            .addOnSuccessListener(aVoid -> {
//                                Toast.makeText(getActivity(), "Email updated successfully!", Toast.LENGTH_SHORT).show();
//                                Log.d(TAG, "Email updated successfully!");
//
//                                // Optionally update the email in your Firestore database as well
//                                Map<String, Object> userAccount = new HashMap<>();
//                                userAccount.put("email", newEmail);
//                                db.collection("Accounts").document(user.getUid())
//                                        .set(userAccount)
//                                        .addOnSuccessListener(aVoidFirestore -> Log.d(TAG, "Firestore email update successful"))
//                                        .addOnFailureListener(e -> Log.w(TAG, "Firestore update failed", e));
//
//                                // Notify other parts of your application if necessary
//                                MainActivity.onLoginSuccess(MainActivity.getCurrentUser().getName(), newEmail);
//                            })
//                            .addOnFailureListener(e -> {
//                                Toast.makeText(getActivity(), "Failed to update email: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                                Log.e(TAG, "Email update failed", e);
//                            });
                } else {
                    Log.d(TAG, "Password too short.");
                    Toast.makeText(getContext(), "Password needs to be at least 6 characters long.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Email field is empty or user is not logged in", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnDeleteAccount = root.findViewById(R.id.btnDeleteAccount);
        btnDeleteAccount.setOnClickListener(view -> deleteAccount());

        return root;
    }

    private void deleteAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                    // Log out the user after account deletion
                    FirebaseAuth.getInstance().signOut();
                    // Navigate back to the login fragment or any other appropriate action
                    hideActionBar();

                    navigateToLoginFragment();
                } else {
                    Toast.makeText(getActivity(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                }
            });
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

    private void navigateToLoginFragment() {
        if (getActivity() != null) {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_login);
        }
    }
}
