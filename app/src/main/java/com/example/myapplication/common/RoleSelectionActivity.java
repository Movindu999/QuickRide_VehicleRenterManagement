package com.example.myapplication.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.admin.AdminDashboardActivity;
import com.example.myapplication.customer.CustomerDashboardActivity;
import com.example.myapplication.driver.DriverDashboardActivity;
import com.example.myapplication.renter.RenterDashboardActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle(R.string.screen_role_selection);

        // Admin button
        Button btnAdmin = findViewById(R.id.btnAdmin);
        btnAdmin.setOnClickListener(v ->
                startActivity(new Intent(RoleSelectionActivity.this, AdminDashboardActivity.class)));

        // Customer button
        Button btnCustomer = findViewById(R.id.btnCustomer);
        btnCustomer.setOnClickListener(v ->
                startActivity(new Intent(RoleSelectionActivity.this, CustomerDashboardActivity.class)));

        // Renter button
        Button btnRenter = findViewById(R.id.btnRenter);
        btnRenter.setOnClickListener(v ->
                startActivity(new Intent(RoleSelectionActivity.this, RenterDashboardActivity.class)));

        // Driver button
        Button btnDriver = findViewById(R.id.btnDriver);
        btnDriver.setOnClickListener(v ->
                startActivity(new Intent(RoleSelectionActivity.this, DriverDashboardActivity.class)));
    }
}
