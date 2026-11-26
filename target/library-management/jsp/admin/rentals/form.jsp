<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<!DOCTYPE html>
<html>
<head>
    <title>Record Rental</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <div class="container">
        <div class="card" style="max-width:700px; margin:40px auto;">
            <h2>Record a Rental</h2>
            <html:errors />
            <c:if test="${not empty errorMessage}">
                <div class="error"><c:out value="${errorMessage}" /></div>
            </c:if>
            <c:choose>
                <c:when test="${empty availableBooks}">
                    <p>There are no books available to rent.</p>
                </c:when>
                <c:otherwise>
                    <form action="${pageContext.request.contextPath}/rentals/create.do" method="post">
                        <label for="bookId">Book</label>
                        <select id="bookId" name="bookId" required="required">
                            <option value="">Select a book</option>
                            <c:forEach var="book" items="${availableBooks}">
                                <option value="${book.bookId}" <c:if test="${book.bookId == selectedBookId || book.bookId == rentalForm.bookId}">selected="selected"</c:if>>
                                    <c:out value="${book.title}" /> (ISBN <c:out value="${book.isbn}" />; ${book.availableQuantity} available)
                                </option>
                            </c:forEach>
                        </select>

                        <label for="renterName">Renter name</label>
                        <input id="renterName" type="text" name="renterName" maxlength="120" required="required" value="<c:out value='${rentalForm.renterName}' />" />
                        <label for="renterEmail">Renter email</label>
                        <input id="renterEmail" type="email" name="renterEmail" maxlength="150" required="required" value="<c:out value='${rentalForm.renterEmail}' />" />
                        <label for="renterPhone">Phone</label>
                        <input id="renterPhone" type="tel" name="renterPhone" maxlength="30" value="<c:out value='${rentalForm.renterPhone}' />" />
                        <label for="renterAddress">Address</label>
                        <input id="renterAddress" type="text" name="renterAddress" maxlength="255" value="<c:out value='${rentalForm.renterAddress}' />" />
                        <label for="quantity">Number of copies</label>
                        <input id="quantity" type="number" name="quantity" min="1" value="${rentalForm.quantity > 0 ? rentalForm.quantity : 1}" required="required" />
                        <label for="rentalDays">Rental duration (days)</label>
                        <input id="rentalDays" type="number" name="rentalDays" min="1" value="${rentalForm.rentalDays > 0 ? rentalForm.rentalDays : 14}" required="required" />
                        <button type="submit" class="btn primary">Save Rental</button>
                        <a href="${pageContext.request.contextPath}/books/list.do" class="btn secondary">Cancel</a>
                    </form>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
