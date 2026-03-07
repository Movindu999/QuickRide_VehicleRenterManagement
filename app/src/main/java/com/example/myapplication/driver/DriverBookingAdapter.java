package com.example.myapplication.driver;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DriverBookingAdapter extends RecyclerView.Adapter<DriverBookingAdapter.BookingViewHolder> {

    private List<BookingItem> bookingList = new ArrayList<>();

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
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public void setBookings(List<BookingItem> bookings) {
        this.bookingList = bookings;
        notifyDataSetChanged();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView txtBookingNumber;
        TextView txtCustomerId;
        TextView txtVehicleType;
        TextView txtStartDate;
        TextView txtEndDate;
        MaterialButton btnCancelBooking;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBookingNumber = itemView.findViewById(R.id.txtBookingNumber);
            txtCustomerId = itemView.findViewById(R.id.txtCustomerId);
            txtVehicleType = itemView.findViewById(R.id.txtVehicleType);
            txtStartDate = itemView.findViewById(R.id.txtStartDate);
            txtEndDate = itemView.findViewById(R.id.txtEndDate);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
        }

        public void bind(BookingItem booking) {
            txtBookingNumber.setText(booking.getBookingNumber());
            txtCustomerId.setText(booking.getCustomerId());
            txtVehicleType.setText(booking.getVehicleType());
            txtStartDate.setText(booking.getStartDate());
            txtEndDate.setText(booking.getEndDate());

            btnCancelBooking.setOnClickListener(v -> {
                // Backend will be implemented later
                Toast.makeText(itemView.getContext(),
                    "Cancel booking: " + booking.getBookingNumber(),
                    Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Simple data class for booking items (temporary, backend will have proper model)
    public static class BookingItem {
        private String bookingNumber;
        private String customerId;
        private String vehicleType;
        private String startDate;
        private String endDate;

        public BookingItem(String bookingNumber, String customerId, String vehicleType,
                          String startDate, String endDate) {
            this.bookingNumber = bookingNumber;
            this.customerId = customerId;
            this.vehicleType = vehicleType;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public String getBookingNumber() {
            return bookingNumber;
        }

        public String getCustomerId() {
            return customerId;
        }

        public String getVehicleType() {
            return vehicleType;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }
    }
}

