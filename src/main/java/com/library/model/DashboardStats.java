package com.library.model;

import java.math.BigDecimal;

public class DashboardStats {
    private int totalBooks;
    private int availableBooks;
    private int rentedBooks;
    private int activeRentals;
    private int overdueRentals;
    private int returnedBooks;
    private BigDecimal outstandingFines = BigDecimal.ZERO;

    public int getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(int totalBooks) {
        this.totalBooks = totalBooks;
    }

    public int getAvailableBooks() {
        return availableBooks;
    }

    public void setAvailableBooks(int availableBooks) {
        this.availableBooks = availableBooks;
    }

    public int getRentedBooks() {
        return rentedBooks;
    }

    public void setRentedBooks(int rentedBooks) {
        this.rentedBooks = rentedBooks;
    }

    public int getActiveRentals() {
        return activeRentals;
    }

    public void setActiveRentals(int activeRentals) {
        this.activeRentals = activeRentals;
    }

    public int getOverdueRentals() {
        return overdueRentals;
    }

    public void setOverdueRentals(int overdueRentals) {
        this.overdueRentals = overdueRentals;
    }

    public int getReturnedBooks() {
        return returnedBooks;
    }

    public void setReturnedBooks(int returnedBooks) {
        this.returnedBooks = returnedBooks;
    }

    public BigDecimal getOutstandingFines() {
        return outstandingFines;
    }

    public void setOutstandingFines(BigDecimal outstandingFines) {
        this.outstandingFines = outstandingFines;
    }
}
