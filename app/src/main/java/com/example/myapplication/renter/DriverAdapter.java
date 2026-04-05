package com.example.myapplication.renter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {
    private final List<DocumentSnapshot> driverList;
    private final OnDriverDeleteListener deleteListener;

    public interface OnDriverDeleteListener {
        void onDeleteClick(DocumentSnapshot documentSnapshot);
    }

    public DriverAdapter(List<DocumentSnapshot> driverList, OnDriverDeleteListener deleteListener) {
        this.driverList = driverList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        DocumentSnapshot doc = driverList.get(position);

        String firstName = safe(doc.getString("firstName"));
        String lastName = safe(doc.getString("lastName"));
        String fullName = (firstName + " " + lastName).trim();

        holder.tvName.setText(fullName.isEmpty()
                ? holder.itemView.getContext().getString(R.string.driver_default_name)
                : fullName);

        holder.tvEmail.setText(holder.itemView.getContext().getString(
                R.string.driver_email_format,
                safe(doc.getString("email"))
        ));
        holder.tvContact.setText(holder.itemView.getContext().getString(
                R.string.driver_contact_format,
                safe(doc.getString("contactNumber"))
        ));

        holder.tvDetails.setText(holder.itemView.getContext().getString(
                R.string.driver_details_format,
                safe(doc.getString("idNumber")),
                safe(doc.getString("licenseNumber")),
                safe(doc.getString("age"))
        ));

        holder.btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(doc));
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    public static class DriverViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvContact, tvDetails;
        Button btnDelete;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDriverName);
            tvEmail = itemView.findViewById(R.id.tvDriverEmail);
            tvContact = itemView.findViewById(R.id.tvDriverContact);
            tvDetails = itemView.findViewById(R.id.tvDriverDetails);
            btnDelete = itemView.findViewById(R.id.btnDeleteDriver);
        }
    }
}