<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
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
        <html:errors />
        <div class="card">
            <a href="${pageContext.request.contextPath}/rentals/new.do" class="btn primary">Record a Rental</a>
        </div>
        <table>
            <thead>
                <tr>
                    <th>Rental ID</th>
                    <th>Renter</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Address</th>
                    <th>Book</th>
                    <th>Quantity</th>
                    <th>Returned</th>
                    <th>Rental Date</th>
                    <th>Due Date</th>
                    <th>Time Remaining / Overdue</th>
                    <th>Current Fine</th>
                    <th>Final Fine</th>
                    <th>Status</th>
                    <th>Return</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="rental" items="${rentals}">
                    <tr>
                        <td>${rental.rentalId}</td>
                        <td><c:out value="${rental.renterName}" /></td>
                        <td><c:out value="${rental.renterEmail}" /></td>
                        <td><c:out value="${rental.renterPhone}" /></td>
                        <td><c:out value="${rental.renterAddress}" /></td>
                        <td><c:out value="${rental.bookTitle}" /></td>
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
                        <td>
                            <c:if test="${rental.returnedQuantity < rental.rentalQuantity}">
                                <form action="${pageContext.request.contextPath}/returns/create.do" method="post">
                                    <input type="hidden" name="rentalId" value="${rental.rentalId}" />
                                    <input type="number" name="quantity" min="1" max="${rental.rentalQuantity - rental.returnedQuantity}" value="${rental.rentalQuantity - rental.returnedQuantity}" required="required" />
                                    <button type="submit" class="btn primary">Return</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty rentals}">
                    <tr><td colspan="15">No rental records found.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
