package com.example.myapplication.medica;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.example.myapplication.R; // ✅ Remplace par le bon nom de package

public class MedicamentAdapter extends RecyclerView.Adapter<MedicamentAdapter.MedicamentViewHolder> {

    private List<Medicament> medicamentList;

    public MedicamentAdapter(List<Medicament> medicamentList) {
        this.medicamentList = medicamentList;
    }

    @NonNull
    @Override
    public MedicamentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicament, parent, false);
        return new MedicamentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicamentViewHolder holder, int position) {
        Medicament medicament = medicamentList.get(position);
        holder.tvName.setText(medicament.getNom());
        holder.tvDoseTime.setText("Dose: " + medicament.getDose() + " | Heure: " + medicament.getHeurePrise());

    }

    @Override
    public int getItemCount() {
        return medicamentList.size();
    }

    public static class MedicamentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDoseTime;

        public MedicamentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_medicine_name);
            tvDoseTime = itemView.findViewById(R.id.tv_dose_time);
        }
    }
}
