package com.example.myapplication.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminComplaintsActivity extends AppCompatActivity {

    private RecyclerView rvComplaints;
    private View emptyStateCard;

    private FirebaseFirestore db;
    private final List<AdminComplaintsAdapter.ComplaintItem> items = new ArrayList<>();
    private AdminComplaintsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_complaints);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Complaints");
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        rvComplaints = findViewById(R.id.rvAdminComplaints);
        emptyStateCard = findViewById(R.id.emptyStateCard);

        rvComplaints.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminComplaintsAdapter(items, db, this::confirmDismiss);
        rvComplaints.setAdapter(adapter);

        loadComplaints();
    }

    private void loadComplaints() {
        db.collection("reviews")
                .whereEqualTo("type", "Complaint")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    items.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        items.add(new AdminComplaintsAdapter.ComplaintItem(
                                doc.getId(),
                                safe(doc.getString("customerId"), ""),
                                safe(doc.getString("renterId"), ""),
                                asFloat(doc.get("rating")),
                                safe(doc.getString("reviewText"), ""),
                                doc.getTimestamp("timestamp")
                        ));
                    }

                    // Local timestamp DESC sort avoids composite index requirement.
                    items.sort((left, right) -> {
                        long leftTs = left.timestamp == null ? 0L : left.timestamp.toDate().getTime();
                        long rightTs = right.timestamp == null ? 0L : right.timestamp.toDate().getTime();
                        return Long.compare(rightTs, leftTs);
                    });

                    adapter.notifyDataSetChanged();
                    emptyStateCard.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    emptyStateCard.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Failed to load complaints", Toast.LENGTH_LONG).show();
                });
    }

    private void confirmDismiss(AdminComplaintsAdapter.ComplaintItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Dismiss Complaint")
                .setMessage("Remove this complaint from the list?")
                .setPositiveButton("Dismiss", (dialog, which) -> dismissComplaint(item))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void dismissComplaint(AdminComplaintsAdapter.ComplaintItem item) {
        db.collection("reviews")
                .document(item.id)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Complaint dismissed", Toast.LENGTH_SHORT).show();
                    loadComplaints();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to dismiss complaint", Toast.LENGTH_SHORT).show());
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
