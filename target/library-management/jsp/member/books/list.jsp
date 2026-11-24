<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Available Books</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body>
    <jsp:include page="/jsp/common/header.jsp" />
    <div class="container">
        <h2>Available Books</h2>
        <div class="card">
            <a href="${pageContext.request.contextPath}/jsp/admin/books/form.jsp" class="btn primary">Add New Book</a>
        </div>
        <form action="${pageContext.request.contextPath}/books/search.do" method="get">
            <input type="text" name="searchText" placeholder="Search by title, author, category or ISBN" />
            <button type="submit" class="btn primary">Search</button>
        </form>
        <table>
            <thead>
                <tr>
                    <th>Title</th>
                    <th>Author</th>
                    <th>Category</th>
                    <th>Available</th>
                    <th>Rent</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="book" items="${books}">
                    <tr>
                        <td>${book.title}</td>
                        <td>${book.author}</td>
                        <td>${book.category}</td>
                        <td>${book.availableQuantity}</td>
                        <td>
                            <form action="${pageContext.request.contextPath}/rentals/create.do" method="post">
                                <input type="hidden" name="bookId" value="${book.bookId}" />
                                <label for="quantity">Number of books:</label>
                                <input type="number" placeholder="Number of books" name="quantity" min="1" max="${book.availableQuantity}" required="required" />
                                <label for="rentalDays">Number of days:</label>
                                <input type="number" placeholder="Number of days" name="rentalDays" min="1" required="required" />
                                <button type="submit" class="btn primary">Rent</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
