package com.medical.medicarepro.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.medical.medicarepro.R;
import com.medical.medicarepro.models.HealthRecord;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RecordViewHolder> {
    private Context context;
    private List<HealthRecord> records;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public RecordsAdapter(Context context, List<HealthRecord> records) {
        this.context = context;
        this.records = records;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        HealthRecord record = records.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvValue, tvDate, tvRecordedBy, tvNote;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvValue = itemView.findViewById(R.id.tvValue);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvRecordedBy = itemView.findViewById(R.id.tvRecordedBy);
            tvNote = itemView.findViewById(R.id.tvNote);
        }

        void bind(HealthRecord record) {
            tvType.setText(record.getType());
            tvValue.setText(record.getValue() + " " + record.getUnit());

            if (record.getRecordedAt() > 0) {
                tvDate.setText(dateFormat.format(record.getRecordedAt()));
            }

            tvRecordedBy.setText("Recorded by: " + record.getRecordedBy());

            if (record.getNote() != null && !record.getNote().isEmpty()) {
                tvNote.setVisibility(View.VISIBLE);
                tvNote.setText("Note: " + record.getNote());
            } else {
                tvNote.setVisibility(View.GONE);
            }
        }
    }
}