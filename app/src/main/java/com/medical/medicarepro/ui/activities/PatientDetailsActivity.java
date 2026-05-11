package com.medical.medicarepro.ui.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.medical.medicarepro.R;
import com.medical.medicarepro.ui.adapters.PatientDetailsPagerAdapter;
import com.medical.medicarepro.models.Patient;

public class PatientDetailsActivity extends AppCompatActivity {
    private TextView tvPatientNameHeader, tvAgeHeader, tvSexHeader, tvDOBHeader, tvFolderNoHeader;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private PatientDetailsPagerAdapter pagerAdapter;
    private String patientId;
    private Patient patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);

        getPatientDataFromIntent();
        initViews();
        setupToolbar();
        setupHeader();
        setupViewPager();
    }

    private void getPatientDataFromIntent() {
        patientId = getIntent().getStringExtra("patient_id");

        patient = new Patient();
        patient.setId(patientId);
        patient.setFirstName(getIntent().getStringExtra("patient_name"));
        patient.setAge(getIntent().getIntExtra("patient_age", 0));
        patient.setSex(getIntent().getStringExtra("patient_sex"));
        patient.setDob(getIntent().getStringExtra("patient_dob"));
        patient.setNhn(getIntent().getStringExtra("patient_nhn"));
    }

    private void initViews() {
        tvPatientNameHeader = findViewById(R.id.tvPatientNameHeader);
        tvAgeHeader = findViewById(R.id.tvAgeHeader);
        tvSexHeader = findViewById(R.id.tvSexHeader);
        tvDOBHeader = findViewById(R.id.tvDOBHeader);
        tvFolderNoHeader = findViewById(R.id.tvFolderNoHeader);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupHeader() {
        tvPatientNameHeader.setText(patient.getFullName());
        tvAgeHeader.setText("Age: " + patient.getAge());
        tvSexHeader.setText("Sex: " + patient.getSex());
        tvDOBHeader.setText("DOB: " + patient.getDob());
        tvFolderNoHeader.setText("Folder No: " + patient.getNhn());
    }

    private void setupViewPager() {
        pagerAdapter = new PatientDetailsPagerAdapter(this, patientId);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Info");
                            break;
                        case 1:
                            tab.setText("Records");
                            break;
                    }
                }
        ).attach();
    }
}