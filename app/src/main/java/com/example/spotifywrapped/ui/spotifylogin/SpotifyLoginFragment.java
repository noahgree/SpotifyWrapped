package com.example.spotifywrapped.ui.spotifylogin;

import static com.example.spotifywrapped.MainActivity.context;
import static com.example.spotifywrapped.MainActivity.updateUserProfilePhoto;

import android.app.Activity;
import android.os.Bundle;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.spotifywrapped.MainActivity;
import com.example.spotifywrapped.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifywrapped.databinding.FragmentHomeBinding;
import com.example.spotifywrapped.databinding.FragmentLogInBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.example.spotifywrapped.databinding.FragmentSpotifyLoginBinding;
public class SpotifyLoginFragment extends Fragment {

    public static final String CLIENT_ID = "b063d7ec2077456d898909d249da6e49";
    public static final String REDIRECT_URI = "com.example.spotifywrapped://auth";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken, mAccessCode;
    private Call mCall;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ActivityResultLauncher<Intent> spotifyAuthLauncher;

    private AppCompatButton loginButton;
    private FragmentSpotifyLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_slide_left));
        setExitTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.fragment_slide_left));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity mainActivity = MainActivity.getInstance();

        // Initialize the ActivityResultLauncher
        spotifyAuthLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        final AuthorizationResponse response = AuthorizationClient.getResponse(result.getResultCode(), result.getData());
                        if (response.getType() == AuthorizationResponse.Type.TOKEN) {
                            MainActivity.saveSpotifyToken(response.getAccessToken());
                            mainActivity.setupNavigationAndToolbar();
                            updateUserProfilePhoto();
                            mAuth = FirebaseAuth.getInstance();
                            db = FirebaseFirestore.getInstance();
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Map<String, Object> userAccount = new HashMap<>();
                                userAccount.put("token", response.getAccessToken());

                                db.collection("Accounts").document(user.getUid())
                                        .set(userAccount, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                                        .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                            }
                            NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_content_main);
                            MainActivity.getInstance().saveUser(MainActivity.getCurrentUser());
                            navController.navigate(R.id.nav_gallery);
                        }
                    }
                });

        if (MainActivity.isSpotifyLoggedIn()) {
            // User is already logged in with Spotify, navigate away or load data
        }

        binding.spotifySignIn.setOnClickListener(v -> getToken());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSpotifyLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        loginButton = binding.spotifySignIn;
        loginButton.setOnClickListener((v) -> {
            getToken();
        });
        // Inflate the layout for this fragment
        return root;
    }

    /**
     * Get token from Spotify
     * This method will open the Spotify login activity and get the token
     * What is token?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getToken() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        Intent intent = AuthorizationClient.createLoginActivityIntent(getActivity(), request);
        spotifyAuthLauncher.launch(intent);
    }

    /**
     * Get code from Spotify
     * This method will open the Spotify login activity and get the code
     * What is code?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getCode() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
        AuthorizationClient.openLoginActivity(getActivity(), AUTH_CODE_REQUEST_CODE, request);
    }

    /**
     * Get authentication request
     *
     * @param type the type of the request
     * @return the authentication request
     */
    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                .setScopes(new String[] { "user-read-email", "user-top-read" })
                .setCampaign("your-campaign-token")
                .build();
    }

    // Gets the redirect Uri for Spotify
    private Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    @Override
    public void onDestroy() {
        cancelCall();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        ImageView imageView = getActivity().findViewById(R.id.currentPageIcon);
        imageView.setVisibility(View.VISIBLE);
    }
}