# Library Management System

A server-rendered Java 8 library application for managing books, inventory, member rentals, partial/full returns, and overdue reports. It uses Apache Struts 1.2, JSP/JSTL, JDBC, MySQL 8, Maven, and Apache Tomcat.

## Features

- BCrypt password authentication and session-based login
- Administrator and member workflows
- Book catalog with searchable inventory
- Add, update, and delete book operations
- Book rental with stock locking and database transactions
- Partial and full return processing
- Active, partially returned, returned, and overdue rental states
- Admin dashboard and overdue rental report
- Member dashboard, available-book search, and rental history
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

## Main Routes

| Route | Purpose |
|---|---|
| `/login.do` | Authenticate and start a session |
| `/register.do` | Register a member account |
| `/logout.do` | End the session |
| `/admin/dashboard.do` | Admin statistics |
| `/member/dashboard.do` | Member statistics |
| `/books/list.do` | Admin inventory |
| `/books/search.do` | Member search |
| `/rentals/create.do` | Create a rental |
| `/rentals/list.do` | User rentals or all rentals for admin |
| `/returns/create.do` | Process a partial or full return |
| `/rentals/overdue.do` | Overdue rental report |

The full route map is defined in [struts-config.xml](src/main/webapp/WEB-INF/struts-config.xml).

## Seed Accounts

The SQL script creates these accounts:

| Role | Email |
|---|---|
| Admin | `admin@library.local` |
| Member | `member@library.local` |

Both rows contain BCrypt hashes. The source SQL does not record the corresponding plain-text passwords, so reset them through a controlled setup process before deployment.

## Rental and Return Behavior

- Renting locks the book row and atomically updates inventory plus rental state.
- Insufficient stock rejects the rental.
- A full return sets status to `RETURNED`; a partial return sets `PARTIALLY_RETURNED`.
- Returning copies atomically updates rental status, book inventory, and the `returns` table.
- Books with active or pending rental quantities cannot be deleted.

## Security and Operations Notes

- `AuthFilter` requires a session for protected paths, but it does not currently enforce admin role checks on every admin action.
- Member routes and admin routes should receive explicit role checks before production use.
- The SQL and application both enforce important constraints, but form and action validation is still basic.
- Run the application only over HTTPS in production and configure secure Tomcat session settings.
- No automated test suite is currently included.

## Validation

Build verification:

```bash
mvn clean package
```

The Maven WAR build should complete and produce `target/library-management.war`. There are currently no test sources to run with `mvn test`.
