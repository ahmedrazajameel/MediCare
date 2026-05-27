package com.medical.medicarepro.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.medical.medicarepro.R;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddPatientActivity extends AppCompatActivity {
    private TextInputEditText etFirstName, etLastName, etNHN;
    private AutoCompleteTextView etSex;
    private TextInputEditText etDOB;
    private MaterialButton btnSubmit;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    private static final String TAG = "AddPatient";

    private int selectedAge = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        // Initialize Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("patients");

        initViews();
        setupToolbar();
        setupGenderDropdown();
        setupDatePicker();
        setupListeners();
    }

    private void initViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etNHN = findViewById(R.id.etNHN);
        etSex = findViewById(R.id.etSex);
        etDOB = findViewById(R.id.etDOB);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Patient");
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupGenderDropdown() {
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        etSex.setAdapter(adapter);
        etSex.setThreshold(1);
        etSex.setInputType(0); // Prevent keyboard, show dropdown only
    }

    private void setupDatePicker() {
        // Make DOB field non-editable and show calendar on click
        etDOB.setFocusable(false);
        etDOB.setClickable(true);
        etDOB.setOnClickListener(v -> showDatePickerDialog());
        etDOB.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePickerDialog();
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedYear + "-" +
                            String.format("%02d", (selectedMonth + 1)) + "-" +
                            String.format("%02d", selectedDay);
                    etDOB.setText(selectedDate);
                    calculateAge(selectedYear, selectedMonth, selectedDay);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void calculateAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        selectedAge = age;

        // Show age in a toast
        Toast.makeText(this, "Age calculated: " + age + " years", Toast.LENGTH_SHORT).show();
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> addPatient());
    }

    private void addPatient() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String nhn = etNHN.getText().toString().trim();
        String sex = etSex.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();

        if (firstName.isEmpty()) {
            etFirstName.setError("First name required");
            return;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Last name required");
            return;
        }

        if (nhn.isEmpty()) {
            etNHN.setError("NHN required");
            return;
        }

        if (sex.isEmpty()) {
            etSex.setError("Gender required");
            return;
        }

        if (dob.isEmpty()) {
            etDOB.setError("Date of birth required");
            return;
        }

        setLoading(true);

        Log.d(TAG, "Adding patient: " + firstName + " " + lastName);

        // Create patient data
        String patientId = databaseReference.push().getKey();

        Map<String, Object> patient = new HashMap<>();
        patient.put("id", patientId);
        patient.put("firstName", firstName);
        patient.put("lastName", lastName);
        patient.put("fullName", firstName + " " + lastName);
        patient.put("nhn", nhn);
        patient.put("sex", sex);
        patient.put("dob", dob);
        patient.put("age", selectedAge);
        patient.put("location", "CLINIC");
        patient.put("createdAt", System.currentTimeMillis());
        patient.put("updatedAt", System.currentTimeMillis());

        // Add to Realtime Database
        databaseReference.child(patientId).setValue(patient)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Log.d(TAG, "Patient added successfully! ID: " + patientId);
                    Toast.makeText(AddPatientActivity.this, "Patient added successfully!", Toast.LENGTH_SHORT).show();

                    // Set result to OK so CasesFragment knows to refresh
                    setResult(RESULT_OK);

                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    String errorMsg = e.getMessage();
                    Log.e(TAG, "Error adding patient: " + errorMsg, e);
                    Toast.makeText(AddPatientActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!isLoading);
        btnSubmit.setText(isLoading ? "Saving..." : "Save Patient");
    }
}