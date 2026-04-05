package com.example.myapplication.driver;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DriverBookingAdapter extends RecyclerView.Adapter<DriverBookingAdapter.BookingViewHolder> {

    public interface OnBookingClickListener {
        void onBookingClick(BookingItem item);
    }

    public interface OnCancelBookingClickListener {
        void onCancelBookingClick(BookingItem item);
    }

    private List<BookingItem> bookingList = new ArrayList<>();
    private OnBookingClickListener clickListener;
    private OnCancelBookingClickListener cancelClickListener;

    public void setOnBookingClickListener(OnBookingClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnCancelBookingClickListener(OnCancelBookingClickListener listener) {
        this.cancelClickListener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_driver_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingItem booking = bookingList.get(position);
        holder.bind(booking);
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onBookingClick(booking);
            }
        });

        boolean canCancel = "Booked".equalsIgnoreCase(booking.getStatus());
        holder.btnCancelBooking.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        holder.btnCancelBooking.setOnClickListener(canCancel && cancelClickListener != null
                ? v -> cancelClickListener.onCancelBookingClick(booking)
                : null);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public void setBookings(List<BookingItem> bookings) {
        this.bookingList = bookings;
        notifyDataSetChanged();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView txtBookingNumber;
        TextView txtCustomerId;
        TextView txtVehicleType;
        TextView txtStartDate;
        TextView txtEndDate;
        TextView txtStatus;
        MaterialButton btnCancelBooking;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBookingNumber = itemView.findViewById(R.id.txtBookingNumber);
            txtCustomerId = itemView.findViewById(R.id.txtCustomerId);
            txtVehicleType = itemView.findViewById(R.id.txtVehicleType);
            txtStartDate = itemView.findViewById(R.id.txtStartDate);
            txtEndDate = itemView.findViewById(R.id.txtEndDate);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
        }

        public void bind(BookingItem booking) {
            txtBookingNumber.setText(booking.getBookingNumber());
            txtCustomerId.setText(booking.getCustomerName());
            txtVehicleType.setText(booking.getVehicleLabel());
            txtStartDate.setText(booking.getStartDate());
            txtEndDate.setText(booking.getEndDate());
            txtStatus.setText(booking.getStatus());
        }
    }

    public static class BookingItem {
        private final String bookingNumber;
        private final String customerId;
        private final String startDate;
        private final String endDate;
        private String customerName;
        private String vehicleId;
        private String vehicleLabel;
        private String status;

        public BookingItem(String bookingNumber, String customerId, String startDate, String endDate, String customerName) {
            this.bookingNumber = bookingNumber;
            this.customerId = customerId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.customerName = customerName;
            this.vehicleLabel = "Vehicle";
            this.status = "Booked";
        }

        public String getBookingNumber() { return bookingNumber; }
        public String getCustomerId() { return customerId; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public String getCustomerName() { return customerName; }
        public String getVehicleId() { return vehicleId; }
        public String getVehicleLabel() { return vehicleLabel; }
        public String getStatus() { return status; }

        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
        public void setVehicleLabel(String vehicleLabel) { this.vehicleLabel = vehicleLabel; }
        public void setStatus(String status) { this.status = TextUtils.isEmpty(status) ? "Booked" : status; }
    }
}
