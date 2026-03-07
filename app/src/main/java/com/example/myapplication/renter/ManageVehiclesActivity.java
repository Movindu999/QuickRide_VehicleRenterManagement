package com.example.myapplication.renter;

import android.os.Bundle;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ManageVehiclesActivity extends AppCompatActivity {

    private String vehicleType;
    private RecyclerView vehicleRecyclerView;
    private FloatingActionButton fabAddVehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_vehicles);

        // Get vehicle type from intent
        vehicleType = getIntent().getStringExtra("vehicleType");
        if (vehicleType == null) {
            vehicleType = "Vehicle";
        }

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle("Manage " + vehicleType + "s");
        topAppBar.setNavigationOnClickListener(v -> finish());

        vehicleRecyclerView = findViewById(R.id.vehicleRecyclerView);
        fabAddVehicle = findViewById(R.id.fabAddVehicle);

        fabAddVehicle.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditVehicleActivity.class);
            intent.putExtra("vehicleType", vehicleType);
            intent.putExtra("isEditMode", false);
            startActivity(intent);
        });
    }
}

