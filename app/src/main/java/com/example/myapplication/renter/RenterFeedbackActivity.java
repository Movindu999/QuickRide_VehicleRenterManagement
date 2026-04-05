package com.example.myapplication.renter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.common.ReviewFeedbackAdapter;
import com.example.myapplication.common.ReviewFeedbackItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RenterFeedbackActivity extends AppCompatActivity {

    private static final String TAG = "RenterFeedback";

    private TextView tvAverageRating;
    private View emptyStateCard;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final List<ReviewFeedbackItem> items = new ArrayList<>();
    private ReviewFeedbackAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renter_feedback);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle("My Feedback");
        topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        tvAverageRating = findViewById(R.id.tvRenterAverageRating);
        RecyclerView rvFeedback = findViewById(R.id.rvRenterFeedback);
        emptyStateCard = findViewById(R.id.emptyStateCard);

        rvFeedback.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewFeedbackAdapter(items);
        rvFeedback.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavRenter);
        bottomNav.setSelectedItemId(R.id.nav_renter_dashboard);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_renter_dashboard) {
                Intent intent = new Intent(this, RenterDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            if (itemId == R.id.nav_renter_my_vehicles) {
                Intent intent = new Intent(this, ManageVehiclesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            if (itemId == R.id.nav_renter_bookings) {
                Intent intent = new Intent(this, RenterBookingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });

        loadFeedback();
    }

    private void loadFeedback() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String renterId = auth.getCurrentUser().getUid();
        if (renterId.trim().isEmpty()) {
            Log.e(TAG, "Current renter UID is empty");
            Toast.makeText(this, "Unable to identify renter account", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "Loading feedback for renterId=" + renterId);

        // Use whereEqualTo only and sort locally to avoid composite-index failures.
        db.collection("reviews")
                .whereEqualTo("renterId", renterId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot == null ? 0 : querySnapshot.size();
                    Log.d(TAG, "Feedback documents found=" + count);

                    items.clear();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            items.add(new ReviewFeedbackItem(
                                    doc.getId(),
                                    safe(doc.getString("renterId"), ""),
                                    safe(doc.getString("customerId"), ""),
                                    safe(doc.getString("customerName"), "Anonymous"),
                                    asFloat(doc.get("rating")),
                                    safe(doc.getString("reviewText"), ""),
                                    safe(doc.getString("type"), "Review"),
                                    doc.getTimestamp("timestamp")
                            ));
                        }
                    }

                    items.sort((left, right) -> {
                        long leftTs = left.getTimestamp() == null ? 0L : left.getTimestamp().toDate().getTime();
                        long rightTs = right.getTimestamp() == null ? 0L : right.getTimestamp().toDate().getTime();
                        return Long.compare(rightTs, leftTs);
                    });

                    adapter.notifyDataSetChanged();
                    emptyStateCard.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    updateAverageRating();

                    Log.d(TAG, "Feedback list rendered. items=" + items.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load renter feedback", e);
                    Toast.makeText(this, "Failed to load feedback", Toast.LENGTH_LONG).show();
                });
    }

    private void updateAverageRating() {
        float sum = 0f;
        int count = 0;
        for (ReviewFeedbackItem item : items) {
            if (!item.isComplaint() && item.getRating() > 0f) {
                sum += item.getRating();
                count++;
            }
        }

        float average = count == 0 ? 0f : (sum / count);
        tvAverageRating.setText(String.format(Locale.getDefault(), "Average Rating: %.1f (%d reviews)", average, count));
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private float asFloat(Object value) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return 0f;
    }
}
