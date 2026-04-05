package com.example.myapplication.admin;

public class AdminBookingItem {

    private final String bookingId;
    private final String customerId;
    private final String renterId;
    private final String vehicleId;
    private final String driverId;
    private final String startDate;
    private final String endDate;
    private final double totalPrice;
    private final String status;

    private int bookingNumber; // Sequential number (1, 2, 3...)

    private String customerName;
    private String renterName;
    private String renterOrgName;
    private String renterOwnerName;
    private String vehicleModel;
    private String driverName;

    public AdminBookingItem(
            String bookingId,
            String customerId,
            String renterId,
            String vehicleId,
            String driverId,
            String startDate,
            String endDate,
            double totalPrice,
            String status
    ) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.renterId = renterId;
        this.vehicleId = vehicleId;
        this.driverId = driverId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public String getBookingId() {
        return bookingId;
    }

    public int getBookingNumber() {
        return bookingNumber;
    }

    public void setBookingNumber(int bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getRenterId() {
        return renterId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getRenterName() {
        return renterName;
    }

    public void setRenterName(String renterName) {
        this.renterName = renterName;
    }

    public String getRenterOrgName() {
        return renterOrgName;
    }

    public void setRenterOrgName(String renterOrgName) {
        this.renterOrgName = renterOrgName;
    }

    public String getRenterOwnerName() {
        return renterOwnerName;
    }

    public void setRenterOwnerName(String renterOwnerName) {
        this.renterOwnerName = renterOwnerName;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
}
