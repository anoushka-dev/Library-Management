package com.library.dao;

import com.library.model.Book;
import com.library.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    public boolean createBook(Book book) throws SQLException {
        String sql = "INSERT INTO books (isbn, title, author, category, publisher, publication_year, description, total_quantity, available_quantity, rented_quantity) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getCategory());
            ps.setString(5, book.getPublisher());
            if (book.getPublicationYear() != null) {
                ps.setInt(6, book.getPublicationYear());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.setString(7, book.getDescription());
            ps.setInt(8, book.getTotalQuantity());
            ps.setInt(9, book.getAvailableQuantity() > 0 ? book.getAvailableQuantity() : book.getTotalQuantity());
            ps.setInt(10, 0);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET isbn = ?, title = ?, author = ?, category = ?, publisher = ?, publication_year = ?, description = ?, total_quantity = ?, available_quantity = ?, rented_quantity = ?, updated_at = CURRENT_TIMESTAMP WHERE book_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getCategory());
            ps.setString(5, book.getPublisher());
            if (book.getPublicationYear() != null) {
                ps.setInt(6, book.getPublicationYear());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.setString(7, book.getDescription());
            ps.setInt(8, book.getTotalQuantity());
            ps.setInt(9, book.getAvailableQuantity());
            ps.setInt(10, book.getRentedQuantity());
            ps.setInt(11, book.getBookId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        }
    }

    public Book findById(int bookId) throws SQLException {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Book findByIsbn(String isbn) throws SQLException {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Book> listBooks(String keyword) throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE (? IS NULL OR ? = '') OR (title LIKE ? OR author LIKE ? OR category LIKE ? OR isbn LIKE ?) ORDER BY title";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String val = keyword == null ? "" : keyword.trim();
            ps.setString(1, val);
            ps.setString(2, val);
            ps.setString(3, '%' + val + '%');
            ps.setString(4, '%' + val + '%');
            ps.setString(5, '%' + val + '%');
            ps.setString(6, '%' + val + '%');
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
        }
        return books;
    }

    public List<Book> listAvailableBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE available_quantity > 0 ORDER BY title";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                books.add(mapRow(rs));
            }
        }
        return books;
    }

    public int countBooks() throws SQLException {
        String sql = "SELECT COUNT(*) FROM books";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countAvailableBooks() throws SQLException {
        String sql = "SELECT COALESCE(SUM(available_quantity), 0) FROM books";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countRentedBooks() throws SQLException {
        String sql = "SELECT COALESCE(SUM(rented_quantity), 0) FROM books";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean canDeleteBook(int bookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rentals WHERE book_id = ? AND status IN ('ACTIVE','PARTIALLY_RETURNED','OVERDUE')";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return true;
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookId(rs.getInt("book_id"));
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setCategory(rs.getString("category"));
        book.setPublisher(rs.getString("publisher"));
        book.setPublicationYear(rs.getObject("publication_year") == null ? null : rs.getInt("publication_year"));
        book.setDescription(rs.getString("description"));
        book.setTotalQuantity(rs.getInt("total_quantity"));
        book.setAvailableQuantity(rs.getInt("available_quantity"));
        book.setRentedQuantity(rs.getInt("rented_quantity"));
        book.setCreatedAt(rs.getTimestamp("created_at"));
        book.setUpdatedAt(rs.getTimestamp("updated_at"));
        return book;
    }
}
