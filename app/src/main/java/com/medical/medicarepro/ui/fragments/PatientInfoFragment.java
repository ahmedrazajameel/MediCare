package com.medical.medicarepro.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.medical.medicarepro.R;
import com.medical.medicarepro.models.Patient;
import com.medical.medicarepro.utils.FirebaseManager;
import java.util.List;

public class PatientInfoFragment extends Fragment {
    private static final String ARG_PATIENT_ID = "patient_id";
    private String patientId;
    private TextView tvDiagnosis, tvFullName, tvNHNDetail, tvBloodGroup, tvEthnicity, tvPhone, tvEmail, tvAddress;
    private FirebaseManager firebaseManager;

    public static PatientInfoFragment newInstance(String patientId) {
        PatientInfoFragment fragment = new PatientInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_patient_info, container, false);
        initViews(view);
        loadPatientData();
        setupLongPressEdits();
        return view;
    }

    private void initViews(View view) {
        tvDiagnosis = view.findViewById(R.id.tvDiagnosis);
        tvFullName = view.findViewById(R.id.tvFullName);
        tvNHNDetail = view.findViewById(R.id.tvNHNDetail);
        tvBloodGroup = view.findViewById(R.id.tvBloodGroup);
        tvEthnicity = view.findViewById(R.id.tvEthnicity);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvAddress = view.findViewById(R.id.tvAddress);
    }

    private void loadPatientData() {
        firebaseManager.getAllPatients(new FirebaseManager.DatabaseCallback<List<Patient>>() {
            @Override
            public void onSuccess(List<Patient> patients) {
                for (Patient p : patients) {
                    if (p.getId().equals(patientId)) {
                        displayPatientData(p);
                        break;
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error loading patient data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPatientData(Patient patient) {
        tvFullName.setText("Full Name: " + patient.getFullName());
        tvNHNDetail.setText("NHN: " + patient.getNhn());
        tvBloodGroup.setText("Blood Group: " + (patient.getBloodGroup() != null ? patient.getBloodGroup() : "Not specified"));
        tvEthnicity.setText("Ethnicity: " + (patient.getEthnicity() != null ? patient.getEthnicity() : "Not specified"));
        tvPhone.setText("Phone: " + (patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "Not specified"));
        tvEmail.setText("Email: " + (patient.getEmail() != null ? patient.getEmail() : "Not specified"));
        tvAddress.setText("Address: " + (patient.getAddress() != null ? patient.getAddress() : "Not specified"));
        tvDiagnosis.setText(patient.getSummary() != null ? patient.getSummary() : "No diagnosis recorded");
    }

    private void setupLongPressEdits() {
        tvDiagnosis.setOnLongClickListener(v -> {
            showEditDialog("Diagnosis", tvDiagnosis.getText().toString(), value -> {
                tvDiagnosis.setText(value);
                updatePatientField("summary", value);
            });
            return true;
        });

        tvBloodGroup.setOnLongClickListener(v -> {
            String current = tvBloodGroup.getText().toString().replace("Blood Group: ", "");
            showEditDialog("Blood Group", current, value -> {
                tvBloodGroup.setText("Blood Group: " + value);
                updatePatientField("bloodGroup", value);
            });
            return true;
        });

        tvPhone.setOnLongClickListener(v -> {
            String current = tvPhone.getText().toString().replace("Phone: ", "");
            showEditDialog("Phone Number", current, value -> {
                tvPhone.setText("Phone: " + value);
                updatePatientField("phoneNumber", value);
            });
            return true;
        });
    }

    private void showEditDialog(String field, String currentValue, OnValueSaved listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit " + field);

        final EditText input = new EditText(requireContext());
        input.setText(currentValue);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newValue = input.getText().toString();
            listener.onSave(newValue);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updatePatientField(String field, String value) {
        firebaseManager.getAllPatients(new FirebaseManager.DatabaseCallback<List<Patient>>() {
            @Override
            public void onSuccess(List<Patient> patients) {
                for (Patient p : patients) {
                    if (p.getId().equals(patientId)) {
                        switch (field) {
                            case "summary":
                                p.setSummary(value);
                                break;
                            case "bloodGroup":
                                p.setBloodGroup(value);
                                break;
                            case "phoneNumber":
                                p.setPhoneNumber(value);
                                break;
                        }
                        firebaseManager.updatePatient(p, new FirebaseManager.FirebaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error loading patient", Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface OnValueSaved {
        void onSave(String value);
    }
}