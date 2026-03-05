package com.example.myapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle(R.string.screen_admin_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavAdmin);
        bottomNav.setSelectedItemId(R.id.nav_admin_dashboard);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_admin_dashboard) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_admin_renters) {
                intent = new Intent(this, ManageRentersActivity.class);
            } else if (itemId == R.id.nav_admin_customers) {
                intent = new Intent(this, ManageCustomersActivity.class);
            } else if (itemId == R.id.nav_admin_bookings) {
                intent = new Intent(this, ManageBookingsActivity.class);
            } else if (itemId == R.id.nav_admin_complaints) {
                intent = new Intent(this, ViewComplaintsActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });

        Button btnManageRenters = findViewById(R.id.btnManageRenters);
        Button btnManageCustomers = findViewById(R.id.btnManageCustomers);
        Button btnManageBookings = findViewById(R.id.btnManageBookings);
        Button btnViewComplaints = findViewById(R.id.btnViewComplaints);

        btnManageRenters.setOnClickListener(v ->
                startActivity(new Intent(this, ManageRentersActivity.class)));

        btnManageCustomers.setOnClickListener(v ->
                startActivity(new Intent(this, ManageCustomersActivity.class)));

        btnManageBookings.setOnClickListener(v ->
                startActivity(new Intent(this, ManageBookingsActivity.class)));

        btnViewComplaints.setOnClickListener(v ->
                startActivity(new Intent(this, ViewComplaintsActivity.class)));
    }
}
