package com.example.myapplication.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle(R.string.screen_customer_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavCustomer);
        bottomNav.setSelectedItemId(R.id.nav_customer_dashboard);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_customer_dashboard) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_customer_renters) {
                intent = new Intent(this, ViewRentersActivity.class);
            } else if (itemId == R.id.nav_customer_bookings) {
                intent = new Intent(this, MyBookingsActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });

        Button btnViewRenters = findViewById(R.id.btnViewRenters);
        Button btnMyBookings = findViewById(R.id.btnMyBookings);
        Button btnAccount = findViewById(R.id.btnAccount);

        btnViewRenters.setOnClickListener(v ->
                startActivity(new Intent(this, ViewRentersActivity.class)));

        btnMyBookings.setOnClickListener(v ->
                startActivity(new Intent(this, MyBookingsActivity.class)));

        btnAccount.setOnClickListener(v ->
                startActivity(new Intent(this, CustomerProfileActivity.class)));
    }
}
