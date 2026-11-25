<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Member Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <div class="container">
        <h2>My Account</h2>
        <div class="card">
            <h3>Account Details</h3>
            <p><strong>Name:</strong> <c:out value="${sessionScope.currentUser.fullName}" /></p>
            <p><strong>Email:</strong> <c:out value="${sessionScope.currentUser.email}" /></p>
            <p><strong>Phone:</strong>
                <c:choose>
                    <c:when test="${not empty sessionScope.currentUser.phone}"><c:out value="${sessionScope.currentUser.phone}" /></c:when>
                    <c:otherwise>Not provided</c:otherwise>
                </c:choose>
            </p>
            <p><strong>Address:</strong>
                <c:choose>
                    <c:when test="${not empty sessionScope.currentUser.address}"><c:out value="${sessionScope.currentUser.address}" /></c:when>
                    <c:otherwise>Not provided</c:otherwise>
                </c:choose>
            </p>
        </div>
        <h3>Rental Overview</h3>
        <div class="grid">
            <div class="stat-box"><h3>Available Books</h3><h2>${stats.availableBooks}</h2></div>
            <div class="stat-box"><h3>My Active Rentals</h3><h2>${stats.activeRentals}</h2></div>
            <div class="stat-box"><h3>My Overdue Rentals</h3><h2>${stats.overdueRentals}</h2></div>
            <div class="stat-box"><h3>Outstanding Fines</h3><h2>₹${stats.outstandingFines}</h2></div>
        </div>
        <div class="card">
            <a href="${pageContext.request.contextPath}/books/search.do" class="btn primary">Browse Books</a>
            <a href="${pageContext.request.contextPath}/rentals/list.do" class="btn secondary">My Rental History</a>
        </div>
        <div class="card">
            <h3>Currently Rented Books</h3>
            <table>
                <thead>
                    <tr>
                        <th>Book</th>
                        <th>Quantity</th>
                        <th>Rental Date</th>
                        <th>Expected Return</th>
                        <th>Time Remaining</th>
                        <th>Overdue Days</th>
                        <th>Current Fine</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="rental" items="${activeRentals}">
                        <tr>
                            <td>${rental.bookTitle}</td>
                            <td>${rental.rentalQuantity - rental.returnedQuantity}</td>
                            <td>${rental.rentalDate}</td>
                            <td>${rental.dueDate}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${rental.status == 'OVERDUE'}">${rental.overdueDays} days overdue</c:when>
                                    <c:when test="${rental.daysRemaining == 0}">Due today</c:when>
                                    <c:otherwise>${rental.daysRemaining} days remaining</c:otherwise>
                                </c:choose>
                            </td>
                            <td>${rental.overdueDays}</td>
                            <td>₹${rental.currentFine}</td>
                            <td>${rental.status}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty activeRentals}">
                        <tr><td colspan="8">You have no currently rented books.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
