package com.medical.medicarepro.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.medical.medicarepro.R;
import com.medical.medicarepro.models.User;
import com.medical.medicarepro.ui.activities.LoginActivity;
import com.medical.medicarepro.utils.FirebaseManager;

public class ProfileFragment extends Fragment {
    private TextView tvName, tvLocation, tvRole;
    private Button btnSignOut;
    private FirebaseManager firebaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tvName);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvRole = view.findViewById(R.id.tvRole);
        btnSignOut = view.findViewById(R.id.btnSignOut);

        firebaseManager = FirebaseManager.getInstance();

        // Load user data
        loadUserProfile();

        btnSignOut.setOnClickListener(v -> {
            firebaseManager.logout();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void loadUserProfile() {
        // First try to get user from LoginActivity
        User loggedInUser = LoginActivity.getLoggedInUser();

        if (loggedInUser != null) {
            displayUserData(loggedInUser);
        } else {
            // If not available, fetch from Firebase
            firebaseManager.getCurrentUserData(new FirebaseManager.FirebaseCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user != null && getContext() != null) {
                        displayUserData(user);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    // Set default values
                    tvName.setText("Medical Officer");
                    tvRole.setText("Medical Officer");
                    tvLocation.setText("Central");
                }
            });
        }
    }

    private void displayUserData(User user) {
        if (getContext() == null) return;

        // Display Full Name
        String name = user.getName();
        if (name != null && !name.isEmpty()) {
            tvName.setText(name);
        } else {
            tvName.setText("Medical Officer");
        }

        // Display Role (Medical Officer or Nurse)
        String role = user.getRole();
        if (role != null && !role.isEmpty()) {
            tvRole.setText(role);
        } else {
            tvRole.setText("Medical Officer");
        }

        // Display Location
        String location = user.getLocation();
        if (location != null && !location.isEmpty()) {
            tvLocation.setText(location);
        } else {
            tvLocation.setText("Central");
        }
    }
}