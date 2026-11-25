USE library_management_system;

ALTER TABLE rentals
    ADD COLUMN fine_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00
    AFTER actual_return_date;

UPDATE rentals
SET fine_amount = GREATEST(DATEDIFF(DATE(actual_return_date), DATE(due_date)), 0) * 10.00
WHERE returned_quantity >= rental_quantity
  AND actual_return_date IS NOT NULL;

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

UPDATE rentals
SET status = 'OVERDUE'
WHERE status IN ('ACTIVE', 'PARTIALLY_RETURNED')
  AND returned_quantity < rental_quantity
  AND DATE(due_date) < CURRENT_DATE;
