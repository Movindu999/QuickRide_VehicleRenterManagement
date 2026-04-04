package com.example.myapplication.customer;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class RenterVehiclesActivity extends AppCompatActivity {

    private String renterId;
    private String renterName;
    private String selectedFilter = "All";

    private MaterialAutoCompleteTextView actVehicleFilter;
    private RecyclerView rvVehicles;
    private View emptyStateCard;

    private final List<VehicleItem> vehicleItems = new ArrayList<>();
    private VehicleAdapter vehicleAdapter;

    private FirebaseFirestore db;
    private ListenerRegistration vehiclesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renter_vehicles);

        db = FirebaseFirestore.getInstance();

        renterId = getIntent().getStringExtra("renterId");
        renterName = getIntent().getStringExtra("renterName");

        if (TextUtils.isEmpty(renterId)) {
            Toast.makeText(this, "Renter not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle("Renter Vehicles");
        topAppBar.setSubtitle(TextUtils.isEmpty(renterName) ? null : renterName);
        topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        actVehicleFilter = findViewById(R.id.actVehicleFilter);
        rvVehicles = findViewById(R.id.rvRenterVehicles);
        emptyStateCard = findViewById(R.id.emptyStateCard);

        rvVehicles.setLayoutManager(new LinearLayoutManager(this));
        vehicleAdapter = new VehicleAdapter(vehicleItems, this::openBooking);
        rvVehicles.setAdapter(vehicleAdapter);

        String[] filters = new String[]{"All", "Car", "Van", "Motorbike", "Bus", "Tuktuk"};
        actVehicleFilter.setSimpleItems(filters);
        actVehicleFilter.setText(filters[0], false);
        actVehicleFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedFilter = filters[position];
            listenForVehicles();
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavCustomer);
        bottomNav.setSelectedItemId(R.id.nav_customer_renters);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_customer_renters) {
                Intent i = new Intent(this, ViewRentersActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                return true;
            }
            if (itemId == R.id.nav_customer_dashboard) {
                Intent i = new Intent(this, CustomerDashboardActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                return true;
            }
            if (itemId == R.id.nav_customer_bookings) {
                Intent i = new Intent(this, MyBookingsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                return true;
            }
            return false;
        });

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
        if (vehiclesListener != null) {
            vehiclesListener.remove();
            vehiclesListener = null;
        }

        Query query = db.collection("Vehicles").whereEqualTo("renterId", renterId);
        if (!"All".equalsIgnoreCase(selectedFilter)) {
            query = query.whereEqualTo("vehicleType", selectedFilter);
        }

        vehiclesListener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Failed to load vehicles: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            vehicleItems.clear();
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    boolean isBooked = Boolean.TRUE.equals(doc.getBoolean("isBooked"));
                    vehicleItems.add(new VehicleItem(
                            doc.getId(),
                            safe(doc.getString("vehicleType"), "Vehicle"),
                            safe(doc.getString("vehicleNumber"), "N/A"),
                            asDouble(doc.get("pricePerDay")),
                            firstImage(doc),
                            isBooked,
                            isBooked ? "Booked" : "Available"
                    ));
                }
            }

            vehicleAdapter.notifyDataSetChanged();
            emptyStateCard.setVisibility(vehicleItems.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private String firstImage(DocumentSnapshot doc) {
        String imageUrl = doc.getString("imageUrl");
        if (!TextUtils.isEmpty(imageUrl)) {
            return imageUrl;
        }
        Object raw = doc.get("imageUrls");
        if (raw instanceof List<?>) {
            for (Object item : (List<?>) raw) {
                if (item instanceof String && !((String) item).trim().isEmpty()) {
                    return (String) item;
                }
            }
        }
        return null;
    }

    private void openBooking(VehicleItem item) {
        if (item.isBooked) {
            Toast.makeText(this, "This vehicle is already booked", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("vehicleId", item.vehicleId);
        intent.putExtra("renterId", renterId);
        intent.putExtra("pricePerDay", item.pricePerDay);
        intent.putExtra("vehicleType", item.vehicleType);
        intent.putExtra("vehicleNumber", item.vehicleNumber);
        intent.putExtra("vehicleImageUrl", item.imageUrl);
        startActivity(intent);
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private double asDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0d;
    }

    private static class VehicleItem {
        final String vehicleId;
        final String vehicleType;
        final String vehicleNumber;
        final double pricePerDay;
        final String imageUrl;
        final boolean isBooked;
        final String bookingStatus;

        VehicleItem(
                String vehicleId,
                String vehicleType,
                String vehicleNumber,
                double pricePerDay,
                String imageUrl,
                boolean isBooked,
                String bookingStatus
        ) {
            this.vehicleId = vehicleId;
            this.vehicleType = vehicleType;
            this.vehicleNumber = vehicleNumber;
            this.pricePerDay = pricePerDay;
            this.imageUrl = imageUrl;
            this.isBooked = isBooked;
            this.bookingStatus = bookingStatus;
        }
    }

    private static class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

        interface OnVehicleClickListener {
            void onVehicleClick(VehicleItem item);
        }

        private final List<VehicleItem> items;
        private final OnVehicleClickListener listener;

        VehicleAdapter(List<VehicleItem> items, OnVehicleClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle_customer, parent, false);
            return new VehicleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
            VehicleItem item = items.get(position);
            holder.tvVehicleTitle.setText(item.vehicleType + " - " + item.vehicleNumber);
            holder.tvVehiclePrice.setText(String.format(
                    java.util.Locale.getDefault(),
                    "Price per day: %.2f | %s",
                    item.pricePerDay,
                    item.bookingStatus
            ));

            Glide.with(holder.itemView.getContext())
                    .load(item.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(holder.ivVehicle);

            holder.itemView.setAlpha(item.isBooked ? 0.7f : 1f);
            holder.itemView.setOnClickListener(v -> listener.onVehicleClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VehicleViewHolder extends RecyclerView.ViewHolder {
            final ImageView ivVehicle;
            final TextView tvVehicleTitle;
            final TextView tvVehiclePrice;

            VehicleViewHolder(@NonNull View itemView) {
                super(itemView);
                ivVehicle = itemView.findViewById(R.id.ivVehicle);
                tvVehicleTitle = itemView.findViewById(R.id.tvVehicleTitle);
                tvVehiclePrice = itemView.findViewById(R.id.tvVehiclePrice);
            }
        }
    }
}
