package com.example.myapplication.driver;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;

public class DriverDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerViewBookings;
    private TextView txtEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle(R.string.screen_driver_dashboard);
        topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        txtEmptyState = findViewById(R.id.txtEmptyState);

        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));

        // For now, show empty state (backend will be implemented later)
        showEmptyState();
    }

    private void showEmptyState() {
        recyclerViewBookings.setVisibility(View.GONE);
        txtEmptyState.setVisibility(View.VISIBLE);
    }

    private void showBookings() {
        recyclerViewBookings.setVisibility(View.VISIBLE);
        txtEmptyState.setVisibility(View.GONE);
    }
}

