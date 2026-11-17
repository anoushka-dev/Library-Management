<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Library Management System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body class="landing-page">
    <div class="container hero-box">
        <h1>Library Management System</h1>
        <p>Manage books, inventory, rentals, returns and overdue tracking.</p>
        <div class="actions">
            <a href="${pageContext.request.contextPath}/jsp/auth/login.jsp" class="btn primary">Login</a>
            <a href="${pageContext.request.contextPath}/jsp/auth/register.jsp" class="btn secondary">Register</a>
        </div>
    </div>
</body>
</html>
