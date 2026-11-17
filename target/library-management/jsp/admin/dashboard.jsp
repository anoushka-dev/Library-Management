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
            <div class="stat-box"><h3>Total Members</h3><h2>${stats.totalMembers}</h2></div>
            <div class="stat-box"><h3>Active Rentals</h3><h2>${stats.activeRentals}</h2></div>
            <div class="stat-box"><h3>Overdue Rentals</h3><h2>${stats.overdueRentals}</h2></div>
        </div>
        <div class="card">
            <a href="${pageContext.request.contextPath}/jsp/admin/books/form.jsp" class="btn primary">Add Book</a>
            <a href="${pageContext.request.contextPath}/books/list.do" class="btn secondary">Book Inventory</a>
            <a href="${pageContext.request.contextPath}/rentals/list.do" class="btn secondary">All Rentals</a>
            <a href="${pageContext.request.contextPath}/rentals/overdue.do" class="btn secondary">Overdue Rentals</a>
        </div>
    </div>
    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
