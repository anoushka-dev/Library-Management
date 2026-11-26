# Library Management System

A server-rendered Java 8 library application where an administrator manages books and records rentals for customers. It uses Apache Struts 1.2, JSP/JSTL, JDBC, MySQL 8, Maven, and Apache Tomcat.

## Features

- BCrypt password authentication and session-based login
- Single administrator authentication; renters do not need app accounts
- Book catalog with searchable inventory
- Add, update, and delete book operations
- Admin-entered renter name and contact details on each rental
- Rental history with renter details, due-date countdowns, and ₹10/day overdue fines
- Rental confirmation, due-date, overdue, and return email notifications
- Partial and full return processing
- Active, partially returned, returned, and overdue rental states
- Admin dashboard and overdue rental report
- Docker Compose MySQL setup and seeded data

## Architecture at a Glance

```text
Browser/JSP
    -> Struts ActionServlet (/*.do)
    -> Action + ActionForm
    -> AuthFilter
    -> DAO (JDBC)
    -> MySQL
    -> JSP forward
```

Important source areas:

- `src/main/java/com/library/action/`: Struts request handlers
- `src/main/java/com/library/dao/`: JDBC operations
- `src/main/java/com/library/form/`: form fields and validation
- `src/main/java/com/library/model/`: domain models
- `src/main/java/com/library/filter/AuthFilter.java`: session authentication
- `src/main/java/com/library/util/DatabaseUtil.java`: JDBC and environment configuration
- `src/main/webapp/WEB-INF/struts-config.xml`: action and forward mapping
- `src/main/webapp/WEB-INF/web.xml`: servlet/filter registration
- `database/library_management_system.sql`: schema and seed data

For a detailed package, route, workflow, database, and implementation analysis, see [Project Understanding Guide](docs/PROJECT_GUIDE.md).

## Prerequisites

- JDK 8 or newer
- Maven 3+
- Apache Tomcat 8 or 9
- MySQL 8 or Docker with Docker Compose

## Database Setup

### Option 1: Docker Compose

Create a `.env` file in the project root:

```dotenv
MYSQL_ROOT_PASSWORD=change-me
```

Start MySQL:

```bash
docker compose up -d db
```

Docker initializes the database and runs `database/library_management_system.sql` on the first container volume creation. Verify readiness:

```bash
docker compose ps
```

The database is exposed on `localhost:3306`.

### Option 2: Local MySQL

```bash
mysql -u root -p < database/library_management_system.sql
```

The script creates `library_management_system`, the tables, indexes, constraints, and seed rows.

## Configure the Application

`DatabaseUtil` requires `DB_PASSWORD`. It also accepts optional `DB_URL` and `DB_USER` values.

```bash
export DB_PASSWORD='change-me'
export DB_USER='root'
export DB_URL='jdbc:mysql://localhost:3306/library_management_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
```

Without `DB_URL`, the application uses `localhost:3306/library_management_system`. Without `DB_USER`, it uses `root`. The database password is required and has no default.

Configure SMTP on the Tomcat process to deliver rental notifications:

```bash
export MAIL_SMTP_HOST='smtp.example.com'
export MAIL_SMTP_PORT='587'
export MAIL_SMTP_USERNAME='library@example.com'
export MAIL_SMTP_PASSWORD='change-me'
export MAIL_FROM='library@example.com'
export MAIL_SMTP_STARTTLS='true'
```

SMTP username/password are optional for servers that do not require authentication. If `MAIL_SMTP_HOST` is not set, notifications remain queued in MySQL until SMTP is configured. The application retries failed deliveries; each event is recorded with a unique key to prevent routine duplicate sends.

For an existing database, back it up first. Apply migration 001 only if it has not already been applied, then apply migration 002 to snapshot renter details and remove member accounts (do not rerun the fresh-install script, which recreates its tables):

```bash
mysql -u root -p library_management_system < database/migrations/001_rental_fines_notifications.sql
mysql -u root -p library_management_system < database/migrations/002_admin_only_rentals.sql
```

## Build and Deploy

```bash
mvn clean package
```

The build produces:

```text
target/library-management.war
```

Deploy it to Tomcat:

```bash
cp target/library-management.war "$CATALINA_HOME/webapps/"
"$CATALINA_HOME/bin/catalina.sh" run
```

Open:

```text
http://localhost:8080/library-management/
```

Make sure `DB_PASSWORD`, `DB_URL`, and `DB_USER` are present in the Tomcat process environment. For a Tomcat process in Docker, use the MySQL container hostname instead of `localhost`.

## Administrator Access

Open the site, choose **Login**, and sign in with an account whose database role is `ADMIN`. Successful admin login opens `/admin/dashboard.do`; the admin navigation provides inventory, all rentals, and overdue rentals.

The fresh-install SQL seeds `admin@library.local`, but does not provide its plain-text password. Set a known BCrypt password hash directly for this administrator before first login. Public registration is disabled; only users with the `ADMIN` role can authenticate.

```sql
UPDATE users
SET password_hash = '$2a$10$replace-with-a-generated-bcrypt-hash'
WHERE email = 'admin@library.local';
```

Do not use the example placeholder as a real hash. After setting the hash, sign in with the administrator email and corresponding password.

## Main Routes

| Route | Purpose |
|---|---|
| `/login.do` | Authenticate and start a session |
| `/logout.do` | End the session |
| `/admin/dashboard.do` | Admin statistics |
| `/books/list.do` | Admin inventory |
| `/books/search.do` | Search inventory |
| `/rentals/new.do` | Open the admin rental form |
| `/rentals/create.do` | Record a rental with renter contact details |
| `/rentals/list.do` | Admin rental history |
| `/returns/create.do` | Process a partial or full return as admin |
| `/rentals/overdue.do` | Overdue rental report |

The full route map is defined in [struts-config.xml](src/main/webapp/WEB-INF/struts-config.xml).

## Seed Accounts

The SQL script creates these accounts:

| Role | Email |
|---|---|
| Admin | `admin@library.local` |

The row contains a BCrypt hash. The source SQL does not record the corresponding plain-text password, so reset it through a controlled setup process before deployment.

## Rental and Return Behavior

- Renting locks the book row and atomically updates inventory plus rental state.
- Rental duration defaults to 14 calendar days; each rental stores its due date.
- Overdue days are counted after the due date and the backend calculates a ₹10/day fine. Open rentals accrue the current fine daily; full return stores the final amount.
- Insufficient stock rejects the rental.
- A full return sets status to `RETURNED`; a partial return sets `PARTIALLY_RETURNED`.
- Returning copies atomically updates rental status, book inventory, and the `returns` table.
- Books with active or pending rental quantities cannot be deleted.

## Security and Operations Notes

- The authentication filter requires an administrator session for all application pages and actions except login and static assets.
- Each rental stores a snapshot of renter name, email, phone, and address so history remains available without renter accounts.
- The SQL and application both enforce important constraints, but form and action validation is still basic.
- Run the application only over HTTPS in production and configure secure Tomcat session settings.
- No automated test suite is currently included.

## Validation

Build verification:

```bash
mvn clean package
```

The Maven WAR build should complete and produce `target/library-management.war`. There are currently no test sources to run with `mvn test`.

set -a
source .env
set +a
export DB_PASSWORD="${DB_PASSWORD:-$MYSQL_ROOT_PASSWORD}"
export DB_USER="${DB_USER:-root}"
export DB_URL="jdbc:mysql://localhost:3306/library_management_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"