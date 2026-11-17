package com.library.model;

public class DashboardStats {
    private int totalBooks;
    private int availableBooks;
    private int rentedBooks;
    private int totalMembers;
    private int activeRentals;
    private int overdueRentals;

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

    public int getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(int totalMembers) {
        this.totalMembers = totalMembers;
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
}
