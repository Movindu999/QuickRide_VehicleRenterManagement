package com.example.myapplication.customer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class BookingActivity extends AppCompatActivity {

    private static final String TAG = "BookingActivity";
    private static final long DAY_MILLIS = 24L * 60L * 60L * 1000L;
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat FIRESTORE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    static {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        DISPLAY_DATE_FORMAT.setTimeZone(utc);
        FIRESTORE_DATE_FORMAT.setTimeZone(utc);
    }

    private TextView tvVehicleTitle;
    private TextView tvVehiclePricePerDay;
    private ImageView ivVehicle;
    private TextView tvBookingSelectedRange;
    private TextView tvBookingTotalDays;
    private TextView tvBookingTotalPrice;
    private Spinner spinnerDrivers;
    private RadioGroup rgPaymentMethod;  ///
    private Button btnSelectDateRange;
    private Button btnConfirmBooking;
    private Button btnCancelBooking;
    private Button btnViewDriverProfile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String renterId;
    private String vehicleId;
    private String vehicleType;
    private String vehicleNumber;
    private String vehicleImageUrl;
    private double pricePerDay;
    private double driverFeePerDay;

    private long selectedStartDateMillis = -1L;
    private long selectedEndDateMillis = -1L;

    private final List<DriverItem> drivers = new ArrayList<>();
    private ArrayAdapter<String> driverAdapter;
    private final List<BookedRange> blockedRanges = new ArrayList<>();
    private boolean bookedRangesLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        renterId = getIntent().getStringExtra("renterId");
        vehicleId = getIntent().getStringExtra("vehicleId");
        vehicleType = getIntent().getStringExtra("vehicleType");
        vehicleNumber = getIntent().getStringExtra("vehicleNumber");
        vehicleImageUrl = getIntent().getStringExtra("vehicleImageUrl");
        pricePerDay = getIntent().getDoubleExtra("pricePerDay", 0d);
        driverFeePerDay = getIntent().getDoubleExtra("driverFeePerDay", 0d);

        if (TextUtils.isEmpty(renterId) || TextUtils.isEmpty(vehicleId)) {
            Toast.makeText(this, "Booking details are missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle("Booking");
        topAppBar.setNavigationIcon(android.R.drawable.ic_media_previous);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        tvVehicleTitle = findViewById(R.id.tvBookingVehicleTitle);
        tvVehiclePricePerDay = findViewById(R.id.tvBookingVehiclePricePerDay);
        ivVehicle = findViewById(R.id.ivBookingVehicle);
        tvBookingSelectedRange = findRuntimeTextView("tvBookingSelectedRange");
        tvBookingTotalDays = findViewById(R.id.tvBookingTotalDays);
        tvBookingTotalPrice = findViewById(R.id.tvBookingTotalPrice);
        spinnerDrivers = findViewById(R.id.spinnerDrivers);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod); ////
        btnSelectDateRange = findRuntimeButton("btnSelectDateRange");
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        btnCancelBooking = findViewById(R.id.btnCancelBooking);
        btnViewDriverProfile = findViewById(R.id.btnViewDriverProfile);

        tvVehicleTitle.setText(String.format(
                Locale.getDefault(),
                "%s - %s",
                safe(vehicleType, "Vehicle"),
                safe(vehicleNumber, "N/A")
        ));
        tvVehiclePricePerDay.setText(String.format(Locale.getDefault(), "Price per day: %.2f | Driver fee per day: %.2f", pricePerDay, driverFeePerDay));
        Glide.with(this)
                .load(vehicleImageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(ivVehicle);

        resetBookingSummary();
        setupDriverSpinner();
        loadDrivers();
        loadBookedDateRanges();

        btnSelectDateRange.setEnabled(false);
        btnSelectDateRange.setOnClickListener(v -> openDateRangePicker());
        btnConfirmBooking.setOnClickListener(v -> saveBooking());
        btnCancelBooking.setOnClickListener(v -> cancelBooking());
        btnViewDriverProfile.setOnClickListener(v -> viewDriverProfile());
    }

    private void setupDriverSpinner() {
        drivers.clear();
        drivers.add(new DriverItem("", getString(R.string.option_self_drive)));

        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.option_self_drive));
        driverAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        driverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDrivers.setAdapter(driverAdapter);
    }

    private void loadDrivers() {
        db.collection("Drivers")
                .whereEqualTo("renterId", renterId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    drivers.clear();
                    drivers.add(new DriverItem("", getString(R.string.option_self_drive)));
                    driverAdapter.clear();
                    driverAdapter.add(getString(R.string.option_self_drive));

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String firstName = safe(doc.getString("firstName"), "");
                        String lastName = safe(doc.getString("lastName"), "");
                        String fullName = (firstName + " " + lastName).trim();
                        if (fullName.isEmpty()) {
                            fullName = "Driver " + doc.getId();
                        }
                        drivers.add(new DriverItem(doc.getId(), fullName));
                        driverAdapter.add(fullName);
                    }
                    driverAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load drivers: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadBookedDateRanges() {
        bookedRangesLoaded = false;
        btnSelectDateRange.setEnabled(false);
        tvBookingSelectedRange.setText(R.string.loading_booked_dates);

        db.collection("Bookings")
                .whereEqualTo("vehicleId", vehicleId)
                .get()
                .addOnSuccessListener(this::handleBookedDateRanges)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load booked date ranges", e);
                    blockedRanges.clear();
                    bookedRangesLoaded = true;
                    btnSelectDateRange.setEnabled(true);
                    tvBookingSelectedRange.setText(R.string.label_no_dates_selected);
                    Toast.makeText(this, R.string.loading_booked_dates_failed, Toast.LENGTH_LONG).show();
                });
    }

    private void handleBookedDateRanges(QuerySnapshot snapshot) {
        blockedRanges.clear();
        if (snapshot != null) {
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Long startMillis = resolveBookingDateMillis(doc, "startDateMillis", "startDate");
                Long endMillis = resolveBookingDateMillis(doc, "endDateMillis", "endDate");
                if (startMillis == null || endMillis == null) {
                    continue;
                }

                long normalizedStart = normalizeUtcDay(startMillis);
                long normalizedEnd = normalizeUtcDay(endMillis);
                if (normalizedEnd < normalizedStart) {
                    long swap = normalizedStart;
                    normalizedStart = normalizedEnd;
                    normalizedEnd = swap;
                }
                blockedRanges.add(new BookedRange(normalizedStart, normalizedEnd));
            }
        }

        bookedRangesLoaded = true;
        btnSelectDateRange.setEnabled(true);
        tvBookingSelectedRange.setText(R.string.label_no_dates_selected);
    }

    private void openDateRangePicker() {
        if (!bookedRangesLoaded) {
            Toast.makeText(this, R.string.please_wait_booked_dates, Toast.LENGTH_SHORT).show();
            return;
        }

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setStart(normalizeUtcDay(System.currentTimeMillis()));
        constraintsBuilder.setValidator(new BookingDateValidator(normalizeUtcDay(System.currentTimeMillis()), blockedRanges));

        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(R.string.label_select_booking_dates)
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null || selection.first == null || selection.second == null) {
                return;
            }

            long start = normalizeUtcDay(selection.first);
            long end = normalizeUtcDay(selection.second);
            if (end < start) {
                long swap = start;
                start = end;
                end = swap;
            }

            if (rangeOverlapsBookedDates(start, end, blockedRanges)) {
                Toast.makeText(this, R.string.selected_dates_overlap, Toast.LENGTH_LONG).show();
                return;
            }

            selectedStartDateMillis = start;
            selectedEndDateMillis = end;
            updateSelectedRangeViews();
            recalculateTotals();
        });

        picker.show(getSupportFragmentManager(), "booking_range_picker");
    }

    private void updateSelectedRangeViews() {
        if (selectedStartDateMillis > 0 && selectedEndDateMillis > 0) {
            tvBookingSelectedRange.setText(String.format(
                    Locale.getDefault(),
                    "%s to %s",
                    DISPLAY_DATE_FORMAT.format(selectedStartDateMillis),
                    DISPLAY_DATE_FORMAT.format(selectedEndDateMillis)
            ));
        } else {
            tvBookingSelectedRange.setText(R.string.label_no_dates_selected);
        }
    }

    private void resetBookingSummary() {
        selectedStartDateMillis = -1L;
        selectedEndDateMillis = -1L;
        tvBookingSelectedRange.setText(R.string.label_no_dates_selected);
        tvBookingTotalDays.setText(String.format(Locale.getDefault(), getString(R.string.label_total_days_format), 0));
        tvBookingTotalPrice.setText(String.format(Locale.getDefault(), getString(R.string.label_total_price_format), 0d));
    }

    private void recalculateTotals() {
        if (selectedStartDateMillis <= 0 || selectedEndDateMillis <= 0 || selectedEndDateMillis < selectedStartDateMillis) {
            tvBookingTotalDays.setText(String.format(Locale.getDefault(), getString(R.string.label_total_days_format), 0));
            tvBookingTotalPrice.setText(String.format(Locale.getDefault(), getString(R.string.label_total_price_format), 0d));
            return;
        }

        int totalDays = (int) ((selectedEndDateMillis - selectedStartDateMillis) / DAY_MILLIS) + 1;
        double basePrice = totalDays * pricePerDay;
        double driverPrice = isSelfDriveSelected() ? 0d : (totalDays * driverFeePerDay);
        double totalPrice = basePrice + driverPrice;

        tvBookingTotalDays.setText(String.format(Locale.getDefault(), getString(R.string.label_total_days_format), totalDays));
        tvBookingTotalPrice.setText(String.format(Locale.getDefault(), getString(R.string.label_total_price_format), totalPrice));
    }

    private void saveBooking() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedStartDateMillis <= 0 || selectedEndDateMillis <= 0) {
            Toast.makeText(this, "Please select booking dates", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedEndDateMillis < selectedStartDateMillis) {
            Toast.makeText(this, "End date cannot be before start date", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmBooking.setEnabled(false);

        final String customerId = mAuth.getCurrentUser().getUid();
        final int totalDays = (int) ((selectedEndDateMillis - selectedStartDateMillis) / DAY_MILLIS) + 1;
        final boolean selfDriveSelected = isSelfDriveSelected();
        final String selectedDriverId = getSelectedDriverId();
        final double totalPrice = (totalDays * pricePerDay) + (selfDriveSelected ? 0d : (totalDays * driverFeePerDay));
        final String startDate = FIRESTORE_DATE_FORMAT.format(selectedStartDateMillis);
        final String endDate = FIRESTORE_DATE_FORMAT.format(selectedEndDateMillis);
        final String bookingStatus = "Booked";

        db.collection("Bookings")
                .whereEqualTo("vehicleId", vehicleId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<BookedRange> latestRanges = extractBookedRanges(snapshot);
                    if (rangeOverlapsBookedDates(selectedStartDateMillis, selectedEndDateMillis, latestRanges)) {
                        btnConfirmBooking.setEnabled(true);
                        Toast.makeText(this, R.string.date_range_already_booked, Toast.LENGTH_LONG).show();
                        loadBookedDateRanges();
                        return;
                    }
/// /
                    String paymentMethod = "Cash";
                    if (rgPaymentMethod != null && rgPaymentMethod.getCheckedRadioButtonId() == R.id.rbCard) {
                        paymentMethod = "Card";
                    }

                    Map<String, Object> booking = new HashMap<>();
                    booking.put("customerId", customerId);
                    booking.put("renterId", renterId);
                    booking.put("vehicleId", vehicleId);
                    booking.put("driverId", selfDriveSelected ? null : selectedDriverId);
                    booking.put("paymentMethod", paymentMethod);
                    booking.put("startDate", startDate);
                    booking.put("endDate", endDate);
                    booking.put("startDateMillis", selectedStartDateMillis);
                    booking.put("endDateMillis", selectedEndDateMillis);
                    booking.put("totalDays", totalDays);
                    booking.put("pricePerDay", pricePerDay);
                    booking.put("driverFeePerDay", driverFeePerDay);
                    booking.put("totalPrice", totalPrice);
                    booking.put("status", bookingStatus);
                    booking.put("vehicleLabel", safe(vehicleType, "Vehicle") + " - " + safe(vehicleNumber, "N/A"));
                    booking.put("createdAt", Timestamp.now());
                    booking.put("updatedAt", Timestamp.now());

                    db.collection("Bookings").document()
                            .set(booking)
                            .addOnSuccessListener(unused -> {
                                btnConfirmBooking.setEnabled(true);
                                Toast.makeText(this, "Vehicle booked successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnConfirmBooking.setEnabled(true);
                                Toast.makeText(this, "Failed to book vehicle: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnConfirmBooking.setEnabled(true);
                    Toast.makeText(this, "Failed to verify booking dates: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void cancelBooking() {
        Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void viewDriverProfile() {
        if (isSelfDriveSelected()) {
            Toast.makeText(this, "Self-drive selected. No driver profile available.", Toast.LENGTH_SHORT).show();
            return;
        }

        String driverId = getSelectedDriverId();
        if (TextUtils.isEmpty(driverId)) {
            Toast.makeText(this, "Please select a driver first", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, com.example.myapplication.renter.DriverProfileActivity.class);
        intent.putExtra("driverId", driverId);
        startActivity(intent);
    }

    private boolean isSelfDriveSelected() {
        return spinnerDrivers.getSelectedItemPosition() <= 0;
    }

    private String getSelectedDriverId() {
        int selectedIndex = spinnerDrivers.getSelectedItemPosition();
        if (selectedIndex >= 0 && selectedIndex < drivers.size()) {
            return drivers.get(selectedIndex).driverId;
        }
        return "";
    }

    private List<BookedRange> extractBookedRanges(QuerySnapshot snapshot) {
        List<BookedRange> ranges = new ArrayList<>();
        if (snapshot == null) {
            return ranges;
        }

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Long startMillis = resolveBookingDateMillis(doc, "startDateMillis", "startDate");
            Long endMillis = resolveBookingDateMillis(doc, "endDateMillis", "endDate");
            if (startMillis == null || endMillis == null) {
                continue;
            }

            long normalizedStart = normalizeUtcDay(startMillis);
            long normalizedEnd = normalizeUtcDay(endMillis);
            if (normalizedEnd < normalizedStart) {
                long swap = normalizedStart;
                normalizedStart = normalizedEnd;
                normalizedEnd = swap;
            }
            ranges.add(new BookedRange(normalizedStart, normalizedEnd));
        }
        return ranges;
    }

    private Long resolveBookingDateMillis(DocumentSnapshot doc, String millisField, String textField) {
        Object millisValue = doc.get(millisField);
        if (millisValue instanceof Number) {
            return ((Number) millisValue).longValue();
        }
        if (millisValue instanceof Timestamp) {
            return ((Timestamp) millisValue).toDate().getTime();
        }

        Object textValue = doc.get(textField);
        if (textValue instanceof String) {
            try {
                java.util.Date parsedDate = FIRESTORE_DATE_FORMAT.parse((String) textValue);
                return parsedDate == null ? null : parsedDate.getTime();
            } catch (ParseException ignored) {
                return null;
            }
        }
        return null;
    }

    private long normalizeUtcDay(long millis) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private boolean rangeOverlapsBookedDates(long startMillis, long endMillis, List<BookedRange> ranges) {
        long normalizedStart = normalizeUtcDay(startMillis);
        long normalizedEnd = normalizeUtcDay(endMillis);

        for (BookedRange range : ranges) {
            if (normalizedStart <= range.endMillis && normalizedEnd >= range.startMillis) {
                return true;
            }
        }
        return false;
    }

    private TextView findRuntimeTextView(String idName) {
        int id = getResources().getIdentifier(idName, "id", getPackageName());
        return id != 0 ? findViewById(id) : null;
    }

    private Button findRuntimeButton(String idName) {
        int id = getResources().getIdentifier(idName, "id", getPackageName());
        return id != 0 ? findViewById(id) : null;
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static class DriverItem {
        final String driverId;
        final String displayName;

        DriverItem(String driverId, String displayName) {
            this.driverId = driverId;
            this.displayName = displayName;
        }
    }

    private static class BookedRange implements Parcelable {
        final long startMillis;
        final long endMillis;

        BookedRange(long startMillis, long endMillis) {
            this.startMillis = startMillis;
            this.endMillis = endMillis;
        }

        protected BookedRange(Parcel in) {
            startMillis = in.readLong();
            endMillis = in.readLong();
        }

        public static final Creator<BookedRange> CREATOR = new Creator<>() {
            @Override
            public BookedRange createFromParcel(Parcel in) {
                return new BookedRange(in);
            }

            @Override
            public BookedRange[] newArray(int size) {
                return new BookedRange[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(startMillis);
            dest.writeLong(endMillis);
        }
    }

    public static class BookingDateValidator implements CalendarConstraints.DateValidator {

        private final long minDateMillis;
        private final ArrayList<BookedRange> ranges;

        BookingDateValidator(long minDateMillis, List<BookedRange> ranges) {
            this.minDateMillis = minDateMillis;
            this.ranges = new ArrayList<>(ranges);
        }

        protected BookingDateValidator(Parcel in) {
            minDateMillis = in.readLong();
            ranges = in.createTypedArrayList(BookedRange.CREATOR);
            if (ranges == null) {
                throw new IllegalStateException("Booked ranges missing from parcel");
            }
        }

        public static final Creator<BookingDateValidator> CREATOR = new Creator<>() {
            @Override
            public BookingDateValidator createFromParcel(Parcel in) {
                return new BookingDateValidator(in);
            }

            @Override
            public BookingDateValidator[] newArray(int size) {
                return new BookingDateValidator[size];
            }
        };

        @Override
        public boolean isValid(long date) {
            long normalizedDate = normalizeStaticUtcDay(date);
            if (normalizedDate < minDateMillis) {
                return false;
            }
            for (BookedRange range : ranges) {
                if (normalizedDate >= range.startMillis && normalizedDate <= range.endMillis) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(minDateMillis);
            dest.writeTypedList(ranges);
        }

        private static long normalizeStaticUtcDay(long millis) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(millis);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
        }
    }
}
