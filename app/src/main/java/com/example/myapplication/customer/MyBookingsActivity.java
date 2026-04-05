package com.example.myapplication.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyBookingsActivity extends AppCompatActivity {

    private RecyclerView rvMyBookings;
    private View emptyStateCard;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final List<BookingItem> bookingItems = new ArrayList<>();
    private BookingAdapter bookingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle(R.string.screen_my_bookings);
        topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        rvMyBookings = findViewById(R.id.rvMyBookings);
        emptyStateCard = findViewById(R.id.emptyStateCard);

        rvMyBookings.setLayoutManager(new LinearLayoutManager(this));
        bookingAdapter = new BookingAdapter(bookingItems, this::cancelBooking);
        rvMyBookings.setAdapter(bookingAdapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavCustomer);
        bottomNav.setSelectedItemId(R.id.nav_customer_bookings);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_customer_bookings) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_customer_dashboard) {
                intent = new Intent(this, CustomerDashboardActivity.class);
            } else if (itemId == R.id.nav_customer_renters) {
                intent = new Intent(this, ViewRentersActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });

        listenForMyBookings();
    }

    private void listenForMyBookings() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        String customerId = mAuth.getCurrentUser().getUid();
        db.collection("Bookings")
                .whereEqualTo("customerId", customerId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load bookings: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    bookingItems.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            bookingItems.add(new BookingItem(
                                    doc.getId(),
                                    safe(doc.getString("vehicleId"), ""),
                                    safe(doc.getString("vehicleLabel"), "Vehicle " + safe(doc.getString("vehicleId"), "N/A")),
                                    safe(doc.getString("startDate"), "N/A"),
                                    safe(doc.getString("endDate"), "N/A"),
                                    asDouble(doc.get("totalPrice")),
                                    safe(doc.getString("status"), "Booked")
                            ));
                        }
                    }

                    bookingAdapter.notifyDataSetChanged();
                    emptyStateCard.setVisibility(bookingItems.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void cancelBooking(BookingItem item) {
        if (TextUtils.isEmpty(item.vehicleId)) {
            Toast.makeText(this, "Vehicle data missing", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    final com.google.firebase.firestore.DocumentReference bookingRef = db.collection("Bookings").document(item.bookingId);
                    final com.google.firebase.firestore.DocumentReference vehicleRef = db.collection("Vehicles").document(item.vehicleId);

                    db.runTransaction(transaction -> {
                        transaction.delete(bookingRef);
                        transaction.update(vehicleRef,
                                "isBooked", false,
                                "bookingStatus", "Available",
                                "bookedBy", null,
                                "currentBookingId", null);
                        return null;
                    }).addOnSuccessListener(unused -> Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show())
                      .addOnFailureListener(e -> Toast.makeText(this, "Failed to cancel booking: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
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

    private static class BookingItem {
        final String bookingId;
        final String vehicleId;
        final String vehicleLabel;
        final String startDate;
        final String endDate;
        final double totalPrice;
        final String status;

        BookingItem(String bookingId, String vehicleId, String vehicleLabel, String startDate, String endDate, double totalPrice, String status) {
            this.bookingId = bookingId;
            this.vehicleId = vehicleId;
            this.vehicleLabel = vehicleLabel;
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalPrice = totalPrice;
            this.status = status;
        }
    }

    private static class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

        interface OnCancelBookingListener {
            void onCancelBooking(BookingItem item);
        }

        private final List<BookingItem> items;
        private final OnCancelBookingListener listener;

        BookingAdapter(List<BookingItem> items, OnCancelBookingListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_customer, parent, false);
            return new BookingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
            BookingItem item = items.get(position);
            holder.tvVehicleLabel.setText(item.vehicleLabel);
            holder.tvDates.setText(String.format(Locale.getDefault(), "Dates: %s to %s", item.startDate, item.endDate));
            holder.tvTotalPrice.setText(String.format(Locale.getDefault(), "Total price: %.2f", item.totalPrice));
            holder.tvStatus.setText("Status: " + item.status);
            holder.btnCancel.setOnClickListener(v -> listener.onCancelBooking(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class BookingViewHolder extends RecyclerView.ViewHolder {
            final TextView tvVehicleLabel;
            final TextView tvDates;
            final TextView tvTotalPrice;
            final TextView tvStatus;
            final Button btnCancel;

            BookingViewHolder(@NonNull View itemView) {
                super(itemView);
                tvVehicleLabel = itemView.findViewById(R.id.tvBookingVehicleLabel);
                tvDates = itemView.findViewById(R.id.tvBookingDates);
                tvTotalPrice = itemView.findViewById(R.id.tvBookingTotalPrice);
                tvStatus = itemView.findViewById(R.id.tvBookingStatus);
                btnCancel = itemView.findViewById(R.id.btnCancelBookingItem);
            }
        }
    }
}
