package com.example.myapplication.medica;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import java.util.List;

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

        // 👉 Clique sur un élément = ouvrir AddMedActivity en mode modification
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddMed.class);
            intent.putExtra("isEditMode", true);
            intent.putExtra("id", medicament.getId());
            intent.putExtra("nom", medicament.getNom());
            intent.putExtra("dose", medicament.getDose());
            intent.putExtra("heure", medicament.getHeurePrise());
            v.getContext().startActivity(intent);
        });
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
