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
import com.medical.medicarepro.R;
import com.medical.medicarepro.ui.activities.AddPatientActivity;
import com.medical.medicarepro.ui.activities.PatientDetailsActivity;
import com.medical.medicarepro.ui.adapters.PatientAdapter;
import com.medical.medicarepro.utils.FirebaseManager;
import com.medical.medicarepro.models.Patient;
import java.util.ArrayList;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cases, container, false);
        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadPatients();
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        btnAddCase.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddPatientActivity.class)));

        btnAddFirstCase.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddPatientActivity.class)));

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

    private void loadPatients() {
        progressBar.setVisibility(View.VISIBLE);

        firebaseManager.getAllPatients(new FirebaseManager.DatabaseCallback<List<Patient>>() {
            @Override
            public void onSuccess(List<Patient> result) {
                progressBar.setVisibility(View.GONE);
                patientList.clear();
                patientList.addAll(result);
                filteredList.clear();
                filteredList.addAll(result);
                adapter.notifyDataSetChanged();

                if (filteredList.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error loading patients: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
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