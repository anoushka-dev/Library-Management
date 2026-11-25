<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Rental History</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <div class="container">
        <h2>Rental History</h2>
        <table>
            <thead>
                <tr>
                    <th>Rental ID</th>
                    <c:if test="${sessionScope.currentUser.role == 'ADMIN'}"><th>User</th><th>Email</th></c:if>
                    <th>Book</th>
                    <th>Quantity</th>
                    <th>Returned</th>
                    <th>Rental Date</th>
                    <th>Due Date</th>
                    <th>Time Remaining / Overdue</th>
                    <th>Current Fine</th>
                    <th>Final Fine</th>
                    <th>Status</th>
                    <c:if test="${sessionScope.currentUser.role != 'ADMIN'}"><th>Return</th></c:if>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="rental" items="${rentals}">
                    <tr>
                        <td>${rental.rentalId}</td>
                        <c:if test="${sessionScope.currentUser.role == 'ADMIN'}"><td>${rental.userName}</td><td>${rental.userEmail}</td></c:if>
                        <td>${rental.bookTitle}</td>
                        <td>${rental.rentalQuantity}</td>
                        <td>${rental.returnedQuantity}</td>
                        <td>${rental.rentalDate}</td>
                        <td>${rental.dueDate}</td>
                        <td>
                            <c:choose>
                                <c:when test="${rental.status == 'OVERDUE'}">${rental.overdueDays} days overdue</c:when>
                                <c:when test="${rental.returnedQuantity >= rental.rentalQuantity}">Returned</c:when>
                                <c:when test="${rental.daysRemaining == 0}">Due today</c:when>
                                <c:otherwise>${rental.daysRemaining} days remaining</c:otherwise>
                            </c:choose>
                        </td>
                        <td>₹${rental.currentFine}</td>
                        <td>₹${rental.fineAmount}</td>
                        <td>${rental.status}</td>
                        <c:if test="${sessionScope.currentUser.role != 'ADMIN'}"><td>
                            <c:choose>
                                <c:when test="${rental.returnedQuantity >= rental.rentalQuantity}">
                                    <span class="return-icon return-complete" title="All copies returned" aria-label="All copies returned">&#10004;</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="return-icon return-partial" title="Not all copies returned" aria-label="Not all copies returned">&#10068;</span>
                                <form action="${pageContext.request.contextPath}/returns/create.do" method="post">
                                    <input type="hidden" name="rentalId" value="${rental.rentalId}" />
                                    <input type="number" name="quantity" min="1" max="${rental.rentalQuantity - rental.returnedQuantity}" value="${rental.rentalQuantity - rental.returnedQuantity}" required="required" />
                                    <button type="submit" class="btn primary">Return</button>
                                </form>
                                </c:otherwise>
                            </c:choose>
                        </td></c:if>
                    </tr>
                </c:forEach>
                <c:if test="${empty rentals}">
                    <tr><td colspan="${sessionScope.currentUser.role == 'ADMIN' ? 12 : 11}">No rental records found.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
