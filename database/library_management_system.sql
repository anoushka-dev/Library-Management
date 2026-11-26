CREATE DATABASE IF NOT EXISTS library_management_system;
USE library_management_system;

DROP TABLE IF EXISTS rental_notifications;
DROP TABLE IF EXISTS returns;
DROP TABLE IF EXISTS rentals;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(30) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    role ENUM('ADMIN') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
);

CREATE TABLE books (
    book_id INT NOT NULL AUTO_INCREMENT,
    isbn VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,
    publisher VARCHAR(150) DEFAULT NULL,
    publication_year INT DEFAULT NULL,
    description TEXT,
    total_quantity INT NOT NULL CHECK (total_quantity >= 0),
    available_quantity INT NOT NULL DEFAULT 0 CHECK (available_quantity >= 0),
    rented_quantity INT NOT NULL DEFAULT 0 CHECK (rented_quantity >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (book_id)
);

CREATE TABLE rentals (
    rental_id INT NOT NULL AUTO_INCREMENT,
    renter_name VARCHAR(120) NOT NULL,
    renter_email VARCHAR(150) NOT NULL,
    renter_phone VARCHAR(30) DEFAULT NULL,
    renter_address VARCHAR(255) DEFAULT NULL,
    book_id INT NOT NULL,
    rental_quantity INT NOT NULL CHECK (rental_quantity > 0),
    returned_quantity INT NOT NULL DEFAULT 0 CHECK (returned_quantity >= 0),
    rental_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP NOT NULL,
    actual_return_date TIMESTAMP NULL,
    fine_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status ENUM('ACTIVE','PARTIALLY_RETURNED','RETURNED','OVERDUE') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (rental_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE RESTRICT,
    CONSTRAINT chk_rental_returned_quantity CHECK (returned_quantity <= rental_quantity)
);

CREATE TABLE returns (
    return_id INT NOT NULL AUTO_INCREMENT,
    rental_id INT NOT NULL,
    returned_quantity INT NOT NULL CHECK (returned_quantity > 0),
    return_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (return_id),
    FOREIGN KEY (rental_id) REFERENCES rentals(rental_id) ON DELETE RESTRICT
);

CREATE TABLE rental_notifications (
    notification_id BIGINT NOT NULL AUTO_INCREMENT,
    rental_id INT NOT NULL,
    return_id INT DEFAULT NULL,
    notification_type VARCHAR(40) NOT NULL,
    event_key VARCHAR(160) NOT NULL UNIQUE,
    status ENUM('PENDING','SENDING','SENT','FAILED','SKIPPED') NOT NULL DEFAULT 'PENDING',
    attempt_count INT NOT NULL DEFAULT 0,
    last_error VARCHAR(1000) DEFAULT NULL,
    sent_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (notification_id),
    FOREIGN KEY (rental_id) REFERENCES rentals(rental_id) ON DELETE RESTRICT,
    FOREIGN KEY (return_id) REFERENCES returns(return_id) ON DELETE RESTRICT,
    INDEX idx_rental_notifications_delivery (status, updated_at)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_category ON books(category);
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_rentals_book ON rentals(book_id);
CREATE INDEX idx_rentals_status ON rentals(status);
CREATE INDEX idx_returns_rental_id ON returns(rental_id);

INSERT INTO users (full_name, email, password_hash, phone, address, role) VALUES
('System Administrator', 'admin@library.local', '$2a$10$QY2P1D0c0Q3QwJh5nGkqcuN6ldzmJ3QZZlQ4vNcM7P1CrEFlm3wV6', '9999999999', 'Head Office', 'ADMIN');

INSERT INTO books (isbn, title, author, category, publisher, publication_year, description, total_quantity, available_quantity, rented_quantity) VALUES
('9780132350884', 'Clean Code', 'Robert C. Martin', 'Programming', 'Prentice Hall', 2008, 'A handbook for writing clean, maintainable code.', 10, 10, 0),
('9780201616224', 'Design Patterns', 'Erich Gamma', 'Programming', 'Addison-Wesley', 1994, 'Classic patterns for software design.', 8, 8, 0),
('9780596009205', 'Head First Java', 'Kathy Sierra', 'Java', 'O'Reilly', 2005, 'A beginner-friendly Java learning guide.', 12, 12, 0);
