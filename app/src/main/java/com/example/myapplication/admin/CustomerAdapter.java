package com.example.myapplication.admin;

import android.graphics.Color;
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

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {

    private List<DocumentSnapshot> customerList;
    private OnBlockClickListener listener;

    public interface OnBlockClickListener {
        void onBlockClick(DocumentSnapshot doc);
    }

    public CustomerAdapter(List<DocumentSnapshot> customerList, OnBlockClickListener listener) {
        this.customerList = customerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot doc = customerList.get(position);

        // Get data with null checks
        String fName = doc.getString("firstName") != null ? doc.getString("firstName") : "";
        String lName = doc.getString("lastName") != null ? doc.getString("lastName") : "";
        String email = doc.getString("email") != null ? doc.getString("email") : "No Email";
        String age = doc.getString("age") != null ? doc.getString("age") : "N/A";
        String nic = doc.getString("nicLicense") != null ? doc.getString("nicLicense") : "N/A";
        String address = doc.getString("address") != null ? doc.getString("address") : "N/A";
        String status = doc.getString("status") != null ? doc.getString("status") : "active";

        // Set text to views
        holder.tvName.setText(fName + " " + lName);
        holder.tvEmail.setText(email);
        holder.tvAge.setText("Age: " + age);
        holder.tvNIC.setText("NIC: " + nic);
        holder.tvAddress.setText("Address: " + address);

        // Update Button UI based on status
        boolean isSuspended = "suspended".equalsIgnoreCase(status)
                || "blocked".equalsIgnoreCase(status);
        if (isSuspended) {
            holder.btnBlock.setText("Unsuspend");
            holder.btnBlock.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.btnBlock.setText("Suspend");
            holder.btnBlock.setBackgroundColor(Color.parseColor("#F44336")); // Red
        }

        holder.btnBlock.setOnClickListener(v -> listener.onBlockClick(doc));
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvAge, tvNIC, tvAddress;
        Button btnBlock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCustName);
            tvEmail = itemView.findViewById(R.id.tvCustEmail);
            tvAge = itemView.findViewById(R.id.tvCustAge);
            tvNIC = itemView.findViewById(R.id.tvCustNIC);
            tvAddress = itemView.findViewById(R.id.tvCustAddress);
            btnBlock = itemView.findViewById(R.id.btnBlock);
        }
    }
}