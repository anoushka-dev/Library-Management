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
        <h2>Member Dashboard</h2>
        <div class="grid">
            <div class="stat-box"><h3>Available Books</h3><h2>${stats.availableBooks}</h2></div>
            <div class="stat-box"><h3>My Active Rentals</h3><h2>${stats.activeRentals}</h2></div>
            <div class="stat-box"><h3>My Overdue Rentals</h3><h2>${stats.overdueRentals}</h2></div>
        </div>
        <div class="card">
            <a href="${pageContext.request.contextPath}/books/search.do" class="btn primary">Browse Books</a>
            <a href="${pageContext.request.contextPath}/rentals/list.do" class="btn secondary">My Rental History</a>
        </div>
    </div>
    <jsp:include page="/jsp/common/footer.jsp" />
</body>
</html>
