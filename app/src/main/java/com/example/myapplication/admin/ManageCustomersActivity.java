package com.example.myapplication.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ManageCustomersActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_customers);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle(R.string.screen_manage_customers);
        topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavAdmin);
        bottomNav.setSelectedItemId(R.id.nav_admin_customers);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_admin_customers) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_admin_dashboard) {
                intent = new Intent(this, AdminDashboardActivity.class);
            } else if (itemId == R.id.nav_admin_renters) {
                intent = new Intent(this, ManageRentersActivity.class);
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
    }
}
