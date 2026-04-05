package com.example.myapplication.customer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.common.ReviewFeedbackAdapter;
import com.example.myapplication.common.ReviewFeedbackItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RenterReviewsActivity extends AppCompatActivity {

    private static final String TAG = "ReviewsList";

    private TextView tvAverageRating;
    private RecyclerView rvReviews;
    private View emptyStateCard;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String renterId;
    private final List<ReviewFeedbackItem> reviewItems = new ArrayList<>();
    private ReviewFeedbackAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renter_reviews);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        renterId = getIntent() != null ? getIntent().getStringExtra("renterId") : null;
        String renterName = getIntent() != null ? getIntent().getStringExtra("renterName") : null;

        Log.d(TAG, "Received renterId from Intent: " + renterId);

        if (TextUtils.isEmpty(renterId)) {
            Log.e(TAG, "renterId is null/empty. Cannot load reviews.");
            Toast.makeText(this, "Error: renterId is missing in Intent", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle("Reviews");
        topAppBar.setSubtitle(TextUtils.isEmpty(renterName) ? null : renterName);
        topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        tvAverageRating = findViewById(R.id.tvAverageRating);
        rvReviews = findViewById(R.id.rvRenterReviews);
        emptyStateCard = findViewById(R.id.emptyStateCard);
        FloatingActionButton fabAddReview = findViewById(R.id.fabAddReview);

        // RecyclerView must be initialized before data fetch.
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewFeedbackAdapter(reviewItems);
        rvReviews.setAdapter(adapter);

        fabAddReview.setOnClickListener(v -> showAddReviewDialog());

        loadReviews();
    }

    private void loadReviews() {
        Log.d(TAG, "Fetching started for Renter: " + renterId);

        db.collection("reviews")
                .whereEqualTo("renterId", renterId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalDocs = querySnapshot == null ? 0 : querySnapshot.size();
                    Log.d(TAG, "Documents found: " + totalDocs);

                    reviewItems.clear();

                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            ReviewFeedbackItem item = new ReviewFeedbackItem(
                                    doc.getId(),
                                    safe(doc.getString("renterId"), ""),
                                    safe(doc.getString("customerId"), ""),
                                    safe(doc.getString("customerName"), "Anonymous"),
                                    asFloat(doc.get("rating")),
                                    safe(doc.getString("reviewText"), ""),
                                    safe(doc.getString("type"), "Review"),
                                    doc.getTimestamp("timestamp")
                            );
                            reviewItems.add(item);
                            Log.d(TAG, "Added review doc: " + doc.getId());
                        }
                    }

                    // Local sorting to avoid composite index requirements.
                    reviewItems.sort((left, right) -> {
                        long leftTs = left.getTimestamp() == null ? 0L : left.getTimestamp().toDate().getTime();
                        long rightTs = right.getTimestamp() == null ? 0L : right.getTimestamp().toDate().getTime();
                        return Long.compare(rightTs, leftTs);
                    });

                    // Update UI immediately after list mutation.
                    adapter.notifyDataSetChanged();
                    emptyStateCard.setVisibility(reviewItems.isEmpty() ? View.VISIBLE : View.GONE);
                    updateAverageRating();

                    Log.d(TAG, "UI updated. Rendered items count: " + reviewItems.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading reviews", e);
                    Toast.makeText(this, "Failed to load reviews. Check connection/logs.", Toast.LENGTH_LONG).show();
                });
    }

    private void updateAverageRating() {
        float sum = 0f;
        int count = 0;
        for (ReviewFeedbackItem item : reviewItems) {
            if (!item.isComplaint() && item.getRating() > 0f) {
                sum += item.getRating();
                count++;
            }
        }
        float average = count == 0 ? 0f : (sum / count);
        tvAverageRating.setText(String.format(Locale.getDefault(), "Average Rating: %.1f (%d reviews)", average, count));
    }

    private void showAddReviewDialog() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_review, null, false);
        RatingBar ratingBar = dialogView.findViewById(R.id.dialogRatingBar);
        RadioGroup radioGroupType = dialogView.findViewById(R.id.dialogTypeGroup);
        EditText etComment = dialogView.findViewById(R.id.etDialogComment);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitReviewDialog);

        // Complaint mode does not need a rating.
        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isComplaint = checkedId == R.id.rbComplaint;
            ratingBar.setVisibility(isComplaint ? View.GONE : View.VISIBLE);
            if (isComplaint) {
                ratingBar.setRating(0f);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnSubmit.setOnClickListener(v -> {
            String comment = etComment.getText().toString().trim();
            float rating = ratingBar.getRating();
            String type = radioGroupType.getCheckedRadioButtonId() == R.id.rbComplaint ? "Complaint" : "Review";

            if ("Review".equalsIgnoreCase(type) && rating <= 0f) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }
            if (comment.isEmpty()) {
                Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
                return;
            }

            submitReview(type, rating, comment, dialog);
        });

        dialog.show();
    }

    private void submitReview(String type, float rating, String comment, AlertDialog dialog) {
        String customerId = auth.getCurrentUser().getUid();

        db.collection("Users")
                .document(customerId)
                .get()
                .addOnSuccessListener(doc -> {
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("renterId", renterId);
                    payload.put("customerId", customerId);
                    payload.put("customerName", buildCustomerName(doc));
                    payload.put("rating", "Review".equalsIgnoreCase(type) ? rating : null);
                    payload.put("reviewText", comment);
                    payload.put("type", type);
                    payload.put("timestamp", Timestamp.now());

                    db.collection("reviews")
                            .add(payload)
                            .addOnSuccessListener(unused -> {
                                Log.d(TAG, "Review submitted successfully.");
                                Toast.makeText(this, "Feedback submitted", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                loadReviews();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to submit review", e);
                                Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to read customer profile", e);
                    Toast.makeText(this, "Failed to read customer profile", Toast.LENGTH_SHORT).show();
                });
    }

    private String buildCustomerName(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            return "Customer";
        }

        String firstName = safe(doc.getString("firstName"), "");
        String lastName = safe(doc.getString("lastName"), "");
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isEmpty()) {
            return fullName;
        }

        String name = safe(doc.getString("name"), "");
        if (!name.isEmpty()) {
            return name;
        }

        return safe(doc.getString("email"), "Customer");
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
