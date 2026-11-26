<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Books</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <div class="container">
        <h2>Book Inventory</h2>
        <div class="card">
            <a href="${pageContext.request.contextPath}/jsp/admin/books/form.jsp" class="btn primary">Add New Book</a>
            <a href="${pageContext.request.contextPath}/rentals/new.do" class="btn secondary">Record a Rental</a>
        </div>
        <form action="${pageContext.request.contextPath}/books/search.do" method="get">
            <input type="text" name="searchText" placeholder="Search by title, author, category or ISBN" />
            <button type="submit" class="btn primary">Search</button>
        </form>
        <table>
            <thead>
                <tr>
                    <th>Title</th>
                    <th>ISBN</th>
                    <th>Author</th>
                    <th>Total</th>
                    <th>Available</th>
                    <th>Rented</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="book" items="${books}">
                    <tr>
                        <td>${book.title}</td>
                        <td>${book.isbn}</td>
                        <td>${book.author}</td>
                        <td>${book.totalQuantity}</td>
                        <td>${book.availableQuantity}</td>
                        <td>${book.rentedQuantity}</td>
                        <td>
                            <c:if test="${book.availableQuantity > 0}">
                                <a href="${pageContext.request.contextPath}/rentals/new.do?bookId=${book.bookId}" class="btn primary">Rent</a>
                            </c:if>
                            <a href="${pageContext.request.contextPath}/books/delete.do?bookId=${book.bookId}" class="btn danger">Delete</a>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
