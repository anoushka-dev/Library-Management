<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<div class="navbar">
    <div>
        <strong>Library Management</strong>
    </div>
    <div>
        <a href="${contextPath}/member/dashboard.do">Dashboard</a>
        <a href="${contextPath}/books/list.do">Books</a>
        <a href="${contextPath}/rentals/list.do">Rentals</a>
        <a href="${contextPath}/logout.do">Logout</a>
    </div>
</div>
