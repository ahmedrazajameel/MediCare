package com.medical.medicarepro.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medical.medicarepro.R;
import com.medical.medicarepro.ui.activities.AddPatientActivity;
import com.medical.medicarepro.ui.activities.LoginActivity;
import com.medical.medicarepro.utils.FirebaseManager;
import com.medical.medicarepro.models.Patient;
import com.medical.medicarepro.models.User;
import java.util.List;

public class DashboardFragment extends Fragment {
    private TextView tvUserName, tvLocation, tvTotalPatients, tvActiveCases, tvTodayAppointments;
    private CardView cardCases, cardAddPatient;
    private SwipeRefreshLayout swipeRefresh;
    private FirebaseManager firebaseManager;

    // Real-time listener for patients count
    private DatabaseReference patientsRef;
    private ValueEventListener patientsCountListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        initViews(view);
        setupListeners();
        loadUserData();
        setupRealtimePatientCount(); // Add real-time listener for patient count
        return view;
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvTotalPatients = view.findViewById(R.id.tvTotalPatients);
        tvActiveCases = view.findViewById(R.id.tvActiveCases);
        tvTodayAppointments = view.findViewById(R.id.tvTodayAppointments);
        cardCases = view.findViewById(R.id.cardCases);
        cardAddPatient = view.findViewById(R.id.cardAddPatient);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        firebaseManager = FirebaseManager.getInstance();
    }

    private void setupListeners() {
        cardCases.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.navigation_cases);
                }
            }
        });

        cardAddPatient.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddPatientActivity.class));
        });

        swipeRefresh.setOnRefreshListener(() -> {
            // Manual refresh if needed
            swipeRefresh.setRefreshing(false);
        });
    }

    private void loadUserData() {
        // First try to get user from LoginActivity (stored during login)
        User loggedInUser = LoginActivity.getLoggedInUser();

        if (loggedInUser != null && loggedInUser.getName() != null) {
            // User found from LoginActivity
            tvUserName.setText(loggedInUser.getName());
            tvLocation.setText(loggedInUser.getLocation() != null ? loggedInUser.getLocation() : "Central");
            return;
        }

        // If not found, try to get from Firebase
        firebaseManager.getCurrentUserData(new FirebaseManager.FirebaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (getActivity() != null && user != null) {
                    if (user.getName() != null && !user.getName().isEmpty()) {
                        tvUserName.setText(user.getName());
                    } else {
                        tvUserName.setText("Medical Officer");
                    }

                    if (user.getLocation() != null && !user.getLocation().isEmpty()) {
                        tvLocation.setText(user.getLocation());
                    } else {
                        tvLocation.setText("Central");
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() != null) {
                    tvUserName.setText("Medical Officer");
                    tvLocation.setText("Central");
                }
            }
        });
    }

    // Real-time listener for patient count - Updates instantly when patient added
    private void setupRealtimePatientCount() {
        patientsRef = FirebaseDatabase.getInstance().getReference("patients");
        patientsCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getActivity() != null) {
                    int count = (int) dataSnapshot.getChildrenCount();
                    tvTotalPatients.setText(String.valueOf(count));
                    tvActiveCases.setText(String.valueOf(count));
                    // tvTodayAppointments remains 0 for now
                    tvTodayAppointments.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getActivity() != null) {
                    tvTotalPatients.setText("0");
                    tvActiveCases.setText("0");
                    tvTodayAppointments.setText("0");
                }
            }
        };
        patientsRef.addValueEventListener(patientsCountListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove listener to prevent memory leaks
        if (patientsRef != null && patientsCountListener != null) {
            patientsRef.removeEventListener(patientsCountListener);
        }
    }
}