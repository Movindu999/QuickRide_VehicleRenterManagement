package com.example.myapplication.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.admin.AdminDashboardActivity;
import com.example.myapplication.customer.CustomerDashboardActivity;
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


        Button btnRenter = findViewById(R.id.btnRenter);
        btnCustomer.setOnClickListener(v ->
                startActivity(new Intent(RoleSelectionActivity.this, RenterDashboardActivity.class)));
    }
}
