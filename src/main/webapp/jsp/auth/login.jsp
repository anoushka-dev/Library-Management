<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body>
    <div class="container">
        <div class="card" style="max-width:500px; margin:80px auto;">
            <h2>Login</h2>
            <% if (request.getAttribute("errorMessage") != null) { %>
                <div class="error"><%= request.getAttribute("errorMessage") %></div>
            <% } %>
            <% if (request.getAttribute("successMessage") != null) { %>
                <div class="success"><%= request.getAttribute("successMessage") %></div>
            <% } %>
            <html:form action="/login" focus="email">
                <label>Email</label>
                <html:text property="email" />
                <label>Password</label>
                <html:password property="password" />
                <html:submit styleClass="btn primary">Login</html:submit>
            </html:form>
            <p>New user? <a href="${pageContext.request.contextPath}/jsp/auth/register.jsp">Register here</a></p>
        </div>
    </div>
</body>
</html>
