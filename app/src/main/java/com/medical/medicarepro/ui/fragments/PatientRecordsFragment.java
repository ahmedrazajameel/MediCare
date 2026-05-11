package com.medical.medicarepro.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.medical.medicarepro.R;
import com.medical.medicarepro.models.HealthRecord;
import com.medical.medicarepro.ui.adapters.RecordsAdapter;
import com.medical.medicarepro.utils.FirebaseManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PatientRecordsFragment extends Fragment {
    private static final String ARG_PATIENT_ID = "patient_id";
    private String patientId;
    private RecyclerView recyclerViewRecords;
    private LinearLayout emptyRecordsView;
    private MaterialCardView cardBMI, cardBloodPressure, cardGlucose;

    private RecordsAdapter adapter;
    private List<HealthRecord> recordsList = new ArrayList<>();
    private FirebaseManager firebaseManager;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public static PatientRecordsFragment newInstance(String patientId) {
        PatientRecordsFragment fragment = new PatientRecordsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PATIENT_ID, patientId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            patientId = getArguments().getString(ARG_PATIENT_ID);
        }
        firebaseManager = FirebaseManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_records, container, false);
        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadRecords();
        return view;
    }

    private void initViews(View view) {
        recyclerViewRecords = view.findViewById(R.id.recyclerViewRecords);
        emptyRecordsView = view.findViewById(R.id.emptyRecordsView);
        cardBMI = view.findViewById(R.id.cardBMI);
        cardBloodPressure = view.findViewById(R.id.cardBloodPressure);
        cardGlucose = view.findViewById(R.id.cardGlucose);
    }

    private void setupRecyclerView() {
        adapter = new RecordsAdapter(getContext(), recordsList);
        recyclerViewRecords.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRecords.setAdapter(adapter);
    }

    private void setupListeners() {
        cardBMI.setOnClickListener(v -> showAddRecordDialog("BMI", "kg/m²"));
        cardBloodPressure.setOnClickListener(v -> showAddRecordDialog("Blood Pressure", "mmHg"));
        cardGlucose.setOnClickListener(v -> showAddRecordDialog("Glucose", "mmol/L"));
    }

    private void loadRecords() {
        firebaseManager.getPatientRecords(patientId, new FirebaseManager.DatabaseCallback<List<HealthRecord>>() {
            @Override
            public void onSuccess(List<HealthRecord> result) {
                recordsList.clear();
                recordsList.addAll(result);
                adapter.notifyDataSetChanged();

                if (recordsList.isEmpty()) {
                    emptyRecordsView.setVisibility(View.VISIBLE);
                    recyclerViewRecords.setVisibility(View.GONE);
                } else {
                    emptyRecordsView.setVisibility(View.GONE);
                    recyclerViewRecords.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error loading records: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                emptyRecordsView.setVisibility(View.VISIBLE);
                recyclerViewRecords.setVisibility(View.GONE);
            }
        });
    }

    private void showAddRecordDialog(String type, String unit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add " + type);

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_record, null);
        EditText etValue = dialogView.findViewById(R.id.etValue);
        EditText etNote = dialogView.findViewById(R.id.etNote);
        TextView tvUnit = dialogView.findViewById(R.id.tvUnit);

        tvUnit.setText(unit);
        builder.setView(dialogView);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String value = etValue.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (value.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a value", Toast.LENGTH_SHORT).show();
                return;
            }

            HealthRecord record = new HealthRecord();
            record.setPatientId(patientId);
            record.setType(type);
            record.setValue(value);
            record.setUnit(unit);
            record.setNote(note);
            record.setRecordedBy(firebaseManager.getCurrentUser() != null ?
                    firebaseManager.getCurrentUser().getEmail() : "Unknown");
            record.setRecordedAt(System.currentTimeMillis());

            firebaseManager.addHealthRecord(record, new FirebaseManager.FirebaseCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Toast.makeText(getContext(), type + " added successfully", Toast.LENGTH_SHORT).show();
                    loadRecords();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}