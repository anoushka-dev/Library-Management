<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<!DOCTYPE html>
<html>
<head>
    <title>Register</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body>
    <div class="container">
        <div class="card" style="max-width:600px; margin:60px auto;">
            <h2>Member Registration</h2>
            <% if (request.getAttribute("errorMessage") != null) { %>
                <div class="error"><%= request.getAttribute("errorMessage") %></div>
            <% } %>
            <html:form action="/register" focus="fullName">
                <label>Full Name</label>
                <html:text property="fullName" />
                <label>Email</label>
                <html:text property="email" />
                <label>Password</label>
                <html:password property="password" />
                <label>Confirm Password</label>
                <html:password property="confirmPassword" />
                <label>Phone</label>
                <html:text property="phone" />
                <label>Address</label>
                <html:text property="address" />
                <html:submit styleClass="btn primary">Register</html:submit>
                <a href="${pageContext.request.contextPath}/jsp/auth/login.jsp" class="btn secondary">Back to Login</a>
            </html:form>
        </div>
    </div>
</body>
</html>
