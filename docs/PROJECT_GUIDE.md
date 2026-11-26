# Project Understanding Guide

## 1. System Overview

The Library Management System is a Java 8, Apache Struts 1.2, JSP/JSTL, JDBC, and MySQL web application. It has a single authenticated role: `ADMIN`. The administrator maintains the book catalog, records rentals for customers, processes returns, and reviews rental and overdue history. Customers do not create accounts or sign in; their contact information is stored on each rental.

## 2. Application Structure

```text
src/main/java/com/library/
  action/       Struts request handlers
  dao/          JDBC operations
  filter/       Admin-only session authentication
  form/         Form fields and validation
  model/        Book, rental, admin, and dashboard data
  util/         Database, password, and notification helpers

src/main/webapp/
  index.jsp     Public landing page
  jsp/admin/    Admin inventory, rental, and report screens
  jsp/auth/     Admin login
  WEB-INF/      Servlet and Struts configuration

database/
  library_management_system.sql
  migrations/
```

## 3. Authentication and Authorization

`LoginAction` authenticates against the admin-only `users` table using BCrypt. Successful login stores the administrator in the session and opens `/admin/dashboard.do`. Registration is not available. `AuthFilter` allows unauthenticated access only to the landing page, login page/action, and static assets; every application route and JSP otherwise requires a session with role `ADMIN`.

## 4. Book Management

Admins use `/books/list.do` to review and search inventory, add books, and delete books that have no active rental quantity. The inventory includes a rental action for books with available copies. Book stock changes are protected by database transactions and row locking when a rental is created.

## 5. Rental Workflow

The admin selects **Rent** on an inventory row or uses **Record a Rental** to open `/rentals/new.do`. The form records:

- book and copy quantity;
- renter name and email (required);
- renter phone and address (optional);
- rental duration in days (defaults to 14).

`RentBookAction` validates the request. `RentalDAO.createRental()` locks the book row, verifies stock, updates inventory, creates a rental record containing the renter contact snapshot, and queues a confirmation email in one transaction. A customer account is not created.

## 6. Rental History and Returns

`/rentals/list.do` displays all rental records to the admin, including renter contact details, book, quantities, rental/due dates, remaining or overdue days, current/final fines, and status. Admins can process partial or full returns directly from this page. Return transactions lock the rental, enforce the outstanding quantity, update book stock and rental state, record a return event, and queue a notification.

Overdue statuses are refreshed from due dates. Open rentals accrue a current fine of ₹10 per overdue day; the final fine is stored when all copies are returned. The admin dashboard and `/rentals/overdue.do` provide summary and overdue views.

## 7. Database Model

- `users`: administrator credentials and profile; the only valid role is `ADMIN`.
- `books`: catalog metadata plus total, available, and rented copy counts.
- `rentals`: renter name/email/phone/address snapshot, book, quantities, dates, status, and fine.
- `returns`: each partial or full return event.
- `rental_notifications`: idempotent email outbox and delivery status.

The fresh-install schema is in `database/library_management_system.sql`. For an existing installation, apply `001_rental_fines_notifications.sql` if it has not already been applied, then apply `002_admin_only_rentals.sql`. The second migration copies each prior member's contact details into their rental records before removing the rental-to-user relationship and member accounts. Back up the database before applying migrations.

## 8. Routes

| Route | Purpose |
|---|---|
| `/login.do` | Authenticate the administrator |
| `/logout.do` | End the session |
| `/admin/dashboard.do` | Admin statistics and overdue summary |
| `/books/list.do` | Searchable inventory |
| `/books/add.do` | Add a book |
| `/books/update.do` | Update a book |
| `/books/delete.do` | Delete a book when eligible |
| `/rentals/new.do` | Open the renter/rental form |
| `/rentals/create.do` | Record a rental |
| `/rentals/list.do` | Admin rental history |
| `/returns/create.do` | Record partial or full return |
| `/rentals/overdue.do` | Overdue report |

Routes and forwards are declared in `src/main/webapp/WEB-INF/struts-config.xml`.

## 9. Configuration and Operations

`DatabaseUtil` reads `DB_URL`, `DB_USER`, and required `DB_PASSWORD` from the process environment. The default JDBC URL targets local MySQL. Rental email notifications use `MAIL_SMTP_HOST` and optional SMTP configuration variables; without a host, notifications remain queued.

Maven packages `target/library-management.war`. Tomcat must have access to MySQL and the required environment variables.

## 10. Security and Limitations

- Change the seeded administrator password before deployment; no public account registration exists.
- Run behind HTTPS in production and configure secure Tomcat session settings.
- Form validation covers the rental fields and business rules are rechecked transactionally in the DAO.
- No automated test suite is currently included.
