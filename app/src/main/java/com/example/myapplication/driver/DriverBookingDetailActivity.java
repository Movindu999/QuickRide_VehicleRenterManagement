package com.example.myapplication.driver;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;

public class DriverBookingDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_booking_detail);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle("Booking Details");
        topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        TextView tvBookingId = findViewById(R.id.tvBookingId);
        TextView tvCustomerName = findViewById(R.id.tvCustomerName);
        TextView tvVehicleLabel = findViewById(R.id.tvVehicleLabel);
        TextView tvDates = findViewById(R.id.tvDates);
        TextView tvStatus = findViewById(R.id.tvStatus);

        String bookingId = getIntent().getStringExtra("bookingId");
        String customerName = getIntent().getStringExtra("customerName");
        String vehicleLabel = getIntent().getStringExtra("vehicleLabel");
        String startDate = getIntent().getStringExtra("startDate");
        String endDate = getIntent().getStringExtra("endDate");
        String status = getIntent().getStringExtra("status");

        tvBookingId.setText(!TextUtils.isEmpty(bookingId) ? bookingId : "N/A");
        tvCustomerName.setText(!TextUtils.isEmpty(customerName) ? customerName : "N/A");
        tvVehicleLabel.setText(!TextUtils.isEmpty(vehicleLabel) ? vehicleLabel : "Vehicle");
        tvDates.setText(String.format("%s to %s", safe(startDate), safe(endDate)));
        tvStatus.setText(!TextUtils.isEmpty(status) ? status : "Booked");
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value.trim();
    }
}

