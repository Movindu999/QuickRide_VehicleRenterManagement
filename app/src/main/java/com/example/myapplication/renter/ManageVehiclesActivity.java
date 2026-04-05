package com.example.myapplication.renter;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManageVehiclesActivity extends AppCompatActivity {

    private String vehicleType;
    private RecyclerView vehicleRecyclerView;
    private FloatingActionButton fabAddVehicle;
    private View emptyStateCard;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration vehiclesListener;

    private final List<DocumentSnapshot> vehicleList = new ArrayList<>();
    private VehicleAdapter vehicleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_vehicles);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        vehicleType = getIntent().getStringExtra("vehicleType");
        if (vehicleType == null) {
            vehicleType = "Vehicle";
        }

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle("Manage " + vehicleType + "s");
        topAppBar.setSubtitle(getString(R.string.manage_vehicle_subtitle));
        topAppBar.setNavigationOnClickListener(v -> finish());

        vehicleRecyclerView = findViewById(R.id.vehicleRecyclerView);
        fabAddVehicle = findViewById(R.id.fabAddVehicle);
        emptyStateCard = findViewById(R.id.emptyStateCard);

        vehicleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        vehicleAdapter = new VehicleAdapter();
        vehicleRecyclerView.setAdapter(vehicleAdapter);

        fabAddVehicle.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditVehicleActivity.class);
            intent.putExtra("vehicleType", vehicleType);
            intent.putExtra("isEditMode", false);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenForVehicles();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (vehiclesListener != null) {
            vehiclesListener.remove();
            vehiclesListener = null;
        }
    }

    private void listenForVehicles() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        String renterId = mAuth.getCurrentUser().getUid();
        Query query = db.collection("Vehicles")
                .whereEqualTo("renterId", renterId);

        if (!"Vehicle".equalsIgnoreCase(vehicleType)) {
            query = query.whereEqualTo("vehicleType", vehicleType);
        }

        vehiclesListener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Failed to load vehicles: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            vehicleList.clear();
            if (value != null) {
                vehicleList.addAll(value.getDocuments());
                vehicleList.sort((left, right) -> {
                    Timestamp leftUpdatedAt = left.getTimestamp("updatedAt");
                    Timestamp rightUpdatedAt = right.getTimestamp("updatedAt");

                    if (leftUpdatedAt == null && rightUpdatedAt == null) return 0;
                    if (leftUpdatedAt == null) return 1;
                    if (rightUpdatedAt == null) return -1;
                    return Long.compare(rightUpdatedAt.toDate().getTime(), leftUpdatedAt.toDate().getTime());
                });
            }
            vehicleAdapter.notifyDataSetChanged();
            toggleEmptyState();
        });
    }

    private void toggleEmptyState() {
        if (emptyStateCard == null) {
            return;
        }
        emptyStateCard.setVisibility(vehicleList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openEditVehicle(DocumentSnapshot doc) {
        Intent intent = new Intent(this, AddEditVehicleActivity.class);
        intent.putExtra("isEditMode", true);
        intent.putExtra("vehicleId", doc.getId());
        intent.putExtra("vehicleType", doc.getString("vehicleType"));
        startActivity(intent);
    }

    private void confirmDelete(DocumentSnapshot doc) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Vehicle")
                .setMessage("Are you sure you want to remove this vehicle?")
                .setPositiveButton("Remove", (dialog, which) -> deleteVehicle(doc))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteVehicle(DocumentSnapshot doc) {
        db.collection("Vehicles").document(doc.getId())
                .delete()
                .addOnSuccessListener(unused -> Toast.makeText(this, "Vehicle removed", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove vehicle", Toast.LENGTH_SHORT).show());
    }

    private class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

        @NonNull
        @Override
        public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_vehicle_manage, parent, false);
            return new VehicleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
            DocumentSnapshot doc = vehicleList.get(position);

            String vehicleNumber = doc.getString("vehicleNumber");
            String type = doc.getString("vehicleType");
            Double price = doc.getDouble("pricePerDay");
            Double driverFee = doc.getDouble("driverFeePerDay");
            boolean isBooked = Boolean.TRUE.equals(doc.getBoolean("isBooked"));

            String imageUrl = doc.getString("imageUrl");
            if (TextUtils.isEmpty(imageUrl)) {
                List<String> imageUrls = (List<String>) doc.get("imageUrls");
                if (imageUrls != null && !imageUrls.isEmpty()) {
                    imageUrl = imageUrls.get(0);
                }
            }

            holder.title.setText((type == null ? "Vehicle" : type) + " - " + (vehicleNumber == null ? "N/A" : vehicleNumber));
            holder.price.setText(String.format(Locale.getDefault(), "Price per day: %.2f", price == null ? 0d : price));
            holder.driverFee.setText(String.format(Locale.getDefault(), "Driver fee per day: %.2f", driverFee == null ? 0d : driverFee));

            if (isBooked) {
                holder.status.setText("Booked");
                holder.status.setTextColor(0xFFC0392B);
            } else {
                holder.status.setText("Available");
                holder.status.setTextColor(0xFF155BC0);
            }

            Glide.with(ManageVehiclesActivity.this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(holder.icon);

            holder.itemView.setOnClickListener(v -> openEditVehicle(doc));
            holder.itemView.setOnLongClickListener(v -> {
                confirmDelete(doc);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return vehicleList.size();
        }

        class VehicleViewHolder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView title;
            final TextView price;
            final TextView driverFee;
            final TextView status;

            VehicleViewHolder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.ivManageVehicle);
                title = itemView.findViewById(R.id.tvManageVehicleTitle);
                price = itemView.findViewById(R.id.tvManageVehiclePrice);
                driverFee = itemView.findViewById(R.id.tvManageVehicleDriverFee);
                status = itemView.findViewById(R.id.tvManageVehicleStatus);
            }
        }
    }
}
