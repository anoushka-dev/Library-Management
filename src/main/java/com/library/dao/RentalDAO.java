package com.library.dao;

import com.library.model.Rental;
import com.library.util.DatabaseUtil;
import com.library.util.RentalNotificationService;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RentalDAO {
    public static final int DEFAULT_RENTAL_DAYS = 14;
    public static final BigDecimal FINE_PER_DAY = new BigDecimal("10.00");

    public int createRental(int userId, int bookId, int quantity, int rentalDays) throws SQLException {
        if (userId <= 0 || bookId <= 0 || quantity <= 0) {
            throw new SQLException("User, book, and rental quantity must be valid.");
        }
        int duration = rentalDays > 0 ? rentalDays : DEFAULT_RENTAL_DAYS;
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String lockSql = "SELECT available_quantity FROM books WHERE book_id = ? FOR UPDATE";
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
                    }
                }

                String updateBookSql = "UPDATE books SET available_quantity = available_quantity - ?, rented_quantity = rented_quantity + ?, updated_at = CURRENT_TIMESTAMP WHERE book_id = ?";
                try (PreparedStatement upd = conn.prepareStatement(updateBookSql)) {
                    upd.setInt(1, quantity);
                    upd.setInt(2, quantity);
                    upd.setInt(3, bookId);
                    upd.executeUpdate();
                }

                Calendar cal = Calendar.getInstance();
                Timestamp rentalDate = new Timestamp(cal.getTimeInMillis());
                cal.add(Calendar.DAY_OF_YEAR, duration);
                Timestamp dueDate = new Timestamp(cal.getTimeInMillis());

                String insertRentalSql = "INSERT INTO rentals (user_id, book_id, rental_quantity, returned_quantity, rental_date, due_date, status) VALUES (?, ?, ?, 0, ?, ?, 'ACTIVE')";
                int rentalId;
                try (PreparedStatement insert = conn.prepareStatement(insertRentalSql, Statement.RETURN_GENERATED_KEYS)) {
                    insert.setInt(1, userId);
                    insert.setInt(2, bookId);
                    insert.setInt(3, quantity);
                    insert.setTimestamp(4, rentalDate);
                    insert.setTimestamp(5, dueDate);
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Rental record was not created.");
                        }
                        rentalId = keys.getInt(1);
                    }
                }
                RentalNotificationService.enqueue(conn, rentalId, null, "RENTAL_CONFIRMATION",
                        "RENTAL:" + rentalId + ":CONFIRMATION");
                conn.commit();
                return rentalId;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Rental> listRentalsByUser(int userId) throws SQLException {
        refreshOverdueStatuses();
        String sql = rentalSelect() + " WHERE r.user_id = ? ORDER BY r.rental_date DESC";
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
        refreshOverdueStatuses();
        String sql = rentalSelect() + " ORDER BY r.rental_date DESC";
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

    public List<Rental> listActiveRentalsByUser(int userId) throws SQLException {
        refreshOverdueStatuses();
        String sql = rentalSelect() + " WHERE r.user_id = ? AND r.returned_quantity < r.rental_quantity ORDER BY r.due_date ASC";
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

    public List<Rental> listOverdueRentals() throws SQLException {
        refreshOverdueStatuses();
        String sql = rentalSelect() + " WHERE r.status = 'OVERDUE' AND r.returned_quantity < r.rental_quantity ORDER BY r.due_date ASC";
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

    public int countActiveRentals() throws SQLException {
        refreshOverdueStatuses();
        return count("SELECT COUNT(*) FROM rentals WHERE returned_quantity < rental_quantity");
    }

    public int countActiveRentalsByUser(int userId) throws SQLException {
        return countForUser("SELECT COUNT(*) FROM rentals WHERE user_id = ? AND returned_quantity < rental_quantity", userId);
    }

    public int countOverdueRentals() throws SQLException {
        refreshOverdueStatuses();
        return count("SELECT COUNT(*) FROM rentals WHERE status = 'OVERDUE' AND returned_quantity < rental_quantity");
    }

    public int countOverdueRentalsByUser(int userId) throws SQLException {
        refreshOverdueStatuses();
        return countForUser("SELECT COUNT(*) FROM rentals WHERE user_id = ? AND status = 'OVERDUE' AND returned_quantity < rental_quantity", userId);
    }

    public int countReturnedBooks() throws SQLException {
        return count("SELECT COALESCE(SUM(returned_quantity), 0) FROM rentals");
    }

    public BigDecimal totalOutstandingFines() throws SQLException {
        return outstandingFines(null);
    }

    public BigDecimal outstandingFinesByUser(int userId) throws SQLException {
        return outstandingFines(userId);
    }

    public Rental findById(int rentalId) throws SQLException {
        String sql = rentalSelect() + " WHERE r.rental_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rentalId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public void processReturn(int rentalId, int userId, int quantity) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String rentalSql = "SELECT *, DATE(due_date) < CURRENT_DATE AS is_overdue FROM rentals WHERE rental_id = ? AND user_id = ? FOR UPDATE";
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
                        boolean fullyReturned = currentReturned + quantity == rentalQuantity;
                        String newStatus = fullyReturned ? "RETURNED" :
                                (rs.getBoolean("is_overdue") ? "OVERDUE" : "PARTIALLY_RETURNED");
                        String updateRentalSql = "UPDATE rentals SET returned_quantity = returned_quantity + ?, actual_return_date = CASE WHEN ? = 1 THEN CURRENT_TIMESTAMP ELSE actual_return_date END, fine_amount = CASE WHEN ? = 1 THEN GREATEST(DATEDIFF(CURRENT_DATE, DATE(due_date)), 0) * "
                                + FINE_PER_DAY.toPlainString() + " ELSE fine_amount END, status = ?, updated_at = CURRENT_TIMESTAMP WHERE rental_id = ?";
                        try (PreparedStatement updRental = conn.prepareStatement(updateRentalSql)) {
                            updRental.setInt(1, quantity);
                            updRental.setInt(2, fullyReturned ? 1 : 0);
                            updRental.setInt(3, fullyReturned ? 1 : 0);
                            updRental.setString(4, newStatus);
                            updRental.setInt(5, rentalId);
                            updRental.executeUpdate();
                        }

                        String updateBookSql = "UPDATE books SET available_quantity = available_quantity + ?, rented_quantity = rented_quantity - ?, updated_at = CURRENT_TIMESTAMP WHERE book_id = ?";
                        try (PreparedStatement updBook = conn.prepareStatement(updateBookSql)) {
                            updBook.setInt(1, quantity);
                            updBook.setInt(2, quantity);
                            updBook.setInt(3, bookId);
                            updBook.executeUpdate();
                        }

                        int returnId;
                        String insertReturnSql = "INSERT INTO returns (rental_id, returned_quantity, remarks) VALUES (?, ?, ?)";
                        try (PreparedStatement insertReturn = conn.prepareStatement(insertReturnSql, Statement.RETURN_GENERATED_KEYS)) {
                            insertReturn.setInt(1, rentalId);
                            insertReturn.setInt(2, quantity);
                            insertReturn.setString(3, "Returned by member.");
                            insertReturn.executeUpdate();
                            try (ResultSet keys = insertReturn.getGeneratedKeys()) {
                                if (!keys.next()) {
                                    throw new SQLException("Return record was not created.");
                                }
                                returnId = keys.getInt(1);
                            }
                        }
                        RentalNotificationService.enqueue(conn, rentalId, returnId, "RETURN_CONFIRMATION",
                                "RETURN:" + returnId + ":CONFIRMATION");
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

    public void refreshOverdueStatuses() throws SQLException {
        String sql = "UPDATE rentals SET status = 'OVERDUE', updated_at = CURRENT_TIMESTAMP WHERE status IN ('ACTIVE','PARTIALLY_RETURNED') AND returned_quantity < rental_quantity AND DATE(due_date) < CURRENT_DATE";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private BigDecimal outstandingFines(Integer userId) throws SQLException {
        refreshOverdueStatuses();
        String sql = "SELECT COALESCE(SUM(CASE WHEN returned_quantity >= rental_quantity THEN fine_amount ELSE GREATEST(DATEDIFF(CURRENT_DATE, DATE(due_date)), 0) * "
                + FINE_PER_DAY.toPlainString() + " END), 0) FROM rentals"
                + (userId == null ? "" : " WHERE user_id = ?");
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId != null) {
                ps.setInt(1, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    private int count(String sql) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int countForUser(String sql, int userId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private String rentalSelect() {
        return "SELECT r.*, u.full_name, u.email AS user_email, b.title, b.isbn, "
                + "GREATEST(DATEDIFF(DATE(r.due_date), CURRENT_DATE), 0) AS days_remaining, "
                + "GREATEST(DATEDIFF(CURRENT_DATE, DATE(r.due_date)), 0) AS overdue_days, "
                + "CASE WHEN r.returned_quantity >= r.rental_quantity THEN r.fine_amount "
                + "ELSE GREATEST(DATEDIFF(CURRENT_DATE, DATE(r.due_date)), 0) * " + FINE_PER_DAY.toPlainString() + " END AS current_fine "
                + "FROM rentals r JOIN users u ON r.user_id = u.user_id JOIN books b ON r.book_id = b.book_id";
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
        rental.setUserEmail(rs.getString("user_email"));
        rental.setBookTitle(rs.getString("title"));
        rental.setIsbn(rs.getString("isbn"));
        rental.setDaysRemaining(rs.getInt("days_remaining"));
        rental.setOverdueDays(rs.getInt("overdue_days"));
        rental.setCurrentFine(rs.getBigDecimal("current_fine"));
        rental.setFineAmount(rs.getBigDecimal("fine_amount"));
        return rental;
    }
}
