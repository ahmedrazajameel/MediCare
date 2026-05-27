package com.medical.medicarepro.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medical.medicarepro.R;
import com.medical.medicarepro.ui.activities.AddPatientActivity;
import com.medical.medicarepro.ui.activities.PatientDetailsActivity;
import com.medical.medicarepro.ui.adapters.PatientAdapter;
import com.medical.medicarepro.utils.FirebaseManager;
import com.medical.medicarepro.models.Patient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CasesFragment extends Fragment implements PatientAdapter.OnPatientClickListener {
    private RecyclerView recyclerView;
    private EditText searchInput;
    private TextView clearSearch;
    private ProgressBar progressBar;
    private View emptyView;
    private MaterialButton btnAddCase, btnAddFirstCase;

    private PatientAdapter adapter;
    private List<Patient> patientList = new ArrayList<>();
    private List<Patient> filteredList = new ArrayList<>();
    private FirebaseManager firebaseManager;

    // Real-time listener
    private DatabaseReference patientsRef;
    private ValueEventListener patientsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cases, container, false);
        initViews(view);
        setupRecyclerView();
        setupListeners();
        setupRealtimeListener();
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewPatients);
        searchInput = view.findViewById(R.id.searchInput);
        clearSearch = view.findViewById(R.id.clearSearch);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        btnAddCase = view.findViewById(R.id.btnAddCase);
        btnAddFirstCase = view.findViewById(R.id.btnAddFirstCase);

        firebaseManager = FirebaseManager.getInstance();
    }

    private void setupRecyclerView() {
        adapter = new PatientAdapter(getContext(), filteredList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(false); // Show from beginning
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(true); // Ensure scrolling works
    }

    private void setupListeners() {
        btnAddCase.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddPatientActivity.class);
            startActivityForResult(intent, 100);
        });

        btnAddFirstCase.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddPatientActivity.class);
            startActivityForResult(intent, 100);
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPatients(s.toString());
                if (clearSearch != null) {
                    clearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (clearSearch != null) {
            clearSearch.setOnClickListener(v -> {
                searchInput.setText("");
                filterPatients("");
            });
        }
    }

    // Real-time listener for instant updates
    private void setupRealtimeListener() {
        progressBar.setVisibility(View.VISIBLE);
        patientsRef = FirebaseDatabase.getInstance().getReference("patients");
        patientsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                patientList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Patient patient = snapshot.getValue(Patient.class);
                    if (patient != null) {
                        patient.setId(snapshot.getKey());
                        patientList.add(patient);
                    }
                }

                // Sort patients by creation date (newest first or by name)
                Collections.sort(patientList, new Comparator<Patient>() {
                    @Override
                    public int compare(Patient p1, Patient p2) {
                        return p1.getFullName().compareToIgnoreCase(p2.getFullName());
                    }
                });

                filteredList.clear();
                filteredList.addAll(patientList);
                adapter.notifyDataSetChanged();

                // Show empty view if no patients
                if (filteredList.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                // Log the count for debugging
                android.util.Log.d("CasesFragment", "Total patients loaded: " + patientList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        };
        patientsRef.addValueEventListener(patientsListener);
    }

    private void filterPatients(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(patientList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Patient patient : patientList) {
                if (patient.getFullName().toLowerCase().contains(lowerQuery) ||
                        patient.getNhn().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(patient);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        android.util.Log.d("CasesFragment", "Filtered patients: " + filteredList.size());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == getActivity().RESULT_OK) {
            // Patient added, real-time listener will automatically update
            Toast.makeText(getContext(), "Patient added! List refreshing...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove listener to prevent memory leaks
        if (patientsRef != null && patientsListener != null) {
            patientsRef.removeEventListener(patientsListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ensure adapter is refreshed when returning to fragment
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPatientClick(Patient patient) {
        Intent intent = new Intent(getActivity(), PatientDetailsActivity.class);
        intent.putExtra("patient_id", patient.getId());
        intent.putExtra("patient_name", patient.getFullName());
        intent.putExtra("patient_age", patient.getAge());
        intent.putExtra("patient_sex", patient.getSex());
        intent.putExtra("patient_dob", patient.getDob());
        intent.putExtra("patient_nhn", patient.getNhn());
        startActivity(intent);
    }
}