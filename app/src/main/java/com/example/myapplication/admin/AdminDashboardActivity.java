package com.example.myapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

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
