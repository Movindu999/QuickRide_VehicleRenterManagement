package com.example.myapplication.renter;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;

public class AddEditVehicleActivity extends AppCompatActivity {

    private String vehicleType;
    private boolean isEditMode;
    private EditText etVehicleNumber;
    private AutoCompleteTextView spnVehicleType;
    private EditText etPricePerDay;
    private LinearLayout containerVehicleImages;
    private Button btnAddVehicle;
    private Button btnUpdateVehicle;
    private Button btnRemoveVehicle;
    private String[] vehicleTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_vehicle);

        vehicleType = getIntent().getStringExtra("vehicleType");
        isEditMode = getIntent().getBooleanExtra("isEditMode", false);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle(isEditMode ? "Update Vehicle" : "Add Vehicle");
        topAppBar.setNavigationOnClickListener(v -> finish());

        etVehicleNumber = findViewById(R.id.etVehicleNumber);
        spnVehicleType = findViewById(R.id.spnVehicleType);
        etPricePerDay = findViewById(R.id.etPricePerDay);
        containerVehicleImages = findViewById(R.id.containerVehicleImages);
        btnAddVehicle = findViewById(R.id.btnAddVehicle);
        btnUpdateVehicle = findViewById(R.id.btnUpdateVehicle);
        btnRemoveVehicle = findViewById(R.id.btnRemoveVehicle);

        // Setup vehicle type dropdown
        setupVehicleTypeSpinner();

        // Set up buttons
        if (isEditMode) {
            btnAddVehicle.setVisibility(android.view.View.GONE);
            btnUpdateVehicle.setVisibility(android.view.View.VISIBLE);
            btnRemoveVehicle.setVisibility(android.view.View.VISIBLE);
            loadVehicleData();
            btnUpdateVehicle.setOnClickListener(v -> updateVehicle());
            btnRemoveVehicle.setOnClickListener(v -> removeVehicle());
        } else {
            btnAddVehicle.setVisibility(android.view.View.VISIBLE);
            btnUpdateVehicle.setVisibility(android.view.View.GONE);
            btnRemoveVehicle.setVisibility(android.view.View.GONE);
            btnAddVehicle.setOnClickListener(v -> addVehicle());
        }

        // Add image button
        ImageButton btnAddImage = findViewById(R.id.btnAddImage);
        btnAddImage.setOnClickListener(v -> addVehicleImageField());
    }

    private void setupVehicleTypeSpinner() {
        vehicleTypes = new String[]{"Car", "Van", "Motorbike", "Bus", "Tuktuk"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                vehicleTypes
        );
        spnVehicleType.setAdapter(adapter);

        // Set selected vehicle type by text
        if (vehicleType != null) {
            spnVehicleType.setText(vehicleType, false);
        } else if (vehicleTypes.length > 0) {
            spnVehicleType.setText(vehicleTypes[0], false);
        }
    }

    private void addVehicleImageField() {
        Toast.makeText(this, "Add image functionality will be implemented", Toast.LENGTH_SHORT).show();
    }

    private void addVehicle() {
        String vehicleNumber = etVehicleNumber.getText().toString().trim();
        String vehicleTypeSelected = spnVehicleType.getText().toString();
        String pricePerDay = etPricePerDay.getText().toString().trim();

        if (vehicleNumber.isEmpty() || pricePerDay.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Vehicle added successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updateVehicle() {
        String vehicleNumber = etVehicleNumber.getText().toString().trim();
        String vehicleTypeSelected = spnVehicleType.getText().toString();
        String pricePerDay = etPricePerDay.getText().toString().trim();

        if (vehicleNumber.isEmpty() || pricePerDay.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Vehicle updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void loadVehicleData() {
        // This will be implemented when backend is ready
    }

    private void removeVehicle() {
        Toast.makeText(this, "Vehicle removed successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}

