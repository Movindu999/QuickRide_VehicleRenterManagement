package com.example.myapplication.customer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    private TextView tvVehicleTitle;
    private TextView tvVehiclePricePerDay;
    private ImageView ivVehicle;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private TextView tvTotalDays;
    private TextView tvTotalPrice;
    private Spinner spinnerDrivers;
    private Button btnSelectStartDate;
    private Button btnSelectEndDate;
    private Button btnConfirmBooking;
    private Button btnCancelBooking;
    private Button btnViewDriverProfile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String renterId;
    private String vehicleId;
    private String vehicleType;
    private String vehicleNumber;
    private String vehicleImageUrl;
    private double pricePerDay;

    private long startDateMillis = -1L;
    private long endDateMillis = -1L;

    private final List<DriverItem> drivers = new ArrayList<>();
    private ArrayAdapter<String> driverAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        renterId = getIntent().getStringExtra("renterId");
        vehicleId = getIntent().getStringExtra("vehicleId");
        vehicleType = getIntent().getStringExtra("vehicleType");
        vehicleNumber = getIntent().getStringExtra("vehicleNumber");
        vehicleImageUrl = getIntent().getStringExtra("vehicleImageUrl");
        pricePerDay = getIntent().getDoubleExtra("pricePerDay", 0d);

        if (TextUtils.isEmpty(renterId) || TextUtils.isEmpty(vehicleId)) {
            Toast.makeText(this, "Booking details are missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle("Booking");
        topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        tvVehicleTitle = findViewById(R.id.tvBookingVehicleTitle);
        tvVehiclePricePerDay = findViewById(R.id.tvBookingVehiclePricePerDay);
        ivVehicle = findViewById(R.id.ivBookingVehicle);
        tvStartDate = findViewById(R.id.tvBookingStartDate);
        tvEndDate = findViewById(R.id.tvBookingEndDate);
        tvTotalDays = findViewById(R.id.tvBookingTotalDays);
        tvTotalPrice = findViewById(R.id.tvBookingTotalPrice);
        spinnerDrivers = findViewById(R.id.spinnerDrivers);
        btnSelectStartDate = findViewById(R.id.btnSelectStartDate);
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        btnCancelBooking = findViewById(R.id.btnCancelBooking);
        btnViewDriverProfile = findViewById(R.id.btnViewDriverProfile);

        tvVehicleTitle.setText(safe(vehicleType, "Vehicle") + " - " + safe(vehicleNumber, "N/A"));
        tvVehiclePricePerDay.setText(String.format(Locale.getDefault(), "Price per day: %.2f", pricePerDay));
        Glide.with(this)
                .load(vehicleImageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(ivVehicle);

        tvStartDate.setText("");
        tvEndDate.setText("");
        tvTotalDays.setText("0");
        tvTotalPrice.setText(String.format(Locale.getDefault(), "%.2f", 0d));

        setupDriverSpinner();
        loadDrivers();

        btnSelectStartDate.setOnClickListener(v -> pickDate(true));
        btnSelectEndDate.setOnClickListener(v -> pickDate(false));
        btnConfirmBooking.setOnClickListener(v -> saveBooking());
        btnCancelBooking.setOnClickListener(v -> cancelBooking());
        btnViewDriverProfile.setOnClickListener(v -> viewDriverProfile());
    }

    private void setupDriverSpinner() {
        drivers.clear();
        drivers.add(new DriverItem("", "Self Drive / None"));

        List<String> labels = new ArrayList<>();
        labels.add("Self Drive / None");
        driverAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        driverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDrivers.setAdapter(driverAdapter);
    }

    private void loadDrivers() {
        db.collection("Drivers")
                .whereEqualTo("renterId", renterId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    drivers.clear();
                    drivers.add(new DriverItem("", "Self Drive / None"));
                    driverAdapter.clear();
                    driverAdapter.add("Self Drive / None");

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String firstName = safe(doc.getString("firstName"), "");
                        String lastName = safe(doc.getString("lastName"), "");
                        String fullName = (firstName + " " + lastName).trim();
                        if (fullName.isEmpty()) {
                            fullName = "Driver " + doc.getId();
                        }
                        drivers.add(new DriverItem(doc.getId(), fullName));
                        driverAdapter.add(fullName);
                    }
                    driverAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load drivers: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void pickDate(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(Calendar.YEAR, year);
                    picked.set(Calendar.MONTH, month);
                    picked.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    picked.set(Calendar.HOUR_OF_DAY, 0);
                    picked.set(Calendar.MINUTE, 0);
                    picked.set(Calendar.SECOND, 0);
                    picked.set(Calendar.MILLISECOND, 0);

                    if (isStartDate) {
                        startDateMillis = picked.getTimeInMillis();
                    } else {
                        endDateMillis = picked.getTimeInMillis();
                    }

                    updateDateViews();
                    recalculateTotals();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void updateDateViews() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        if (startDateMillis > 0) {
            tvStartDate.setText(sdf.format(startDateMillis));
        }
        if (endDateMillis > 0) {
            tvEndDate.setText(sdf.format(endDateMillis));
        }
    }

    private void recalculateTotals() {
        if (startDateMillis <= 0 || endDateMillis <= 0 || endDateMillis < startDateMillis) {
            tvTotalDays.setText("0");
            tvTotalPrice.setText(String.format(Locale.getDefault(), "%.2f", 0d));
            return;
        }

        long diffMillis = endDateMillis - startDateMillis;
        int totalDays = (int) (diffMillis / (24L * 60L * 60L * 1000L)) + 1;
        double totalPrice = totalDays * pricePerDay;

        tvTotalDays.setText(String.valueOf(totalDays));
        tvTotalPrice.setText(String.format(Locale.getDefault(), "%.2f", totalPrice));
    }

    private void saveBooking() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startDateMillis <= 0 || endDateMillis <= 0) {
            Toast.makeText(this, "Please select start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endDateMillis < startDateMillis) {
            Toast.makeText(this, "End date cannot be before start date", Toast.LENGTH_SHORT).show();
            return;
        }

        long diffMillis = endDateMillis - startDateMillis;
        int totalDays = (int) (diffMillis / (24L * 60L * 60L * 1000L)) + 1;
        double totalPrice = totalDays * pricePerDay;

        String customerId = mAuth.getCurrentUser().getUid();
        int selectedIndex = spinnerDrivers.getSelectedItemPosition();
        final String selectedDriverId = (selectedIndex >= 0 && selectedIndex < drivers.size())
                ? drivers.get(selectedIndex).driverId
                : "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = sdf.format(startDateMillis);
        String endDate = sdf.format(endDateMillis);

        DocumentReference vehicleRef = db.collection("Vehicles").document(vehicleId);
        DocumentReference bookingRef = db.collection("Bookings").document();

        btnConfirmBooking.setEnabled(false);
        db.runTransaction(transaction -> {
                    DocumentSnapshot vehicleSnapshot = transaction.get(vehicleRef);
                    if (!vehicleSnapshot.exists()) {
                        throw new IllegalStateException("Vehicle not found");
                    }

                    if (Boolean.TRUE.equals(vehicleSnapshot.getBoolean("isBooked"))) {
                        throw new IllegalStateException("This vehicle is already booked");
                    }

                    Map<String, Object> booking = new HashMap<>();
                    booking.put("customerId", customerId);
                    booking.put("renterId", renterId);
                    booking.put("vehicleId", vehicleId);
                    booking.put("driverId", TextUtils.isEmpty(selectedDriverId) ? null : selectedDriverId);
                    booking.put("startDate", startDate);
                    booking.put("endDate", endDate);
                    booking.put("totalPrice", totalPrice);
                    booking.put("status", "Booked");
                    booking.put("vehicleLabel", safe(vehicleType, "Vehicle") + " - " + safe(vehicleNumber, "N/A"));
                    booking.put("createdAt", Timestamp.now());

                    transaction.set(bookingRef, booking);

                    Map<String, Object> vehicleUpdate = new HashMap<>();
                    vehicleUpdate.put("isBooked", true);
                    vehicleUpdate.put("bookingStatus", "Booked");
                    vehicleUpdate.put("bookedBy", customerId);
                    vehicleUpdate.put("currentBookingId", bookingRef.getId());
                    vehicleUpdate.put("updatedAt", Timestamp.now());
                    transaction.update(vehicleRef, vehicleUpdate);

                    return null;
                })
                .addOnSuccessListener(unused -> {
                    btnConfirmBooking.setEnabled(true);
                    Toast.makeText(this, "Vehicle booked successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnConfirmBooking.setEnabled(true);
                    Toast.makeText(this, "Failed to book vehicle: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void cancelBooking() {
        Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void viewDriverProfile() {
        int selectedIndex = spinnerDrivers.getSelectedItemPosition();
        if (selectedIndex <= 0) {
            Toast.makeText(this, "Please select a driver first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedIndex < drivers.size()) {
            String driverId = drivers.get(selectedIndex).driverId;
            if (TextUtils.isEmpty(driverId)) {
                Toast.makeText(this, "Self-drive selected. No driver profile available.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, com.example.myapplication.renter.DriverProfileActivity.class);
            intent.putExtra("driverId", driverId);
            startActivity(intent);
        }
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static class DriverItem {
        final String driverId;
        final String displayName;

        DriverItem(String driverId, String displayName) {
            this.driverId = driverId;
            this.displayName = displayName;
        }
    }
}
