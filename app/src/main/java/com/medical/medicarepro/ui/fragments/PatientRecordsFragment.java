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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
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
        cardBMI.setOnClickListener(v -> showBMIDialog());
        cardBloodPressure.setOnClickListener(v -> showBloodPressureDialog());
        cardGlucose.setOnClickListener(v -> showGlucoseDialog());
    }

    // BMI Calculator with Height, Weight, and Category
    private void showBMIDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Calculate BMI");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bmi_calculator, null);
        TextInputEditText etHeight = dialogView.findViewById(R.id.etHeight);
        TextInputEditText etWeight = dialogView.findViewById(R.id.etWeight);
        TextView tvBMIPreview = dialogView.findViewById(R.id.tvBMIPreview);
        TextView tvCategoryPreview = dialogView.findViewById(R.id.tvCategoryPreview);
        TextInputEditText etNote = dialogView.findViewById(R.id.etNote);

        // Real-time BMI calculation
        android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateAndDisplayBMI(etHeight, etWeight, tvBMIPreview, tvCategoryPreview);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };

        etHeight.addTextChangedListener(textWatcher);
        etWeight.addTextChangedListener(textWatcher);

        builder.setView(dialogView);

        builder.setPositiveButton("Save BMI", (dialog, which) -> {
            String heightStr = etHeight.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (heightStr.isEmpty() || weightStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter height and weight", Toast.LENGTH_SHORT).show();
                return;
            }

            double height = Double.parseDouble(heightStr);
            double weight = Double.parseDouble(weightStr);
            double bmi = calculateBMI(height, weight);
            String bmiCategory = getBMICategory(bmi);
            String bmiValue = String.format("%.1f", bmi);

            String fullNote = "Height: " + height + " cm, Weight: " + weight + " kg\n" +
                    "BMI: " + bmiValue + " (" + bmiCategory + ")\n" +
                    (note.isEmpty() ? "" : "Note: " + note);

            saveHealthRecord("BMI", bmiValue, "kg/m²", fullNote);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void calculateAndDisplayBMI(EditText etHeight, EditText etWeight, TextView tvBMI, TextView tvCategory) {
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        if (!heightStr.isEmpty() && !weightStr.isEmpty()) {
            try {
                double height = Double.parseDouble(heightStr);
                double weight = Double.parseDouble(weightStr);
                double bmi = calculateBMI(height, weight);
                String category = getBMICategory(bmi);
                tvBMI.setText(String.format("BMI: %.1f kg/m²", bmi));
                tvBMI.setVisibility(View.VISIBLE);
                tvCategory.setText("Category: " + category);
                tvCategory.setVisibility(View.VISIBLE);
            } catch (NumberFormatException e) {
                tvBMI.setVisibility(View.GONE);
                tvCategory.setVisibility(View.GONE);
            }
        } else {
            tvBMI.setVisibility(View.GONE);
            tvCategory.setVisibility(View.GONE);
        }
    }

    private double calculateBMI(double heightCm, double weightKg) {
        double heightM = heightCm / 100;
        return weightKg / (heightM * heightM);
    }

    private String getBMICategory(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi >= 18.5 && bmi < 25) {
            return "Normal weight";
        } else if (bmi >= 25 && bmi < 30) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }

    // Blood Pressure Dialog
    private void showBloodPressureDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Record Blood Pressure");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_blood_pressure, null);
        TextInputEditText etSystolic = dialogView.findViewById(R.id.etSystolic);
        TextInputEditText etDiastolic = dialogView.findViewById(R.id.etDiastolic);
        TextView tvBPPreview = dialogView.findViewById(R.id.tvBPPreview);
        TextInputEditText etNote = dialogView.findViewById(R.id.etNote);

        android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String systolicStr = etSystolic.getText().toString().trim();
                String diastolicStr = etDiastolic.getText().toString().trim();

                if (!systolicStr.isEmpty() && !diastolicStr.isEmpty()) {
                    try {
                        int systolic = Integer.parseInt(systolicStr);
                        int diastolic = Integer.parseInt(diastolicStr);
                        String category = getBPCategory(systolic, diastolic);
                        tvBPPreview.setText(String.format("BP: %d/%d mmHg (%s)", systolic, diastolic, category));
                        tvBPPreview.setVisibility(View.VISIBLE);
                    } catch (NumberFormatException e) {
                        tvBPPreview.setVisibility(View.GONE);
                    }
                } else {
                    tvBPPreview.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };

        etSystolic.addTextChangedListener(textWatcher);
        etDiastolic.addTextChangedListener(textWatcher);

        builder.setView(dialogView);

        builder.setPositiveButton("Save BP", (dialog, which) -> {
            String systolicStr = etSystolic.getText().toString().trim();
            String diastolicStr = etDiastolic.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (systolicStr.isEmpty() || diastolicStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter both systolic and diastolic values", Toast.LENGTH_SHORT).show();
                return;
            }

            int systolic = Integer.parseInt(systolicStr);
            int diastolic = Integer.parseInt(diastolicStr);
            String category = getBPCategory(systolic, diastolic);
            String bpValue = systolic + "/" + diastolic;
            String fullNote = "Systolic: " + systolic + " mmHg, Diastolic: " + diastolic + " mmHg\n" +
                    "Category: " + category + "\n" +
                    (note.isEmpty() ? "" : "Note: " + note);

            saveHealthRecord("Blood Pressure", bpValue, "mmHg", fullNote);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private String getBPCategory(int systolic, int diastolic) {
        if (systolic < 120 && diastolic < 80) {
            return "Normal";
        } else if (systolic >= 120 && systolic < 130 && diastolic < 80) {
            return "Elevated";
        } else if ((systolic >= 130 && systolic < 140) || (diastolic >= 80 && diastolic < 90)) {
            return "High Blood Pressure (Stage 1)";
        } else if (systolic >= 140 || diastolic >= 90) {
            return "High Blood Pressure (Stage 2)";
        } else {
            return "Hypertensive Crisis";
        }
    }

    // Glucose Dialog
    private void showGlucoseDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Record Glucose");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_glucose, null);
        TextInputEditText etGlucose = dialogView.findViewById(R.id.etGlucose);
        TextView tvGlucosePreview = dialogView.findViewById(R.id.tvGlucosePreview);
        TextInputEditText etNote = dialogView.findViewById(R.id.etNote);

        android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String glucoseStr = etGlucose.getText().toString().trim();

                if (!glucoseStr.isEmpty()) {
                    try {
                        double glucose = Double.parseDouble(glucoseStr);
                        String category = getGlucoseCategory(glucose);
                        tvGlucosePreview.setText(String.format("Glucose: %.1f mmol/L (%s)", glucose, category));
                        tvGlucosePreview.setVisibility(View.VISIBLE);
                    } catch (NumberFormatException e) {
                        tvGlucosePreview.setVisibility(View.GONE);
                    }
                } else {
                    tvGlucosePreview.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };

        etGlucose.addTextChangedListener(textWatcher);

        builder.setView(dialogView);

        builder.setPositiveButton("Save Glucose", (dialog, which) -> {
            String glucoseStr = etGlucose.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (glucoseStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter glucose value", Toast.LENGTH_SHORT).show();
                return;
            }

            double glucose = Double.parseDouble(glucoseStr);
            String category = getGlucoseCategory(glucose);
            String glucoseValue = String.format("%.1f", glucose);
            String fullNote = "Glucose: " + glucoseValue + " mmol/L\n" +
                    "Category: " + category + "\n" +
                    (note.isEmpty() ? "" : "Note: " + note);

            saveHealthRecord("Glucose", glucoseValue, "mmol/L", fullNote);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private String getGlucoseCategory(double glucose) {
        if (glucose < 3.9) {
            return "Low (Hypoglycemia)";
        } else if (glucose >= 3.9 && glucose < 5.6) {
            return "Normal (Fasting)";
        } else if (glucose >= 5.6 && glucose < 7.0) {
            return "Prediabetes";
        } else {
            return "High (Diabetes)";
        }
    }

    private void saveHealthRecord(String type, String value, String unit, String note) {
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
                Toast.makeText(getContext(), type + " recorded successfully!", Toast.LENGTH_SHORT).show();
                loadRecords(); // Refresh the records list immediately
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
}