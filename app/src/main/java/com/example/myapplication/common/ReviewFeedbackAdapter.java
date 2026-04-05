package com.example.myapplication.common;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewFeedbackAdapter extends RecyclerView.Adapter<ReviewFeedbackAdapter.ReviewViewHolder> {

    private static final String TAG = "ReviewsList";

    private final List<ReviewFeedbackItem> items;

    public ReviewFeedbackAdapter(List<ReviewFeedbackItem> items) {
        this.items = items;
        Log.d(TAG, "Adapter initialized. Initial size: " + (items == null ? 0 : items.size()));
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_feedback, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        try {
            ReviewFeedbackItem item = items.get(position);

            holder.tvCustomerName.setText("Customer: " + safe(item.getCustomerName(), "Anonymous"));
            holder.tvType.setText("Type: " + safe(item.getType(), "Review"));
            holder.tvReviewText.setText(safe(item.getReviewText(), "No message"));

            if (item.isComplaint()) {
                holder.ratingBar.setVisibility(View.GONE);
                holder.tvType.setTextColor(Color.parseColor("#C0392B"));
                holder.tvReviewText.setTextColor(Color.parseColor("#C0392B"));
            } else {
                holder.ratingBar.setVisibility(View.VISIBLE);
                holder.ratingBar.setRating(item.getRating());
                holder.tvType.setTextColor(holder.defaultTypeColor);
                holder.tvReviewText.setTextColor(holder.defaultBodyColor);
            }

            if (item.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                holder.tvTimestamp.setText(sdf.format(item.getTimestamp().toDate()));
            } else {
                holder.tvTimestamp.setText("-");
            }

            Log.d(TAG, "Bound review at position " + position + " for customer: " + safe(item.getCustomerName(), "Anonymous"));
        } catch (Exception e) {
            Log.e(TAG, "Adapter bind failed at position: " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        final TextView tvCustomerName;
        final TextView tvType;
        final TextView tvReviewText;
        final TextView tvTimestamp;
        final RatingBar ratingBar;
        final int defaultTypeColor;
        final int defaultBodyColor;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvReviewCustomerName);
            tvType = itemView.findViewById(R.id.tvReviewType);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            tvTimestamp = itemView.findViewById(R.id.tvReviewTimestamp);
            ratingBar = itemView.findViewById(R.id.ratingReview);
            defaultTypeColor = tvType.getCurrentTextColor();
            defaultBodyColor = tvReviewText.getCurrentTextColor();
        }
    }
}
