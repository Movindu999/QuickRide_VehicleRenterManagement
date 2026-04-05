package com.example.myapplication.renter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RenterBookingsActivity extends AppCompatActivity {

    private RecyclerView rvBookings;
    private View emptyStateCard;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final List<BookingItem> bookingItems = new ArrayList<>();
    private BookingAdapter bookingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renter_bookings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle("Bookings");
        topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        rvBookings = findViewById(R.id.rvRenterBookings);
        emptyStateCard = findViewById(R.id.emptyStateCard);

        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        bookingAdapter = new BookingAdapter(bookingItems, this::openDriverProfile, this::cancelBooking);
        rvBookings.setAdapter(bookingAdapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavRenter);
        bottomNav.setSelectedItemId(R.id.nav_renter_bookings);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_renter_bookings) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_renter_dashboard) {
                intent = new Intent(this, RenterDashboardActivity.class);
            } else if (itemId == R.id.nav_renter_my_vehicles) {
                intent = new Intent(this, ManageVehiclesActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });

        loadBookings();
    }

    private void loadBookings() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        String renterId = mAuth.getCurrentUser().getUid();
        db.collection("Bookings")
                .whereEqualTo("renterId", renterId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load bookings: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    bookingItems.clear();
                    bookingAdapter.notifyDataSetChanged();

                    if (value == null || value.isEmpty()) {
                        emptyStateCard.setVisibility(View.VISIBLE);
                        return;
                    }

                    emptyStateCard.setVisibility(View.GONE);

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        String customerId = safe(doc.getString("customerId"), "");
                        String vehicleLabel = safe(doc.getString("vehicleLabel"), "Vehicle");
                        String startDate = safe(doc.getString("startDate"), "N/A");
                        String endDate = safe(doc.getString("endDate"), "N/A");
                        String driverId = safe(doc.getString("driverId"), "");
                        double totalPrice = asDouble(doc.get("totalPrice"));
                        String status = safe(doc.getString("status"), "Booked");
                        String vehicleId = safe(doc.getString("vehicleId"), "");
                        String bookingId = doc.getId();

                        resolveCustomerName(customerId, customerName -> {
                            bookingItems.add(new BookingItem(
                                    bookingId,
                                    vehicleId,
                                    customerName,
                                    vehicleLabel,
                                    startDate,
                                    endDate,
                                    totalPrice,
                                    status,
                                    driverId,
                                    customerId
                            ));
                            bookingAdapter.notifyDataSetChanged();
                            emptyStateCard.setVisibility(bookingItems.isEmpty() ? View.VISIBLE : View.GONE);
                        });
                    }
                });
    }

    private void resolveCustomerName(String customerId, OnCustomerNameResolved callback) {
        if (TextUtils.isEmpty(customerId)) {
            callback.onResolved("Customer");
            return;
        }

        db.collection("Users").document(customerId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onResolved(buildCustomerName(doc));
                    } else {
                        db.collection("users").document(customerId).get()
                                .addOnSuccessListener(lowerDoc -> callback.onResolved(buildCustomerName(lowerDoc)))
                                .addOnFailureListener(e -> callback.onResolved("Customer"));
                    }
                })
                .addOnFailureListener(e -> db.collection("users").document(customerId).get()
                        .addOnSuccessListener(lowerDoc -> callback.onResolved(buildCustomerName(lowerDoc)))
                        .addOnFailureListener(inner -> callback.onResolved("Customer")));
    }

    private String buildCustomerName(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            return "Customer";
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

        return safe(doc.getString("email"), "Customer");
    }

    private void openDriverProfile(BookingItem item) {
        if (item.driverId == null || item.driverId.isEmpty()) {
            Toast.makeText(this, "Customer opted for self-drive", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, DriverProfileActivity.class);
        intent.putExtra("driverId", item.driverId);
        startActivity(intent);
    }

    private double asDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0d;
    }

    private void cancelBooking(BookingItem item) {
        if (!"Booked".equalsIgnoreCase(safe(item.status, ""))) {
            Toast.makeText(this, "Only active bookings can be cancelled", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(item.bookingId) || TextUtils.isEmpty(item.vehicleId)) {
            Toast.makeText(this, "Booking data missing", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    DocumentReference bookingRef = db.collection("Bookings").document(item.bookingId);
                    DocumentReference vehicleRef = db.collection("Vehicles").document(item.vehicleId);

                    db.runTransaction(transaction -> {
                        transaction.delete(bookingRef);
                        transaction.update(vehicleRef,
                                "isBooked", false,
                                "bookingStatus", "Available",
                                "bookedBy", null,
                                "currentBookingId", null);
                        return null;
                    }).addOnSuccessListener(unused -> Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to cancel booking: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("No", null)
                .show();
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private interface OnCustomerNameResolved {
        void onResolved(String customerName);
    }

    private static class BookingItem {
        final String bookingId;
        final String vehicleId;
        final String customerName;
        final String vehicleLabel;
        final String startDate;
        final String endDate;
        final double totalPrice;
        final String status;
        final String driverId;
        final String customerId;

        BookingItem(String bookingId, String vehicleId, String customerName, String vehicleLabel, String startDate, String endDate,
                    double totalPrice, String status, String driverId, String customerId) {
            this.bookingId = bookingId;
            this.vehicleId = vehicleId;
            this.customerName = customerName;
            this.vehicleLabel = vehicleLabel;
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalPrice = totalPrice;
            this.status = status;
            this.driverId = driverId;
            this.customerId = customerId;
        }
    }

    private static class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

        interface OnDriverClickListener {
            void onDriverClick(BookingItem item);
        }

        interface OnCancelBookingListener {
            void onCancelBooking(BookingItem item);
        }

        private final List<BookingItem> items;
        private final OnDriverClickListener listener;
        private final OnCancelBookingListener cancelListener;

        BookingAdapter(List<BookingItem> items, OnDriverClickListener listener, OnCancelBookingListener cancelListener) {
            this.items = items;
            this.listener = listener;
            this.cancelListener = cancelListener;
        }

        @NonNull
        @Override
        public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_renter_booking, parent, false);
            return new BookingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
            BookingItem item = items.get(position);
            holder.tvCustomerName.setText("Customer: " + item.customerName);
            holder.tvVehicleLabel.setText("Vehicle: " + item.vehicleLabel);
            holder.tvDates.setText(item.startDate + " to " + item.endDate);
            holder.tvTotalPrice.setText(String.format("Rs. %.2f", item.totalPrice));
            holder.tvStatus.setText("Status: " + item.status);

            if (item.driverId != null && !item.driverId.isEmpty()) {
                holder.tvDriver.setText("Driver: Tap to view");
                holder.tvDriver.setVisibility(View.VISIBLE);
                holder.tvDriver.setOnClickListener(v -> listener.onDriverClick(item));
            } else {
                holder.tvDriver.setText("Driver: Self-drive");
                holder.tvDriver.setVisibility(View.VISIBLE);
                holder.tvDriver.setOnClickListener(null);
            }

            boolean canCancel = "Booked".equalsIgnoreCase(item.status);
            holder.btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);
            holder.btnCancel.setOnClickListener(canCancel ? v -> cancelListener.onCancelBooking(item) : null);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class BookingViewHolder extends RecyclerView.ViewHolder {
            final TextView tvCustomerName;
            final TextView tvVehicleLabel;
            final TextView tvDates;
            final TextView tvTotalPrice;
            final TextView tvStatus;
            final TextView tvDriver;
            final android.widget.Button btnCancel;

            BookingViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCustomerName = itemView.findViewById(R.id.tvBookingCustomerName);
                tvVehicleLabel = itemView.findViewById(R.id.tvBookingVehicleLabel);
                tvDates = itemView.findViewById(R.id.tvBookingDates);
                tvTotalPrice = itemView.findViewById(R.id.tvBookingTotalPrice);
                tvStatus = itemView.findViewById(R.id.tvBookingStatus);
                tvDriver = itemView.findViewById(R.id.tvBookingDriver);
                btnCancel = itemView.findViewById(R.id.btnCancelBookingItem);
            }
        }
    }
}
