USE library_management_system;

ALTER TABLE rentals
    ADD COLUMN renter_name VARCHAR(120) DEFAULT NULL AFTER rental_id,
    ADD COLUMN renter_email VARCHAR(150) DEFAULT NULL AFTER renter_name,
    ADD COLUMN renter_phone VARCHAR(30) DEFAULT NULL AFTER renter_email,
    ADD COLUMN renter_address VARCHAR(255) DEFAULT NULL AFTER renter_phone;

UPDATE rentals r
JOIN users u ON r.user_id = u.user_id
SET r.renter_name = u.full_name,
    r.renter_email = u.email,
    r.renter_phone = u.phone,
    r.renter_address = u.address;

ALTER TABLE rentals
    MODIFY COLUMN renter_name VARCHAR(120) NOT NULL,
    MODIFY COLUMN renter_email VARCHAR(150) NOT NULL,
    DROP FOREIGN KEY rentals_ibfk_1,
    DROP INDEX idx_rentals_user,
    DROP COLUMN user_id;

DELETE FROM users WHERE role = 'MEMBER';

ALTER TABLE users MODIFY COLUMN role ENUM('ADMIN') NOT NULL;
