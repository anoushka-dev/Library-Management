package com.library.dao;

import com.library.model.Rental;
import com.library.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RentalDAO {
    public void createRental(int userId, int bookId, int quantity, int rentalDays) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String lockSql = "SELECT available_quantity, rented_quantity FROM books WHERE book_id = ? FOR UPDATE";
                try (PreparedStatement lock = conn.prepareStatement(lockSql)) {
                    lock.setInt(1, bookId);
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Book not found.");
                        }
                        int available = rs.getInt("available_quantity");
                        if (available < quantity) {
                            throw new SQLException("Insufficient stock available. Requested: " + quantity + ", available: " + available);
                        }

                        String updateBookSql = "UPDATE books SET available_quantity = available_quantity - ?, rented_quantity = rented_quantity + ?, updated_at = CURRENT_TIMESTAMP WHERE book_id = ?";
                        try (PreparedStatement upd = conn.prepareStatement(updateBookSql)) {
                            upd.setInt(1, quantity);
                            upd.setInt(2, quantity);
                            upd.setInt(3, bookId);
                            upd.executeUpdate();
                        }
                    }
                }

                Calendar cal = Calendar.getInstance();
                Timestamp rentalDate = new Timestamp(cal.getTimeInMillis());
                cal.add(Calendar.DAY_OF_YEAR, rentalDays);
                Timestamp dueDate = new Timestamp(cal.getTimeInMillis());

                String insertRentalSql = "INSERT INTO rentals (user_id, book_id, rental_quantity, returned_quantity, rental_date, due_date, status) VALUES (?, ?, ?, 0, ?, ?, 'ACTIVE')";
                try (PreparedStatement insert = conn.prepareStatement(insertRentalSql)) {
                    insert.setInt(1, userId);
                    insert.setInt(2, bookId);
                    insert.setInt(3, quantity);
                    insert.setTimestamp(4, rentalDate);
                    insert.setTimestamp(5, dueDate);
                    insert.executeUpdate();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Rental> listRentalsByUser(int userId) throws SQLException {
        String sql = "SELECT r.*, u.full_name, b.title, b.isbn FROM rentals r JOIN users u ON r.user_id = u.user_id JOIN books b ON r.book_id = b.book_id WHERE r.user_id = ? ORDER BY r.rental_date DESC";
        List<Rental> rentals = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rentals.add(mapRow(rs));
                }
            }
        }
        return rentals;
    }

    public List<Rental> listAllRentals() throws SQLException {
        String sql = "SELECT r.*, u.full_name, b.title, b.isbn FROM rentals r JOIN users u ON r.user_id = u.user_id JOIN books b ON r.book_id = b.book_id ORDER BY r.rental_date DESC";
        List<Rental> rentals = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rentals.add(mapRow(rs));
            }
        }
        return rentals;
    }

    public List<Rental> listOverdueRentals() throws SQLException {
        String sql = "SELECT r.*, u.full_name, b.title, b.isbn FROM rentals r JOIN users u ON r.user_id = u.user_id JOIN books b ON r.book_id = b.book_id WHERE r.status IN ('ACTIVE','PARTIALLY_RETURNED') AND r.due_date < CURRENT_TIMESTAMP AND r.returned_quantity < r.rental_quantity";
        List<Rental> rentals = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Rental rental = mapRow(rs);
                rental.setStatus("OVERDUE");
                rentals.add(rental);
            }
        }
        return rentals;
    }

    public int countActiveRentals() throws SQLException {
        String sql = "SELECT COUNT(*) FROM rentals WHERE status IN ('ACTIVE', 'PARTIALLY_RETURNED', 'OVERDUE')";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countActiveRentalsByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rentals WHERE user_id = ? AND returned_quantity < rental_quantity";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int countOverdueRentals() throws SQLException {
        String sql = "SELECT COUNT(*) FROM rentals WHERE status IN ('ACTIVE','PARTIALLY_RETURNED') AND due_date < CURRENT_TIMESTAMP AND returned_quantity < rental_quantity";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countOverdueRentalsByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rentals WHERE user_id = ? AND due_date < CURRENT_TIMESTAMP AND returned_quantity < rental_quantity";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public Rental findById(int rentalId) throws SQLException {
        String sql = "SELECT r.*, u.full_name, b.title, b.isbn FROM rentals r JOIN users u ON r.user_id = u.user_id JOIN books b ON r.book_id = b.book_id WHERE r.rental_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rentalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public void processReturn(int rentalId, int userId, int quantity) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String rentalSql = "SELECT * FROM rentals WHERE rental_id = ? AND user_id = ? FOR UPDATE";
                try (PreparedStatement rentalStmt = conn.prepareStatement(rentalSql)) {
                    rentalStmt.setInt(1, rentalId);
                    rentalStmt.setInt(2, userId);
                    try (ResultSet rs = rentalStmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Rental not found.");
                        }

                        int currentReturned = rs.getInt("returned_quantity");
                        int rentalQuantity = rs.getInt("rental_quantity");
                        int outstanding = rentalQuantity - currentReturned;
                        if (quantity <= 0) {
                            throw new SQLException("Return quantity must be greater than zero.");
                        }
                        if (quantity > outstanding) {
                            throw new SQLException("Return quantity exceeds outstanding inventory.");
                        }

                        int bookId = rs.getInt("book_id");
                        String updateRentalSql = "UPDATE rentals SET returned_quantity = returned_quantity + ?, actual_return_date = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE rental_id = ?";
                        String newStatus = (currentReturned + quantity) >= rentalQuantity ? "RETURNED" : "PARTIALLY_RETURNED";

                        try (PreparedStatement updRental = conn.prepareStatement(updateRentalSql)) {
                            updRental.setInt(1, quantity);
                            updRental.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                            updRental.setString(3, newStatus);
                            updRental.setInt(4, rentalId);
                            updRental.executeUpdate();
                        }

                        String updateBookSql = "UPDATE books SET available_quantity = available_quantity + ?, rented_quantity = rented_quantity - ?, updated_at = CURRENT_TIMESTAMP WHERE book_id = ?";
                        try (PreparedStatement updBook = conn.prepareStatement(updateBookSql)) {
                            updBook.setInt(1, quantity);
                            updBook.setInt(2, quantity);
                            updBook.setInt(3, bookId);
                            updBook.executeUpdate();
                        }

                        String insertReturnSql = "INSERT INTO returns (rental_id, returned_quantity, remarks) VALUES (?, ?, ?)";
                        try (PreparedStatement insertReturn = conn.prepareStatement(insertReturnSql)) {
                            insertReturn.setInt(1, rentalId);
                            insertReturn.setInt(2, quantity);
                            insertReturn.setString(3, "Returned by member.");
                            insertReturn.executeUpdate();
                        }
                    }
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Rental mapRow(ResultSet rs) throws SQLException {
        Rental rental = new Rental();
        rental.setRentalId(rs.getInt("rental_id"));
        rental.setUserId(rs.getInt("user_id"));
        rental.setBookId(rs.getInt("book_id"));
        rental.setRentalQuantity(rs.getInt("rental_quantity"));
        rental.setReturnedQuantity(rs.getInt("returned_quantity"));
        rental.setRentalDate(rs.getTimestamp("rental_date"));
        rental.setDueDate(rs.getTimestamp("due_date"));
        rental.setActualReturnDate(rs.getTimestamp("actual_return_date"));
        rental.setStatus(rs.getString("status"));
        rental.setCreatedAt(rs.getTimestamp("created_at"));
        rental.setUpdatedAt(rs.getTimestamp("updated_at"));
        rental.setUserName(rs.getString("full_name"));
        rental.setBookTitle(rs.getString("title"));
        rental.setIsbn(rs.getString("isbn"));
        return rental;
    }
}
