package com.example.myapplication.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ViewRentersActivity extends AppCompatActivity {

    private RecyclerView rvRenters;
    private View emptyStateCard;
    private final List<RenterItem> renterItems = new ArrayList<>();
    private RenterAdapter renterAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_renters);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle(R.string.screen_view_renters);
        topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        rvRenters = findViewById(R.id.rvRenters);
        emptyStateCard = findViewById(R.id.emptyStateCard);

        rvRenters.setLayoutManager(new LinearLayoutManager(this));
        renterAdapter = new RenterAdapter(renterItems, this::openRenterVehicles);
        rvRenters.setAdapter(renterAdapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavCustomer);
        bottomNav.setSelectedItemId(R.id.nav_customer_renters);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_customer_renters) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_customer_dashboard) {
                intent = new Intent(this, CustomerDashboardActivity.class);
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

        listenForRenters();
    }

    private void listenForRenters() {
        db.collection("Users")
                .whereIn("role", java.util.Arrays.asList("renter", "Renter"))
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load renters: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    renterItems.clear();
                    if (value != null) {
                        value.getDocuments().forEach(doc -> renterItems.add(new RenterItem(
                                doc.getId(),
                                safeText(doc.getString("orgName"), "Renter"),
                                safeText(doc.getString("ownerName"), "Unknown Owner"),
                                safeText(doc.getString("location"), "Location not available")
                        )));
                    }

                    renterAdapter.notifyDataSetChanged();
                    emptyStateCard.setVisibility(renterItems.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void openRenterVehicles(RenterItem item) {
        Intent intent = new Intent(this, RenterVehiclesActivity.class);
        intent.putExtra("renterId", item.renterId);
        intent.putExtra("renterName", item.orgName);
        startActivity(intent);
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static class RenterItem {
        final String renterId;
        final String orgName;
        final String ownerName;
        final String location;

        RenterItem(String renterId, String orgName, String ownerName, String location) {
            this.renterId = renterId;
            this.orgName = orgName;
            this.ownerName = ownerName;
            this.location = location;
        }
    }

    private static class RenterAdapter extends RecyclerView.Adapter<RenterAdapter.RenterViewHolder> {

        interface OnRenterClickListener {
            void onRenterClick(RenterItem item);
        }

        private final List<RenterItem> items;
        private final OnRenterClickListener listener;

        RenterAdapter(List<RenterItem> items, OnRenterClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public RenterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_renter_customer, parent, false);
            return new RenterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RenterViewHolder holder, int position) {
            RenterItem item = items.get(position);
            holder.tvOrgName.setText(item.orgName);
            holder.tvOwnerName.setText("Owner: " + item.ownerName);
            holder.tvLocation.setText("Location: " + item.location);
            holder.itemView.setOnClickListener(v -> listener.onRenterClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class RenterViewHolder extends RecyclerView.ViewHolder {
            final TextView tvOrgName;
            final TextView tvOwnerName;
            final TextView tvLocation;

            RenterViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOrgName = itemView.findViewById(R.id.tvRenterOrgName);
                tvOwnerName = itemView.findViewById(R.id.tvRenterOwnerName);
                tvLocation = itemView.findViewById(R.id.tvRenterLocation);
            }
        }
    }
}
