package com.medical.medicarepro.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.medical.medicarepro.ui.fragments.PatientInfoFragment;
import com.medical.medicarepro.ui.fragments.PatientRecordsFragment;

public class PatientDetailsPagerAdapter extends FragmentStateAdapter {
    private String patientId;

    public PatientDetailsPagerAdapter(@NonNull FragmentActivity fragmentActivity, String patientId) {
        super(fragmentActivity);
        this.patientId = patientId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return PatientInfoFragment.newInstance(patientId);
            case 1:
                return PatientRecordsFragment.newInstance(patientId);
            default:
                return PatientInfoFragment.newInstance(patientId);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}