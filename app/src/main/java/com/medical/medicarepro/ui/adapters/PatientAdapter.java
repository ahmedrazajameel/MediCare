package com.medical.medicarepro.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.medical.medicarepro.R;
import com.medical.medicarepro.models.Patient;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {
    private Context context;
    private List<Patient> patients;
    private OnPatientClickListener listener;

    public interface OnPatientClickListener {
        void onPatientClick(Patient patient);
    }

    public PatientAdapter(Context context, List<Patient> patients, OnPatientClickListener listener) {
        this.context = context;
        this.patients = patients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patients.get(position);
        holder.bind(patient);
        holder.itemView.setOnClickListener(v -> listener.onPatientClick(patient));
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvNHN, tvLocation, tvSex, tvDOB;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvNHN = itemView.findViewById(R.id.tvNHN);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvSex = itemView.findViewById(R.id.tvSex);
            tvDOB = itemView.findViewById(R.id.tvDOB);
        }

        void bind(Patient patient) {
            tvPatientName.setText(patient.getFullName());
            tvNHN.setText("NHN: " + patient.getNhn());
            tvLocation.setText(patient.getLocation() != null ? patient.getLocation() : "CLINIC");
            tvSex.setText(patient.getSex());
            tvDOB.setText(patient.getDob());
        }
    }
}