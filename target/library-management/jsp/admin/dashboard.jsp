<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <div class="container">
        <h2>Admin Dashboard</h2>
        <div class="grid">
            <div class="stat-box"><h3>Total Books</h3><h2>${stats.totalBooks}</h2></div>
            <div class="stat-box"><h3>Available Books</h3><h2>${stats.availableBooks}</h2></div>
            <div class="stat-box"><h3>Currently Rented Books</h3><h2>${stats.rentedBooks}</h2></div>
            <div class="stat-box"><h3>Active Rentals</h3><h2>${stats.activeRentals}</h2></div>
            <div class="stat-box"><h3>Overdue Rentals</h3><h2>${stats.overdueRentals}</h2></div>
            <div class="stat-box"><h3>Total Returned Books</h3><h2>${stats.returnedBooks}</h2></div>
            <div class="stat-box"><h3>Outstanding Fines</h3><h2>₹${stats.outstandingFines}</h2></div>
        </div>
        <div class="card">
            <a href="${pageContext.request.contextPath}/jsp/admin/books/form.jsp" class="btn primary">Add Book</a>
            <a href="${pageContext.request.contextPath}/rentals/new.do" class="btn secondary">Record a Rental</a>
            <a href="${pageContext.request.contextPath}/books/list.do" class="btn secondary">Book Inventory</a>
            <a href="${pageContext.request.contextPath}/rentals/list.do" class="btn secondary">Rental History</a>
            <a href="${pageContext.request.contextPath}/rentals/overdue.do" class="btn secondary">Overdue Rentals</a>
        </div>
        <div class="card">
            <h3>Overdue Rentals</h3>
            <table>
                <thead>
                    <tr>
                        <th>Renter</th>
                        <th>Email</th>
                        <th>Book</th>
                        <th>Rental Date</th>
                        <th>Due Date</th>
                        <th>Overdue Days</th>
                        <th>Current Fine</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="rental" items="${overdueRentals}">
                        <tr>
                            <td>${rental.renterName}</td>
                            <td>${rental.renterEmail}</td>
                            <td>${rental.bookTitle}</td>
                            <td>${rental.rentalDate}</td>
                            <td>${rental.dueDate}</td>
                            <td>${rental.overdueDays}</td>
                            <td>₹${rental.currentFine}</td>
                            <td>${rental.status}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty overdueRentals}">
                        <tr><td colspan="8">No overdue rentals.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
