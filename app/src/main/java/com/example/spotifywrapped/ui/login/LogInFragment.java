package com.example.spotifywrapped.ui.login;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentLogInBinding;

import java.util.Random;

public class LogInFragment extends Fragment {
    // TODO: Rename and change types and number of parameters

    private FragmentLogInBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLogInBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button myButton = root.findViewById(R.id.loginButton);
        myButton.setBackgroundResource(R.drawable.rounded_button);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}