package com.example.myapplication.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminManageBookingsActivity extends AppCompatActivity {

    private RecyclerView rvBookings;
    private TextView tvEmptyState;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private AdminManageBookingsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_bookings);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.screen_manage_bookings));
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        rvBookings = findViewById(R.id.rvAdminBookings);
        tvEmptyState = findViewById(R.id.tvAdminBookingsEmpty);
        progressBar = findViewById(R.id.progressAdminBookings);

        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminManageBookingsAdapter(db);
        rvBookings.setAdapter(adapter);

        loadBookings();
    }

    private void loadBookings() {
        showLoading(true);
        fetchBookings("Bookings", true);
    }

    private void fetchBookings(String collectionName, boolean canFallback) {
        db.collection(collectionName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty() && canFallback) {
                        fetchBookings("bookings", false);
                        return;
                    }

                    List<AdminBookingItem> bookings = new ArrayList<>();
                    int bookingNumber = 1;
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        AdminBookingItem item = new AdminBookingItem(
                                doc.getId(),
                                safe(doc.getString("customerId"), ""),
                                safe(doc.getString("renterId"), ""),
                                safe(doc.getString("vehicleId"), ""),
                                safe(doc.getString("driverId"), ""),
                                safe(doc.getString("startDate"), "N/A"),
                                safe(doc.getString("endDate"), "N/A"),
                                asDouble(doc.get("totalPrice")),
                                safe(doc.getString("status"), "N/A")
                        );
                        item.setBookingNumber(bookingNumber++);
                        bookings.add(item);
                    }

                    adapter.submitList(bookings);
                    showLoading(false);
                    toggleEmptyState(bookings.isEmpty());
                })
                .addOnFailureListener(e -> {
                    if (canFallback) {
                        fetchBookings("bookings", false);
                        return;
                    }
                    showLoading(false);
                    toggleEmptyState(true);
                    Toast.makeText(this, "Failed to load bookings: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        rvBookings.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void toggleEmptyState(boolean isEmpty) {
        tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvBookings.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private double asDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0d;
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}

