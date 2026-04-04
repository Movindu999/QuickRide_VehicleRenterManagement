package com.example.myapplication.renter;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DriverProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String driverId;

    private TextView tvDriverName;
    private TextView tvDriverEmail;
    private TextView tvDriverPhone;
    private TextView tvDriverLicense;
    private TextView tvDriverIdNumber;
    private TextView tvDriverAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        db = FirebaseFirestore.getInstance();

        driverId = getIntent().getStringExtra("driverId");

        if (TextUtils.isEmpty(driverId)) {
            Toast.makeText(this, "Driver not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle("Driver Profile");
        topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        tvDriverName = findViewById(R.id.tvDriverName);
        tvDriverEmail = findViewById(R.id.tvDriverEmail);
        tvDriverPhone = findViewById(R.id.tvDriverPhone);
        tvDriverLicense = findViewById(R.id.tvDriverLicense);
        tvDriverIdNumber = findViewById(R.id.tvDriverIdNumber);
        tvDriverAge = findViewById(R.id.tvDriverAge);

        loadDriverProfile();
    }

    private void loadDriverProfile() {
        db.collection("Drivers").document(driverId).get()
                .addOnSuccessListener(this::bindDriverData)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load driver: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void bindDriverData(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Driver not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String firstName = safe(doc.getString("firstName"), "");
        String lastName = safe(doc.getString("lastName"), "");
        String fullName = (firstName + " " + lastName).trim();

        tvDriverName.setText(TextUtils.isEmpty(fullName) ? "N/A" : fullName);
        tvDriverEmail.setText("Email: " + safe(doc.getString("email"), "N/A"));
        tvDriverPhone.setText("Phone: " + safe(doc.getString("contactNumber"), "N/A"));
        tvDriverLicense.setText("License: " + safe(doc.getString("licenseNumber"), "N/A"));
        tvDriverIdNumber.setText("ID Number: " + safe(doc.getString("idNumber"), "N/A"));
        tvDriverAge.setText("Age: " + safe(doc.getString("age"), "N/A"));
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}

