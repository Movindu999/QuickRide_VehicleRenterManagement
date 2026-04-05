package com.example.myapplication.driver;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class DriverDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerViewBookings;
    private TextView txtEmptyState;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DriverBookingAdapter bookingAdapter;
    private final List<DriverBookingAdapter.BookingItem> bookingItems = new ArrayList<>();
    private ListenerRegistration bookingsListener;
    private boolean fallbackTried;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle(R.string.screen_driver_dashboard);
        topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        txtEmptyState = findViewById(R.id.txtEmptyState);

        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));
        bookingAdapter = new DriverBookingAdapter();
        bookingAdapter.setOnBookingClickListener(this::openBookingDetails);
        bookingAdapter.setOnCancelBookingClickListener(this::cancelBooking);
        recyclerViewBookings.setAdapter(bookingAdapter);

        loadDriverBookings();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bookingsListener != null) {
            bookingsListener.remove();
            bookingsListener = null;
        }
    }

    private void loadDriverBookings() {
        if (mAuth.getCurrentUser() == null) {
            showEmptyState();
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        fallbackTried = false;
        startBookingsListener("Bookings");
    }

    private void startBookingsListener(String collectionName) {
        if (bookingsListener != null) {
            bookingsListener.remove();
            bookingsListener = null;
        }

        String driverId = mAuth.getCurrentUser() == null ? "" : mAuth.getCurrentUser().getUid();
        if (driverId.isEmpty()) {
            showEmptyState();
            return;
        }

        bookingsListener = db.collection(collectionName)
                .whereEqualTo("driverId", driverId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load bookings: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        showEmptyState();
                        return;
                    }

                    bookingItems.clear();
                    if (value != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            String bookingId = doc.getId();
                            String customerId = safe(doc.getString("customerId"), "");
                            String vehicleId = safe(doc.getString("vehicleId"), "");
                            String vehicleLabel = safe(doc.getString("vehicleLabel"), "Vehicle");
                            String startDate = safe(doc.getString("startDate"), "N/A");
                            String endDate = safe(doc.getString("endDate"), "N/A");
                            String status = safe(doc.getString("status"), "Booked");

                            DriverBookingAdapter.BookingItem item = new DriverBookingAdapter.BookingItem(
                                    bookingId,
                                    customerId,
                                    startDate,
                                    endDate,
                                    customerId
                            );
                            item.setVehicleId(vehicleId);
                            item.setVehicleLabel(vehicleLabel);
                            item.setStatus(status);
                            bookingItems.add(item);

                            if (!customerId.isEmpty()) {
                                db.collection("Users").document(customerId).get()
                                        .addOnSuccessListener(userDoc -> {
                                            String firstName = userDoc.getString("firstName");
                                            String lastName = userDoc.getString("lastName");
                                            String name = buildName(firstName, lastName, customerId);
                                            item.setCustomerName(name);
                                            bookingAdapter.notifyDataSetChanged();
                                        });
                            }
                        }
                    }

                    if (bookingItems.isEmpty() && "Bookings".equals(collectionName) && !fallbackTried) {
                        fallbackTried = true;
                        startBookingsListener("bookings");
                        return;
                    }

                    bookingAdapter.setBookings(new ArrayList<>(bookingItems));
                    if (bookingItems.isEmpty()) {
                        showEmptyState();
                    } else {
                        showBookings();
                    }
                });
    }

    private void openBookingDetails(DriverBookingAdapter.BookingItem item) {
        Intent intent = new Intent(this, DriverBookingDetailActivity.class);
        intent.putExtra("bookingId", item.getBookingNumber());
        intent.putExtra("customerId", item.getCustomerId());
        intent.putExtra("customerName", item.getCustomerName());
        intent.putExtra("vehicleId", item.getVehicleId());
        intent.putExtra("vehicleLabel", item.getVehicleLabel());
        intent.putExtra("startDate", item.getStartDate());
        intent.putExtra("endDate", item.getEndDate());
        intent.putExtra("status", item.getStatus());
        startActivity(intent);
    }

    private void cancelBooking(DriverBookingAdapter.BookingItem item) {
        if (!"Booked".equalsIgnoreCase(safe(item.getStatus(), ""))) {
            Toast.makeText(this, "Only active bookings can be cancelled", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(item.getBookingNumber()) || TextUtils.isEmpty(item.getVehicleId())) {
            Toast.makeText(this, "Booking data missing", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    DocumentReference bookingRef = db.collection("Bookings").document(item.getBookingNumber());
                    DocumentReference vehicleRef = db.collection("Vehicles").document(item.getVehicleId());

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

    private String buildName(String firstName, String lastName, String fallback) {
        String full = ((firstName == null ? "" : firstName.trim()) + " " + (lastName == null ? "" : lastName.trim())).trim();
        return full.isEmpty() ? fallback : full;
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private void showEmptyState() {
        recyclerViewBookings.setVisibility(View.GONE);
        txtEmptyState.setVisibility(View.VISIBLE);
    }

    private void showBookings() {
        recyclerViewBookings.setVisibility(View.VISIBLE);
        txtEmptyState.setVisibility(View.GONE);
    }
}
