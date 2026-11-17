# Library Management System

This project is a complete Java Struts 1.x MVC application for a library system using MySQL and JDBC. It includes admin and member workflows, a book catalog, inventory, rentals, returns, and overdue monitoring.

## Features

- Admin and member authentication
- Role-based access control
- Book management with inventory tracking
- Book rental workflow with transactional stock updates
- Partial and full book return handling
- Overdue rental detection
- MySQL database schema and seed scripts
- Struts 1.2-style MVC architecture and JSP views

## Project structure

- `src/main/java/com/library/...` – Java model, DAO, form, action, filter, and utility code
- `src/main/webapp/...` – JSP pages, Struts config, and web.xml
- `database/library_management_system.sql` – database schema

## Prerequisites

- JDK 8+
- Maven 3+
- MySQL 8+
- Apache Tomcat 8/9

## Database setup

1. Create a MySQL database and user:
   `CREATE DATABASE library_management_system;`
2. Import the SQL script:
   `mysql -u root -p library_management_system < database/library_management_system.sql`
3. Update database credentials in `DatabaseUtil` if needed.

## Build and run

```bash
mvn clean package
```

Deploy the generated WAR file to Tomcat, or run it locally with an application server configured for the project.

## Notes

The application follows the traditional MVC flow used with Struts 1.x and uses JDBC transactions to keep inventory records consistent.
