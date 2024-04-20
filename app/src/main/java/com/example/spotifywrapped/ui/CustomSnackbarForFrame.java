package com.example.spotifywrapped.ui;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.spotifywrapped.R;
import com.google.android.material.snackbar.Snackbar;

public class CustomSnackbarForFrame {

    public static void showCustomSnackbarForFrame(FragmentActivity activity, View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        Drawable backgroundDrawable = ContextCompat.getDrawable(activity, R.drawable.rounded_corners_drawable);
        Drawable wrappedDrawable = DrawableCompat.wrap(backgroundDrawable);
        int tintColor = ContextCompat.getColor(activity, R.color.spotify_black);
        DrawableCompat.setTint(wrappedDrawable, tintColor);
        DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.ADD);

        snackbarView.setBackground(wrappedDrawable);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        snackbarView.setLayoutParams(params);

        ViewCompat.setOnApplyWindowInsetsListener(snackbarView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            mlp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            mlp.setMargins(15, mlp.topMargin + 15, 15, mlp.bottomMargin);

            if (insets.top > 0) {
                mlp.topMargin = insets.top + 15;
                v.setLayoutParams(mlp);
            }

            return WindowInsetsCompat.CONSUMED;
        });

        snackbar.show();
    }
}