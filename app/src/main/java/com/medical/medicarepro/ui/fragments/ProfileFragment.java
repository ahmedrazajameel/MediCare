package com.medical.medicarepro.ui.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.medical.medicarepro.R;
import com.medical.medicarepro.ui.activities.LoginActivity;
import com.medical.medicarepro.utils.FirebaseManager;
import com.medical.medicarepro.models.User;

public class ProfileFragment extends Fragment {
    private TextView tvName, tvEmail, tvLocation, tvRole;
    private MaterialButton btnSignOut;
    private FirebaseManager firebaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);
        setupListeners();
        loadUserData();
        return view;
    }

    private void initViews(View view) {
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvRole = view.findViewById(R.id.tvRole);
        btnSignOut = view.findViewById(R.id.btnSignOut);

        firebaseManager = FirebaseManager.getInstance();
    }

    private void setupListeners() {
        btnSignOut.setOnClickListener(v -> showSignOutDialog());
    }

    private void loadUserData() {
        firebaseManager.getCurrentUserData(new FirebaseManager.FirebaseCallback<User>() {
            @Override
            public void onSuccess(User result) {
                if (getActivity() != null) {
                    tvName.setText(result.getName());
                    tvEmail.setText(result.getEmail());
                    tvLocation.setText(result.getLocation());
                    tvRole.setText(result.getRole());
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() != null) {
                    tvName.setText("Medical Officer");
                    tvEmail.setText(firebaseManager.getCurrentUser() != null ?
                            firebaseManager.getCurrentUser().getEmail() : "user@medical.com");
                    tvLocation.setText("Central");
                    tvRole.setText("Medical Officer");
                }
            }
        });
    }

    private void showSignOutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> {
                    firebaseManager.logout();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}