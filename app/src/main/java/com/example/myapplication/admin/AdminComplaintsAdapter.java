package com.example.myapplication.admin;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AdminComplaintsAdapter extends RecyclerView.Adapter<AdminComplaintsAdapter.ComplaintViewHolder> {

    interface OnDismissClickListener {
        void onDismiss(ComplaintItem item);
    }

    static class ComplaintItem {
        final String id;
        final String customerId;
        final String renterId;
        final float rating;
        final String complaintText;
        final Timestamp timestamp;

        String customerName;
        String renterName;

        ComplaintItem(String id, String customerId, String renterId, float rating, String complaintText, Timestamp timestamp) {
            this.id = id;
            this.customerId = customerId;
            this.renterId = renterId;
            this.rating = rating;
            this.complaintText = complaintText;
            this.timestamp = timestamp;
        }
    }

    private final List<ComplaintItem> items;
    private final FirebaseFirestore db;
    private final OnDismissClickListener dismissClickListener;

    private final Map<String, String> customerNameCache = new HashMap<>();
    private final Map<String, String> renterNameCache = new HashMap<>();
    private final Set<String> customerLookupsInFlight = new HashSet<>();
    private final Set<String> renterLookupsInFlight = new HashSet<>();

    public AdminComplaintsAdapter(List<ComplaintItem> items, FirebaseFirestore db, OnDismissClickListener dismissClickListener) {
        this.items = items;
        this.db = db;
        this.dismissClickListener = dismissClickListener;
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_complaint, parent, false);
        return new ComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        ComplaintItem item = items.get(position);

        holder.tvComplaintText.setText(safe(item.complaintText, "No complaint text"));
        if (item.rating > 0f) {
            holder.ratingBar.setVisibility(View.VISIBLE);
            holder.ratingBar.setRating(item.rating);
        } else {
            holder.ratingBar.setVisibility(View.GONE);
        }

        if (item.timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            holder.tvTimestamp.setText("Date: " + sdf.format(item.timestamp.toDate()));
        } else {
            holder.tvTimestamp.setText("Date: -");
        }

        bindCustomerName(holder.tvCustomerName, item);
        bindRenterName(holder.tvRenterName, item);

        holder.btnDismiss.setOnClickListener(v -> dismissClickListener.onDismiss(item));
    }

    private void bindCustomerName(TextView view, ComplaintItem item) {
        if (!TextUtils.isEmpty(item.customerName)) {
            view.setText("Customer: " + item.customerName);
            return;
        }

        if (TextUtils.isEmpty(item.customerId)) {
            item.customerName = "Customer (deleted)";
            view.setText("Customer: " + item.customerName);
            return;
        }

        String cached = customerNameCache.get(item.customerId);
        if (!TextUtils.isEmpty(cached)) {
            item.customerName = cached;
            view.setText("Customer: " + cached);
            return;
        }

        view.setText("Customer: Loading...");
        if (!customerLookupsInFlight.add(item.customerId)) {
            return;
        }

        db.collection("Users").document(item.customerId).get()
                .addOnSuccessListener(doc -> {
                    String name = extractCustomerName(doc);
                    customerNameCache.put(item.customerId, name);
                    customerLookupsInFlight.remove(item.customerId);
                    applyCustomerName(item.customerId, name);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    String fallback = "Customer (deleted)";
                    customerNameCache.put(item.customerId, fallback);
                    customerLookupsInFlight.remove(item.customerId);
                    applyCustomerName(item.customerId, fallback);
                    notifyDataSetChanged();
                });
    }

    private void bindRenterName(TextView view, ComplaintItem item) {
        if (!TextUtils.isEmpty(item.renterName)) {
            view.setText("Renter: " + item.renterName);
            return;
        }

        if (TextUtils.isEmpty(item.renterId)) {
            item.renterName = "Renter (deleted)";
            view.setText("Renter: " + item.renterName);
            return;
        }

        String cached = renterNameCache.get(item.renterId);
        if (!TextUtils.isEmpty(cached)) {
            item.renterName = cached;
            view.setText("Renter: " + cached);
            return;
        }

        view.setText("Renter: Loading...");
        if (!renterLookupsInFlight.add(item.renterId)) {
            return;
        }

        db.collection("Users").document(item.renterId).get()
                .addOnSuccessListener(doc -> {
                    String name = extractRenterName(doc);
                    renterNameCache.put(item.renterId, name);
                    renterLookupsInFlight.remove(item.renterId);
                    applyRenterName(item.renterId, name);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    String fallback = "Renter (deleted)";
                    renterNameCache.put(item.renterId, fallback);
                    renterLookupsInFlight.remove(item.renterId);
                    applyRenterName(item.renterId, fallback);
                    notifyDataSetChanged();
                });
    }

    private void applyCustomerName(String customerId, String name) {
        for (ComplaintItem item : items) {
            if (customerId.equals(item.customerId)) {
                item.customerName = name;
            }
        }
    }

    private void applyRenterName(String renterId, String name) {
        for (ComplaintItem item : items) {
            if (renterId.equals(item.renterId)) {
                item.renterName = name;
            }
        }
    }

    private String extractCustomerName(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            return "Customer (deleted)";
        }

        String firstName = safe(doc.getString("firstName"), "");
        String lastName = safe(doc.getString("lastName"), "");
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isEmpty()) {
            return fullName;
        }

        String name = safe(doc.getString("name"), "");
        if (!name.isEmpty()) {
            return name;
        }

        return safe(doc.getString("email"), "Customer (deleted)");
    }

    private String extractRenterName(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            return "Renter (deleted)";
        }

        String orgName = safe(doc.getString("orgName"), "");
        String ownerName = safe(doc.getString("ownerName"), "");
        if (!orgName.isEmpty() && !ownerName.isEmpty()) {
            return orgName + " (" + ownerName + ")";
        }
        if (!orgName.isEmpty()) {
            return orgName;
        }
        if (!ownerName.isEmpty()) {
            return ownerName;
        }

        String firstName = safe(doc.getString("firstName"), "");
        String lastName = safe(doc.getString("lastName"), "");
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isEmpty()) {
            return fullName;
        }

        return safe(doc.getString("email"), "Renter (deleted)");
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        final TextView tvComplaintText;
        final TextView tvCustomerName;
        final TextView tvRenterName;
        final TextView tvTimestamp;
        final RatingBar ratingBar;
        final Button btnDismiss;

        ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            tvComplaintText = itemView.findViewById(R.id.tvAdminComplaintText);
            tvCustomerName = itemView.findViewById(R.id.tvAdminComplaintCustomer);
            tvRenterName = itemView.findViewById(R.id.tvAdminComplaintRenter);
            tvTimestamp = itemView.findViewById(R.id.tvAdminComplaintTimestamp);
            ratingBar = itemView.findViewById(R.id.ratingAdminComplaint);
            btnDismiss = itemView.findViewById(R.id.btnDismissComplaint);
        }
    }
}
