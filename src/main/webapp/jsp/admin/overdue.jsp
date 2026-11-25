<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Overdue Rentals</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <div class="container">
        <h2>Overdue Rentals</h2>
        <table>
            <thead>
                <tr>
                    <th>Member</th>
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
                <c:forEach var="rental" items="${rentals}">
                    <tr>
                        <td>${rental.userName}</td>
                        <td>${rental.userEmail}</td>
                        <td>${rental.bookTitle}</td>
                        <td>${rental.rentalDate}</td>
                        <td>${rental.dueDate}</td>
                        <td>${rental.overdueDays}</td>
                        <td>₹${rental.currentFine}</td>
                        <td>${rental.status}</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty rentals}">
                    <tr><td colspan="8">No overdue rentals.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
