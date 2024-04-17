package com.example.spotifywrapped.ui;

import android.os.Bundle;

import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentAddWrapBinding;
import com.example.spotifywrapped.databinding.FragmentMainAppBarBinding;
import com.google.android.material.appbar.AppBarLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainAppBar extends Fragment {
    private FragmentMainAppBarBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("yoo", "YOOOO");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainAppBarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        AppBarLayout appBarLayout = binding.appBarLayout;
        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply the insets as a margin to the view. This solution sets only the
            // bottom, left, and right dimensions, but you can apply whichever insets are
            // appropriate to your layout. You can also update the view padding if that's
            // more appropriate.
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.leftMargin = insets.left;
            mlp.bottomMargin = insets.bottom;
            mlp.rightMargin = insets.right;
            mlp.topMargin = insets.top;
            Log.d("here", String.valueOf(insets.top));
            v.setLayoutParams(mlp);

            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            return WindowInsetsCompat.CONSUMED;
        });

        return root;
    }
}