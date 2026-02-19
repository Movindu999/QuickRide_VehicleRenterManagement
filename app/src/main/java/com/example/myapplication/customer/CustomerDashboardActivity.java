package com.example.myapplication.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class CustomerDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        Button btnViewRenters = findViewById(R.id.btnViewRenters);
        Button btnMyBookings = findViewById(R.id.btnMyBookings);

        btnViewRenters.setOnClickListener(v ->
                startActivity(new Intent(this, ViewRentersActivity.class)));

        btnMyBookings.setOnClickListener(v ->
                startActivity(new Intent(this, MyBookingsActivity.class)));
    }
}
