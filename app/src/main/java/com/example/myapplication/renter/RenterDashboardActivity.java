package com.example.myapplication.renter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RenterDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renter_dashboard);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle(R.string.screen_renter_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavRenter);
        bottomNav.setSelectedItemId(R.id.nav_renter_dashboard);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_renter_dashboard) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_renter_my_vehicles) {
                intent = new Intent(this, ManageVehiclesActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });

        Button btnManageDrivers = findViewById(R.id.btnManageDrivers);
        Button btnManageCars = findViewById(R.id.btnManageCars);
        Button btnManageVans = findViewById(R.id.btnManageVans);
        Button btnManageMotorbikes = findViewById(R.id.btnManageMotorbikes);
        Button btnManageBuses = findViewById(R.id.btnManageBuses);
        Button btnManageTuktuk = findViewById(R.id.btnManageTuktuk);

        btnManageDrivers.setOnClickListener(v -> navigateToManageDrivers());
        btnManageCars.setOnClickListener(v -> navigateToManageVehicles("Car"));
        btnManageVans.setOnClickListener(v -> navigateToManageVehicles("Van"));
        btnManageMotorbikes.setOnClickListener(v -> navigateToManageVehicles("Motorbike"));
        btnManageBuses.setOnClickListener(v -> navigateToManageVehicles("Bus"));
        btnManageTuktuk.setOnClickListener(v -> navigateToManageVehicles("Tuktuk"));
    }

    private void navigateToManageDrivers() {
        Intent intent = new Intent(this, ManageDriversActivity.class);
        startActivity(intent);
    }

    private void navigateToManageVehicles(String vehicleType) {
        Intent intent = new Intent(this, ManageVehiclesActivity.class);
        intent.putExtra("vehicleType", vehicleType);
        startActivity(intent);
    }
}