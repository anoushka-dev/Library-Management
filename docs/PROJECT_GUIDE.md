# Project Understanding Guide

## 1. System Overview

The Library Management System is a Java 8, Apache Struts 1.2, JSP/JSTL, JDBC, and MySQL web application packaged as a WAR. It supports two roles:

- **ADMIN**: manage the book catalog, view all rentals, and monitor overdue rentals.
- **MEMBER**: register, search available books, rent copies, return copies, and view rental history.

The application uses a traditional server-rendered MVC architecture. Struts Action objects coordinate requests, DAO classes execute SQL, and JSP files render the result.

## 2. Technology Stack

| Concern | Technology |
|---|---|
| Language/runtime | Java 8 |
| Web framework | Apache Struts 1.2.9 |
| Views | JSP 2.3, JSTL 1.2, Struts HTML tags |
| Database | MySQL 8 |
| Data access | JDBC and MySQL Connector/J 8.0.33 |
| Password security | BCrypt through jBCrypt 0.4 |
| Build | Maven 3, WAR packaging |
| Application server | Apache Tomcat 8/9 |
| Local database | Docker Compose with MySQL 8 |

## 3. Application Structure

```text
src/main/java/com/library/
  action/       Struts request handlers
  dao/          JDBC data-access operations
  filter/       Session authentication filter
  form/         Request forms and validation
  model/        Domain objects and dashboard statistics
  util/         Database and password helpers

src/main/webapp/
  index.jsp     Public landing page
  css/          Application styles
  js/           Optional browser JavaScript
  jsp/admin/    Administrator screens
  jsp/auth/     Login and registration screens
  jsp/member/   Member screens
  WEB-INF/
    web.xml     Servlet/filter configuration
    struts-config.xml

database/
  library_management_system.sql
```

## 4. Request Lifecycle

1. `ActionServlet` receives every `*.do` request.
2. `web.xml` maps `*.do` to `org.apache.struts.action.ActionServlet`.
3. `struts-config.xml` selects a form bean and action class and declares success/error forwards.
4. `AuthFilter` allows public paths but requires `currentUser` in the HTTP session for all other paths.
5. The action validates or reads the form and calls one or more DAO methods.
6. The action stores result data or messages in the request.
7. Struts forwards or redirects to the configured JSP or action.

The Struts form beans are request-scoped and perform basic field validation through `ActionErrors`. The DAO layer uses prepared statements and direct JDBC connections.

## 5. Authentication and Authorization

`LoginAction` uses `UserDAO.login()`, which looks up a user by email and verifies the stored BCrypt hash with `PasswordUtil`. Successful login stores the full `User` model in the HTTP session as `currentUser`.

- Session timeout: 30 minutes.
- Logout invalidates the session.
- `AbstractAction.getCurrentUser()` retrieves the session user.
- `LoginAction` redirects `ADMIN` users to `/admin/dashboard.do` and members to `/member/dashboard.do`.
- Registration always creates a `MEMBER` account.

The `AuthFilter` requires an authenticated `User` session and restricts admin JSPs, admin dashboard routes, inventory mutations, and overdue reports to `ADMIN`. Admin dashboard and overdue actions also verify the role. Member rental queries are scoped to the signed-in user's ID.

## 6. Core Workflows

### Registration

- `GET /jsp/auth/register.jsp` displays the form.
- `POST /register.do` is validated and handled by `RegisterAction`.
- `UserDAO.register()` inserts a new user with `MEMBER` role and a BCrypt password hash.
- Duplicate email addresses are rejected by the database unique constraint and by the DAO lookup.

### Login and Logout

- `POST /login.do` verifies email and password.
- The session is populated with `currentUser`.
- `GET /logout.do` invalidates the session and redirects to `/index.jsp`.

### Book Management

- Administrators use `/books/list.do` to see the complete inventory, add books, and delete eligible books.
- Administrators add books through `/books/add.do` and update them through `/books/update.do`.
- Members use `/books/list.do` to browse available books. The same page provides search, add, and rent controls for the current UI.
- `/books/search.do` searches title, author, category, and ISBN. An empty or blank search shows all available books.
- `/books/delete.do` prevents deletion if active, partially returned, or overdue rental quantities exist.

The `ViewBooksAction` reads `currentUser.role` to select the page returned by `/books/list.do`: `ADMIN` receives the inventory JSP, while `MEMBER` receives the available-books JSP. This behavior keeps each navigation role focused without changing the common Books route.

When a book is created, `available_quantity` starts at `total_quantity` and `rented_quantity` starts at zero. Update validation requires the title, author, ISBN, and total quantity. The application preserves existing rented quantity while recalculating the available quantity from the updated total and rented quantity.

### Rental

`RentBookAction` creates a rental for the current session user. Each available-book row displays two number inputs:

- **Quantity**: number of copies to rent. Its browser minimum is 1 and its maximum is the book's current `available_quantity`.
- **Rental Days**: loan duration in calendar days, defaulting to 14. The DAO adds this value to the rental timestamp to calculate `due_date`.

The rental DAO:

1. Opens a JDBC transaction.
2. Locks the book row with `SELECT ... FOR UPDATE`.
3. Rejects missing books and requests larger than the available stock.
4. Decreases `available_quantity` and increases `rented_quantity` by the requested quantity.
5. Inserts a rental for the signed-in user with `rental_quantity`, `returned_quantity = 0`, status `ACTIVE`, and a 14-day default due date.
6. Queues a unique rental-confirmation email in the same database transaction.
7. Commits both changes together, or rolls back on failure.

The rental form validates that book ID and quantity are positive. A missing or zero duration uses the 14-day default. The DAO also enforces stock availability at transaction time, preventing overselling when stock changes concurrently.

### Rental History and Returns

The Rental History page lists the signed-in member's records, or all user records for administrators, including user name and email, rental and due dates, countdown, overdue days, current/final fine, and status. A return action appears for members when copies remain outstanding.

The return form is shown for `ACTIVE`, `PARTIALLY_RETURNED`, and `OVERDUE` rentals. It provides:

- a hidden `rentalId`;
- a quantity input whose minimum is 1 and maximum is `rental_quantity - returned_quantity`;
- a Return button.

After one or more copies are returned, the page automatically recalculates and limits the next quantity to the remaining outstanding copies. A complete return stores the final fine, calculated at ₹10 per overdue calendar day; open rentals display a backend-calculated daily fine.

`ReturnBookAction` processes `POST /returns/create.do` for the current session user:

1. Locks the rental row only when `rental_id` and `user_id` match the signed-in member.
2. Calculates outstanding quantity as `rental_quantity - returned_quantity`.
3. Rejects quantities at or below zero and quantities above the outstanding amount.
4. Updates the returned quantity and sets `PARTIALLY_RETURNED` or `RETURNED`.
5. Increases `available_quantity` and decreases `rented_quantity` by the returned quantity.
6. Inserts an immutable return event in `returns` and queues a unique return-confirmation email.
7. Commits atomically or rolls back the entire operation.

Ownership matching prevents a member from returning another member's rental by modifying the hidden rental ID. When all copies are returned, the page removes the active return form because the rental status becomes `RETURNED`.

### Overdue Rentals and Notifications

Rentals become `OVERDUE` when their due calendar date has passed and outstanding copies remain. Backend queries calculate remaining days, overdue days, and fines; active fines update daily at ₹10 per overdue day, while `fine_amount` stores the final amount when all copies are returned. The member dashboard shows only that member's active rentals and fines. Admin rental pages include all members' names and email addresses, and admin-only overdue pages show current overdue days and fines.

A scheduled servlet listener queues email events for rental confirmation, two-day and due-date reminders, the first overdue day, subsequent daily overdue reminders, and every return event. The `rental_notifications.event_key` unique constraint makes event enqueueing idempotent. SMTP delivery uses an outbox status and retries failed sends.

## 7. Database Model

### `users`

Stores credentials and profile details. Roles are `ADMIN` or `MEMBER`. Email is unique.

### `books`

Stores catalog metadata and inventory counters:

- `total_quantity`: total owned copies
- `available_quantity`: copies ready to rent
- `rented_quantity`: copies assigned to active rentals

### `rentals`

Connects a user to a book and tracks rented quantity, returned quantity, due date, status, and timestamps. `returned_quantity <= rental_quantity` is enforced by a database constraint.

Status values:

- `ACTIVE`
- `PARTIALLY_RETURNED`
- `RETURNED`
- `OVERDUE`

`fine_amount` stores the final fine once all copies are returned. The `rental_notifications` outbox stores notification type, unique event key, delivery status, retry count, and send errors. Apply `database/migrations/001_rental_fines_notifications.sql` once to an existing installation; the main schema includes the new fields and table for fresh installs.

### `returns`

Stores each partial or full return event, including quantity and remarks.

Foreign keys use `ON DELETE RESTRICT`, so users or books referenced by rental rows cannot be deleted casually.

## 8. Struts Route Map

| Route | Action | Main result |
|---|---|---|
| `/login.do` | `LoginAction` | Role-based dashboard redirect |
| `/register.do` | `RegisterAction` | Member registration |
| `/logout.do` | `LogoutAction` | Session invalidation |
| `/admin/dashboard.do` | `AdminDashboardAction` | Admin statistics |
| `/member/dashboard.do` | `MemberDashboardAction` | Member statistics |
| `/books/add.do` | `AddBookAction` | Create book |
| `/books/update.do` | `UpdateBookAction` | Update book |
| `/books/delete.do` | `DeleteBookAction` | Delete book when eligible |
| `/books/list.do` | `ViewBooksAction` | Admin inventory or member available-book browse |
| `/books/search.do` | `SearchBooksAction` | Available-book search by title, author, category, or ISBN |
| `/rentals/create.do` | `RentBookAction` | Create rental transaction |
| `/rentals/list.do` | `ViewRentalsAction` | Member history or all rentals for admin |
| `/returns/create.do` | `ReturnBookAction` | Process partial/full return |
| `/rentals/overdue.do` | `ViewOverdueRentalsAction` | Overdue report |

## 9. Dashboard Statistics

The admin dashboard combines:

- book row count;
- sum of available copies;
- sum of rented copies;
- member count;
- active rental row count;
- overdue rental row count;
- total copies returned and outstanding fines.

The member account page shows the signed-in user's name, email, phone, and address, plus available-copy total, active and overdue rental counts, outstanding fines, and current rentals with rental date, due date, countdown, and fine. Rental history includes prior returns and final fines. The admin dashboard includes an overdue-rentals table.

## 10. Configuration

`DatabaseUtil` first reads these environment variables:

| Variable | Purpose |
|---|---|
| `DB_URL` | Full JDBC URL |
| `DB_USER` | Database user |
| `DB_PASSWORD` | Database password; required |

If no `DB_URL` is supplied, the default is:

```text
jdbc:mysql://localhost:3306/library_management_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

Rental emails require SMTP environment variables on Tomcat: `MAIL_SMTP_HOST` and optionally `MAIL_SMTP_PORT` (default `587`), `MAIL_SMTP_USERNAME`, `MAIL_SMTP_PASSWORD`, `MAIL_FROM`, and `MAIL_SMTP_STARTTLS` (default `true`). Without a host, email events remain queued until SMTP is configured.

The default user is `root`. `DB_PASSWORD` has no default and must be configured.

Docker Compose supplies MySQL root password and database name variables:

- `MYSQL_ROOT_PASSWORD`: required by `docker-compose.yml`;
- `MYSQL_DATABASE`: fixed to `library_management_system`.

## 11. Build and Runtime

Maven compiles Java 8-compatible source and packages `target/library-management.war`.

The deployed application is accessible below Tomcat's context path, commonly:

```text
http://localhost:8080/library-management/
```

Tomcat must have access to the MySQL host and the database environment variables must be available to the Tomcat process.

## 12. Seed Data

The SQL script creates:

- administrator: `admin@library.local`;
- member: `member@library.local`;
- three sample books.

Both seeded users contain BCrypt password hashes, but the plain-text passwords are not defined in the repository. Reset or set passwords through a controlled registration/setup process before using seed accounts in a deployed environment.

## 13. Implementation Notes and Limitations

- No automated test suite is currently included. Build and HTTP integration checks should be added for authentication, role routing, search, rental, return, and inventory updates.
- The authentication filter restricts admin pages and mutation/report routes to administrator sessions; admin dashboard and overdue-report actions also check the role. Member rental queries are scoped to the signed-in user.
- Return remarks are fixed to `Returned by member.` and a remarks form is not exposed in the UI.
- Form validation is basic and does not cover every numeric or business constraint.
- Exception handling is mostly request/action-level; production needs consistent user-facing error handling and logging.
- Database credentials are environment-based, but no Docker service is currently provided for the Java application itself.
- Passwords are hashed correctly with BCrypt, but HTTPS and stronger session/security configuration should be used outside local development.
