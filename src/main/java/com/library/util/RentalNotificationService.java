package com.library.util;

import com.library.dao.RentalDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class RentalNotificationService {
    private static final Logger LOGGER = Logger.getLogger(RentalNotificationService.class.getName());
    private static volatile boolean missingMailHostLogged;

    private RentalNotificationService() {
    }

    public static void enqueue(Connection connection, int rentalId, Integer returnId,
                               String notificationType, String eventKey) throws SQLException {
        String sql = "INSERT IGNORE INTO rental_notifications (rental_id, return_id, notification_type, event_key) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, rentalId);
            if (returnId == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, returnId);
            }
            statement.setString(3, notificationType);
            statement.setString(4, eventKey);
            statement.executeUpdate();
        }
    }

    public static void processNotifications() throws SQLException {
        queueScheduledNotifications();
        String host = setting("MAIL_SMTP_HOST");
        if (host == null || host.trim().isEmpty()) {
            if (!missingMailHostLogged) {
                LOGGER.warning("Rental email notifications are queued but MAIL_SMTP_HOST is not configured.");
                missingMailHostLogged = true;
            }
            return;
        }
        missingMailHostLogged = false;
        sendPendingNotifications(host);
    }

    private static void queueScheduledNotifications() throws SQLException {
        RentalDAO rentalDAO = new RentalDAO();
        rentalDAO.refreshOverdueStatuses();
        String selectSql = "SELECT rental_id, DATEDIFF(DATE(due_date), CURRENT_DATE) AS days_until_due, CURRENT_DATE AS notification_date "
                + "FROM rentals WHERE returned_quantity < rental_quantity";
        String insertSql = "INSERT IGNORE INTO rental_notifications (rental_id, notification_type, event_key) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement select = connection.prepareStatement(selectSql);
             ResultSet rentals = select.executeQuery();
             PreparedStatement insert = connection.prepareStatement(insertSql)) {
            while (rentals.next()) {
                int rentalId = rentals.getInt("rental_id");
                int daysUntilDue = rentals.getInt("days_until_due");
                String dateKey = rentals.getDate("notification_date").toString();
                String type = null;
                String eventKey = null;
                if (daysUntilDue == 2) {
                    type = "DUE_REMINDER";
                    eventKey = "RENTAL:" + rentalId + ":DUE_REMINDER";
                } else if (daysUntilDue == 0) {
                    type = "DUE_TODAY";
                    eventKey = "RENTAL:" + rentalId + ":DUE_TODAY";
                } else if (daysUntilDue == -1) {
                    type = "OVERDUE";
                    eventKey = "RENTAL:" + rentalId + ":OVERDUE";
                } else if (daysUntilDue < -1) {
                    type = "DAILY_OVERDUE";
                    eventKey = "RENTAL:" + rentalId + ":DAILY_OVERDUE:" +
                            dateKey;
                }
                if (type != null) {
                    insert.setInt(1, rentalId);
                    insert.setString(2, type);
                    insert.setString(3, eventKey);
                    insert.addBatch();
                }
            }
            insert.executeBatch();
        }
    }

    private static void sendPendingNotifications(String host) throws SQLException {
        String findSql = "SELECT notification_id FROM rental_notifications "
                + "WHERE status = 'PENDING' OR (status = 'FAILED' AND updated_at < CURRENT_TIMESTAMP - INTERVAL 1 MINUTE) "
                + "OR (status = 'SENDING' AND updated_at < CURRENT_TIMESTAMP - INTERVAL 10 MINUTE) "
                + "ORDER BY notification_id LIMIT 1";
        String claimSql = "UPDATE rental_notifications SET status = 'SENDING', attempt_count = attempt_count + 1, last_error = NULL "
                + "WHERE notification_id = ? AND (status = 'PENDING' OR (status = 'FAILED' AND updated_at < CURRENT_TIMESTAMP - INTERVAL 1 MINUTE) "
                + "OR (status = 'SENDING' AND updated_at < CURRENT_TIMESTAMP - INTERVAL 10 MINUTE))";
        while (true) {
            long notificationId;
            try (Connection connection = DatabaseUtil.getConnection();
                 PreparedStatement find = connection.prepareStatement(findSql);
                 ResultSet rs = find.executeQuery()) {
                if (!rs.next()) {
                    return;
                }
                notificationId = rs.getLong(1);
            }

            try (Connection connection = DatabaseUtil.getConnection();
                 PreparedStatement claim = connection.prepareStatement(claimSql)) {
                claim.setLong(1, notificationId);
                if (claim.executeUpdate() == 0) {
                    continue;
                }
            }

            try {
                if (sendNotification(notificationId, host)) {
                    markSent(notificationId);
                } else {
                    markSkipped(notificationId);
                }
            } catch (Exception ex) {
                markFailed(notificationId, ex);
                LOGGER.log(Level.SEVERE, "Could not deliver rental notification " + notificationId, ex);
            }
        }
    }

    private static boolean sendNotification(long notificationId, String host) throws Exception {
        String sql = "SELECT n.notification_type, u.full_name, u.email, b.title, r.rental_date, r.due_date, "
                + "r.returned_quantity, r.rental_quantity, COALESCE(ret.return_date, r.actual_return_date) AS return_date, "
                + "GREATEST(DATEDIFF(CURRENT_DATE, DATE(r.due_date)), 0) AS overdue_days, "
                + "CASE WHEN r.returned_quantity >= r.rental_quantity THEN r.fine_amount "
                + "ELSE GREATEST(DATEDIFF(CURRENT_DATE, DATE(r.due_date)), 0) * " + RentalDAO.FINE_PER_DAY.toPlainString() + " END AS current_fine, "
                + "ret.returned_quantity AS event_returned_quantity "
                + "FROM rental_notifications n JOIN rentals r ON n.rental_id = r.rental_id "
                + "JOIN users u ON r.user_id = u.user_id JOIN books b ON r.book_id = b.book_id "
                + "LEFT JOIN returns ret ON n.return_id = ret.return_id WHERE n.notification_id = ?";
        String type;
        String name;
        String email;
        String title;
        Date rentalDate;
        Date dueDate;
        Date returnDate;
        int overdueDays;
        int eventReturnedQuantity;
        int returnedQuantity;
        int rentalQuantity;
        java.math.BigDecimal fine;
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, notificationId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Rental notification details not found.");
                }
                type = rs.getString("notification_type");
                name = rs.getString("full_name");
                email = rs.getString("email");
                title = rs.getString("title");
                rentalDate = rs.getTimestamp("rental_date");
                dueDate = rs.getTimestamp("due_date");
                returnDate = rs.getTimestamp("return_date");
                overdueDays = Math.max(0, rs.getInt("overdue_days"));
                eventReturnedQuantity = rs.getInt("event_returned_quantity");
                returnedQuantity = rs.getInt("returned_quantity");
                rentalQuantity = rs.getInt("rental_quantity");
                fine = rs.getBigDecimal("current_fine");
            }
        }

        if (returnedQuantity >= rentalQuantity && ("DUE_REMINDER".equals(type)
                || "DUE_TODAY".equals(type) || "OVERDUE".equals(type) || "DAILY_OVERDUE".equals(type))) {
            return false;
        }

        String subject;
        String details = "Book: " + title + "\nRental date: " + format(rentalDate)
                + "\nExpected return date: " + format(dueDate) + "\nQuantity rented: " + rentalQuantity;
        if ("RENTAL_CONFIRMATION".equals(type)) {
            subject = "Rental confirmation: " + title;
            details += "\nYour rental has been recorded.";
        } else if ("DUE_REMINDER".equals(type)) {
            subject = "Return reminder: " + title;
            details += "\nThis book is due in 2 days.";
        } else if ("DUE_TODAY".equals(type)) {
            subject = "Book due today: " + title;
            details += "\nPlease return the book today.";
        } else if ("OVERDUE".equals(type) || "DAILY_OVERDUE".equals(type)) {
            subject = "Overdue book notice: " + title;
            details += "\nOverdue days: " + overdueDays + "\nCurrent fine: Rs. " + fine;
        } else if ("RETURN_CONFIRMATION".equals(type)) {
            subject = "Return confirmation: " + title;
            details += "\nReturned on: " + format(returnDate) + "\nQuantity returned in this transaction: "
                    + eventReturnedQuantity + "\nFine amount: Rs. " + fine;
        } else {
            throw new SQLException("Unsupported rental notification type: " + type);
        }
        sendEmail(host, email, subject, "Hello " + name + ",\n\n" + details + "\n\nLibrary Management System");
        return true;
    }

    private static void sendEmail(String host, String recipient, String subject, String body) throws Exception {
        final String username = setting("MAIL_SMTP_USERNAME");
        final String password = setting("MAIL_SMTP_PASSWORD");
        final String port = settingOrDefault("MAIL_SMTP_PORT", "587");
        final String from = settingOrDefault("MAIL_FROM",
                username == null || username.trim().isEmpty() ? "no-reply@library.local" : username);
        final boolean startTls = !"false".equalsIgnoreCase(settingOrDefault("MAIL_SMTP_STARTTLS", "true"));

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.starttls.enable", Boolean.toString(startTls));
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");
        final boolean authenticated = username != null && !username.trim().isEmpty();
        properties.put("mail.smtp.auth", Boolean.toString(authenticated));

        Session session;
        if (authenticated) {
            session = Session.getInstance(properties, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password == null ? "" : password);
                }
            });
        } else {
            session = Session.getInstance(properties);
        }
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject, "UTF-8");
        message.setText(body, "UTF-8");
        Transport.send(message);
    }

    private static void markSent(long notificationId) throws SQLException {
        String sql = "UPDATE rental_notifications SET status = 'SENT', sent_at = CURRENT_TIMESTAMP, last_error = NULL WHERE notification_id = ?";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, notificationId);
            statement.executeUpdate();
        }
    }

    private static void markSkipped(long notificationId) throws SQLException {
        String sql = "UPDATE rental_notifications SET status = 'SKIPPED', last_error = NULL WHERE notification_id = ?";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, notificationId);
            statement.executeUpdate();
        }
    }

    private static void markFailed(long notificationId, Exception failure) throws SQLException {
        String message = failure.getMessage();
        if (message == null) {
            message = failure.getClass().getSimpleName();
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000);
        }
        String sql = "UPDATE rental_notifications SET status = 'FAILED', last_error = ? WHERE notification_id = ?";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, message);
            statement.setLong(2, notificationId);
            statement.executeUpdate();
        }
    }

    private static String format(Date date) {
        return date == null ? "N/A" : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    private static String setting(String name) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? null : value;
    }

    private static String settingOrDefault(String name, String defaultValue) {
        String value = setting(name);
        return value == null ? defaultValue : value;
    }
}
