package com.example.myapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btnManageRenters = findViewById(R.id.btnManageRenters);
        Button btnManageCustomers = findViewById(R.id.btnManageCustomers);
        Button btnManageBookings = findViewById(R.id.btnManageBookings);
        Button btnViewComplaints = findViewById(R.id.btnViewComplaints);

        btnManageRenters.setOnClickListener(v ->
                startActivity(new Intent(this, ManageRentersActivity.class)));

        btnManageCustomers.setOnClickListener(v ->
                startActivity(new Intent(this, ManageCustomersActivity.class)));

        btnManageBookings.setOnClickListener(v ->
                startActivity(new Intent(this, AdminManageBookingsActivity.class)));

        btnViewComplaints.setOnClickListener(v ->
                startActivity(new Intent(this, AdminComplaintsActivity.class)));
    }
}
