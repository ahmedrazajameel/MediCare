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
import com.medical.medicarepro.R;
import com.medical.medicarepro.ui.activities.AddPatientActivity;
import com.medical.medicarepro.utils.FirebaseManager;
import com.medical.medicarepro.models.Patient;
import com.medical.medicarepro.models.User;
import java.util.List;

public class DashboardFragment extends Fragment {
    private TextView tvUserName, tvLocation, tvTotalPatients, tvActiveCases, tvTodayAppointments;
    private CardView cardCases, cardAddPatient;
    private SwipeRefreshLayout swipeRefresh;
    private FirebaseManager firebaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        initViews(view);
        setupListeners();
        loadUserData();
        loadStats();
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
            loadStats();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void loadUserData() {
        firebaseManager.getCurrentUserData(new FirebaseManager.FirebaseCallback<User>() {
            @Override
            public void onSuccess(User result) {
                if (getActivity() != null) {
                    tvUserName.setText(result.getName());
                    tvLocation.setText(result.getLocation());
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

    private void loadStats() {
        firebaseManager.getAllPatients(new FirebaseManager.DatabaseCallback<List<Patient>>() {
            @Override
            public void onSuccess(List<Patient> result) {
                if (getActivity() != null) {
                    tvTotalPatients.setText(String.valueOf(result.size()));
                    tvActiveCases.setText(String.valueOf(result.size()));
                    tvTodayAppointments.setText("0");
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() != null) {
                    tvTotalPatients.setText("0");
                    tvActiveCases.setText("0");
                    tvTodayAppointments.setText("0");
                }
            }
        });
    }
}