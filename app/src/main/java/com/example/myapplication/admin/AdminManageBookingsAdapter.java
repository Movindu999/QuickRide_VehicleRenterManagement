package com.example.myapplication.admin;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AdminManageBookingsAdapter extends RecyclerView.Adapter<AdminManageBookingsAdapter.AdminBookingViewHolder> {

    private final List<AdminBookingItem> items = new ArrayList<>();
    private final FirebaseFirestore db;

    private final Map<String, String> userNameCache = new HashMap<>();
    private final Map<String, String> driverNameCache = new HashMap<>();
    private final Map<String, String> vehicleNameCache = new HashMap<>();

    private final Set<String> userRequestsInFlight = new HashSet<>();
    private final Set<String> driverRequestsInFlight = new HashSet<>();
    private final Set<String> vehicleRequestsInFlight = new HashSet<>();

    public AdminManageBookingsAdapter(FirebaseFirestore db) {
        this.db = db;
    }

    public void submitList(List<AdminBookingItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminBookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_booking, parent, false);
        return new AdminBookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminBookingViewHolder holder, int position) {
        AdminBookingItem item = items.get(position);

        holder.tvBookingId.setText("Booking #" + item.getBookingNumber());
        holder.tvDates.setText(String.format(Locale.getDefault(), "Dates: %s to %s", safe(item.getStartDate(), "N/A"), safe(item.getEndDate(), "N/A")));
        holder.tvTotalPrice.setText(String.format(Locale.getDefault(), "Total Price: %.2f", item.getTotalPrice()));
        holder.tvStatus.setText("Status: " + safe(item.getStatus(), "N/A"));

        bindUserName(holder.tvCustomerName, "Customer", item.getCustomerId(), item.getCustomerName(), true);
        bindRenterDetails(holder.tvRenterName, item);
        bindVehicleName(holder.tvVehicleModel, item);
        bindDriverName(holder.tvDriverName, item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void bindRenterDetails(TextView view, AdminBookingItem item) {
        if (!TextUtils.isEmpty(item.getRenterOrgName()) || !TextUtils.isEmpty(item.getRenterOwnerName())) {
            String renterInfo = buildRenterInfo(item.getRenterOrgName(), item.getRenterOwnerName());
            view.setText("Renter: " + renterInfo);
            return;
        }

        if (TextUtils.isEmpty(item.getRenterId())) {
            view.setText("Renter: None");
            return;
        }

        view.setText("Renter: Loading...");
        fetchRenterDetails(item.getRenterId());
    }

    private String buildRenterInfo(String orgName, String ownerName) {
        String org = safe(orgName, "");
        String owner = safe(ownerName, "");

        if (!org.isEmpty() && !owner.isEmpty()) {
            return org + " (" + owner + ")";
        } else if (!org.isEmpty()) {
            return org;
        } else if (!owner.isEmpty()) {
            return owner;
        } else {
            return "Renter";
        }
    }

    private void fetchRenterDetails(String renterId) {
        db.collection("Users").document(renterId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String orgName = safe(doc.getString("orgName"), "");
                        String ownerName = safe(doc.getString("ownerName"), "");
                        applyRenterDetailsToItems(renterId, orgName, ownerName);
                        notifyDataSetChanged();
                        return;
                    }

                    db.collection("users").document(renterId).get()
                            .addOnSuccessListener(fallbackDoc -> {
                                String orgName = safe(fallbackDoc.getString("orgName"), "");
                                String ownerName = safe(fallbackDoc.getString("ownerName"), "");
                                applyRenterDetailsToItems(renterId, orgName, ownerName);
                                notifyDataSetChanged();
                            });
                });
    }

    private void applyRenterDetailsToItems(String renterId, String orgName, String ownerName) {
        for (AdminBookingItem item : items) {
            if (renterId.equals(item.getRenterId())) {
                item.setRenterOrgName(orgName);
                item.setRenterOwnerName(ownerName);
            }
        }
    }

    private void bindUserName(TextView view, String label, String userId, String resolvedName, boolean isCustomer) {
        if (!TextUtils.isEmpty(resolvedName)) {
            view.setText(label + ": " + resolvedName);
            return;
        }

        if (TextUtils.isEmpty(userId)) {
            view.setText(label + ": None");
            return;
        }

        String cachedName = userNameCache.get(userId);
        if (!TextUtils.isEmpty(cachedName)) {
            view.setText(label + ": " + cachedName);
            return;
        }

        view.setText(label + ": Loading...");
        if (userRequestsInFlight.add(userId)) {
            fetchUserName(userId, isCustomer);
        }
    }

    private void bindVehicleName(TextView view, AdminBookingItem item) {
        if (!TextUtils.isEmpty(item.getVehicleModel())) {
            view.setText("Vehicle: " + item.getVehicleModel());
            return;
        }

        if (TextUtils.isEmpty(item.getVehicleId())) {
            view.setText("Vehicle: None");
            return;
        }

        String cachedName = vehicleNameCache.get(item.getVehicleId());
        if (!TextUtils.isEmpty(cachedName)) {
            item.setVehicleModel(cachedName);
            view.setText("Vehicle: " + cachedName);
            return;
        }

        view.setText("Vehicle: Loading...");
        if (vehicleRequestsInFlight.add(item.getVehicleId())) {
            fetchVehicleName(item.getVehicleId());
        }
    }

    private void bindDriverName(TextView view, AdminBookingItem item) {
        if (!TextUtils.isEmpty(item.getDriverName())) {
            view.setText("Driver: " + item.getDriverName());
            return;
        }

        String driverId = item.getDriverId();
        if (TextUtils.isEmpty(driverId)) {
            view.setText("Driver: Self Drive / None");
            return;
        }

        String cachedName = driverNameCache.get(driverId);
        if (!TextUtils.isEmpty(cachedName)) {
            item.setDriverName(cachedName);
            view.setText("Driver: " + cachedName);
            return;
        }

        view.setText("Driver: Loading...");
        if (driverRequestsInFlight.add(driverId)) {
            fetchDriverName(driverId);
        }
    }

    private void fetchUserName(String userId, boolean isCustomer) {
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    String name = extractPersonName(doc, (isCustomer ? "Customer " : "Renter ") + userId);
                    userNameCache.put(userId, name);
                    applyUserNameToItems(userId, name, isCustomer);
                    userRequestsInFlight.remove(userId);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Fallback to lowercase collection if project uses lowercase names.
                    db.collection("users").document(userId).get()
                            .addOnSuccessListener(doc -> {
                                String name = extractPersonName(doc, (isCustomer ? "Customer " : "Renter ") + userId);
                                userNameCache.put(userId, name);
                                applyUserNameToItems(userId, name, isCustomer);
                                userRequestsInFlight.remove(userId);
                                notifyDataSetChanged();
                            })
                            .addOnFailureListener(inner -> {
                                String fallback = (isCustomer ? "Customer " : "Renter ") + userId;
                                userNameCache.put(userId, fallback);
                                applyUserNameToItems(userId, fallback, isCustomer);
                                userRequestsInFlight.remove(userId);
                                notifyDataSetChanged();
                            });
                });
    }

    private void fetchVehicleName(String vehicleId) {
        db.collection("Vehicles").document(vehicleId).get()
                .addOnSuccessListener(doc -> {
                    String vehicleName = extractVehicleName(doc, vehicleId);
                    vehicleNameCache.put(vehicleId, vehicleName);
                    applyVehicleNameToItems(vehicleId, vehicleName);
                    vehicleRequestsInFlight.remove(vehicleId);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> db.collection("vehicles").document(vehicleId).get()
                        .addOnSuccessListener(doc -> {
                            String vehicleName = extractVehicleName(doc, vehicleId);
                            vehicleNameCache.put(vehicleId, vehicleName);
                            applyVehicleNameToItems(vehicleId, vehicleName);
                            vehicleRequestsInFlight.remove(vehicleId);
                            notifyDataSetChanged();
                        })
                        .addOnFailureListener(inner -> {
                            String fallback = "Vehicle " + vehicleId;
                            vehicleNameCache.put(vehicleId, fallback);
                            applyVehicleNameToItems(vehicleId, fallback);
                            vehicleRequestsInFlight.remove(vehicleId);
                            notifyDataSetChanged();
                        }));
    }

    private void fetchDriverName(String driverId) {
        db.collection("Drivers").document(driverId).get()
                .addOnSuccessListener(doc -> {
                    String name = extractPersonName(doc, "Driver " + driverId);
                    if (doc.exists()) {
                        driverNameCache.put(driverId, name);
                        applyDriverNameToItems(driverId, name);
                        driverRequestsInFlight.remove(driverId);
                        notifyDataSetChanged();
                        return;
                    }

                    db.collection("Users").document(driverId).get()
                            .addOnSuccessListener(userDoc -> {
                                String fallbackName = extractPersonName(userDoc, "Driver " + driverId);
                                driverNameCache.put(driverId, fallbackName);
                                applyDriverNameToItems(driverId, fallbackName);
                                driverRequestsInFlight.remove(driverId);
                                notifyDataSetChanged();
                            })
                            .addOnFailureListener(inner -> onDriverFallbackLookup(driverId));
                })
                .addOnFailureListener(e -> onDriverFallbackLookup(driverId));
    }

    private void onDriverFallbackLookup(String driverId) {
        db.collection("drivers").document(driverId).get()
                .addOnSuccessListener(doc -> {
                    String name = extractPersonName(doc, "Driver " + driverId);
                    driverNameCache.put(driverId, name);
                    applyDriverNameToItems(driverId, name);
                    driverRequestsInFlight.remove(driverId);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> db.collection("users").document(driverId).get()
                        .addOnSuccessListener(userDoc -> {
                            String name = extractPersonName(userDoc, "Driver " + driverId);
                            driverNameCache.put(driverId, name);
                            applyDriverNameToItems(driverId, name);
                            driverRequestsInFlight.remove(driverId);
                            notifyDataSetChanged();
                        })
                        .addOnFailureListener(inner -> {
                            String fallback = "Driver " + driverId;
                            driverNameCache.put(driverId, fallback);
                            applyDriverNameToItems(driverId, fallback);
                            driverRequestsInFlight.remove(driverId);
                            notifyDataSetChanged();
                        }));
    }

    private void applyUserNameToItems(String userId, String displayName, boolean isCustomer) {
        for (AdminBookingItem item : items) {
            if (isCustomer && userId.equals(item.getCustomerId())) {
                item.setCustomerName(displayName);
            } else if (!isCustomer && userId.equals(item.getRenterId())) {
                item.setRenterName(displayName);
            }
        }
    }

    private void applyVehicleNameToItems(String vehicleId, String displayName) {
        for (AdminBookingItem item : items) {
            if (vehicleId.equals(item.getVehicleId())) {
                item.setVehicleModel(displayName);
            }
        }
    }

    private void applyDriverNameToItems(String driverId, String displayName) {
        for (AdminBookingItem item : items) {
            if (driverId.equals(item.getDriverId())) {
                item.setDriverName(displayName);
            }
        }
    }

    private String extractPersonName(DocumentSnapshot doc, String fallback) {
        if (doc == null || !doc.exists()) {
            return fallback;
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

        String ownerName = safe(doc.getString("ownerName"), "");
        if (!ownerName.isEmpty()) {
            return ownerName;
        }

        return fallback;
    }

    private String extractVehicleName(DocumentSnapshot doc, String vehicleId) {
        if (doc == null || !doc.exists()) {
            return "Vehicle " + vehicleId;
        }

        String model = safe(doc.getString("model"), "");
        if (!model.isEmpty()) {
            return model;
        }

        String vehicleModel = safe(doc.getString("vehicleModel"), "");
        if (!vehicleModel.isEmpty()) {
            return vehicleModel;
        }

        String type = safe(doc.getString("vehicleType"), "");
        String number = safe(doc.getString("vehicleNumber"), "");
        String label = (type + " " + number).trim();
        if (!label.isEmpty()) {
            return label;
        }

        return "Vehicle " + vehicleId;
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    static class AdminBookingViewHolder extends RecyclerView.ViewHolder {
        final TextView tvBookingId;
        final TextView tvVehicleModel;
        final TextView tvCustomerName;
        final TextView tvRenterName;
        final TextView tvDriverName;
        final TextView tvDates;
        final TextView tvTotalPrice;
        final TextView tvStatus;

        AdminBookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tvAdminBookingId);
            tvVehicleModel = itemView.findViewById(R.id.tvAdminVehicleModel);
            tvCustomerName = itemView.findViewById(R.id.tvAdminCustomerName);
            tvRenterName = itemView.findViewById(R.id.tvAdminRenterName);
            tvDriverName = itemView.findViewById(R.id.tvAdminDriverName);
            tvDates = itemView.findViewById(R.id.tvAdminBookingDates);
            tvTotalPrice = itemView.findViewById(R.id.tvAdminTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvAdminStatus);
        }
    }
}

