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
                    <th>Book</th>
                    <th>Quantity</th>
                    <th>Returned</th>
                    <th>Due Date</th>
                    <th>Status</th>
                    <th>Return</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="rental" items="${rentals}">
                    <tr>
                        <td>${rental.rentalId}</td>
                        <td>${rental.bookTitle}</td>
                        <td>${rental.rentalQuantity}</td>
                        <td>${rental.returnedQuantity}</td>
                        <td>${rental.dueDate}</td>
                        <td>${rental.status}</td>
                        <td>
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
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
