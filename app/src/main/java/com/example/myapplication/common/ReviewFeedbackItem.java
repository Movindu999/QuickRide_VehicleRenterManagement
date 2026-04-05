package com.example.myapplication.common;

import com.google.firebase.Timestamp;

public class ReviewFeedbackItem {

    private final String id;
    private final String renterId;
    private final String customerId;
    private final String customerName;
    private final float rating;
    private final String reviewText;
    private final String type;
    private final Timestamp timestamp;

    public ReviewFeedbackItem(
            String id,
            String renterId,
            String customerId,
            String customerName,
            float rating,
            String reviewText,
            String type,
            Timestamp timestamp
    ) {
        this.id = id;
        this.renterId = renterId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.rating = rating;
        this.reviewText = reviewText;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getRenterId() {
        return renterId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public float getRating() {
        return rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public String getType() {
        return type;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public boolean isComplaint() {
        return "Complaint".equalsIgnoreCase(type);
    }
}

