package com.example.myapplication.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.admin.AdminLoginActivity;
import com.example.myapplication.customer.CustomerLoginActivity;
import com.example.myapplication.driver.DriverDashboardActivity;
import com.example.myapplication.renter.RenterDashboardActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        // Get buttons from XML
        Button btnAdmin    = findViewById(R.id.btnAdmin);
        Button btnCustomer = findViewById(R.id.btnCustomer);
        Button btnRenter   = findViewById(R.id.btnRenter);
        Button btnDriver   = findViewById(R.id.btnDriver);

        // Set onClick listeners for each button to navigate to the respective login screen
        btnAdmin.setOnClickListener(v ->
                startActivity(new Intent(this, AdminLoginActivity.class)));

        btnCustomer.setOnClickListener(v ->
                startActivity(new Intent(this, CustomerLoginActivity.class)));

        btnRenter.setOnClickListener(v ->
                startActivity(new Intent(this, RenterDashboardActivity.class)));

        btnDriver.setOnClickListener(v ->
                startActivity(new Intent(this, DriverDashboardActivity.class)));
    }
}
