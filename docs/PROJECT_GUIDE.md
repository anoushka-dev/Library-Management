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

The `AuthFilter` provides authentication, but it does not enforce role-level authorization on admin routes. Several admin-specific JSPs and actions are therefore reachable by any authenticated session. Admin actions should explicitly verify `currentUser.role == ADMIN` before being exposed in production.

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

- Administrators add books through `/books/add.do`.
- Administrators update books through `/books/update.do`.
- `/books/list.do` provides the admin inventory list.
- `/books/search.do` searches title, author, category, and ISBN and is used by members.
- `/books/delete.do` prevents deletion if an active, partially returned, or overdue rental exists.

When a book is created, `available_quantity` starts at `total_quantity` and `rented_quantity` starts at zero. Updating a book recalculates available quantity as `newTotal - rentedQuantity`.

### Rental

`RentBookAction` creates a rental for the current session user.

The rental DAO:

1. Opens a JDBC transaction.
2. Locks the book row with `SELECT ... FOR UPDATE`.
3. Checks whether enough copies are available.
4. Decreases `available_quantity` and increases `rented_quantity`.
5. Inserts a rental with the selected quantity and due date.
6. Commits both changes together, or rolls back on failure.

Rental days are supplied by the member form. The rental begins in `ACTIVE` status.

### Return

`ReturnBookAction` processes a return for a rental ID and quantity.

The return DAO:

1. Locks the rental row.
2. Calculates outstanding quantity.
3. Rejects zero quantities or quantities above the outstanding amount.
4. Updates the rental status to `RETURNED` or `PARTIALLY_RETURNED`.
5. Updates available and rented book quantities.
6. Inserts a row in `returns`.
7. Commits atomically or rolls back the operation.

The existing JSP flow does not expose a dedicated return form link in rental history. It depends on the `/returns/create.do` route and the `rentalId`/`quantity` form values.

### Overdue Rentals

An overdue result is selected from active or partially returned rentals whose due date is before the current database timestamp. The result set is labeled `OVERDUE` by the DAO without updating the stored status in the database. The enum can hold `OVERDUE`, but the regular overdue query does not run an update to persist that status.

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
| `/books/list.do` | `ViewBooksAction` | Admin book inventory |
| `/books/search.do` | `SearchBooksAction` | Available-book search |
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
- overdue rental row count.

The member dashboard uses available-copy total and the member's rental count. Its current overdue value uses the global overdue query rather than filtering overdue rentals for the current member.

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

- No automated test suite is currently included.
- Admin routes need explicit role checks in actions.
- The member dashboard's overdue count is global, not member-specific.
- Overdue status is derived by date in queries; stored `OVERDUE` status is not maintained by a scheduled update.
- Return remarks are fixed to `Returned by member.` and the remarks form is not persisted.
- Form validation is basic and does not cover every numeric or business constraint.
- Exception handling is mostly request/action-level; production needs consistent user-facing error handling and logging.
- Database credentials are environment-based, but no Docker service is currently provided for the Java application itself.
- Passwords are hashed correctly with BCrypt, but HTTPS and stronger session/security configuration should be used outside local development.
