<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<!DOCTYPE html>
<html>
<head>
    <title>Return Books</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body>
    <div class="container">
        <div class="card" style="max-width:500px; margin:50px auto;">
            <h2>Return Books</h2>
            <html:form action="/returns/create">
                <label>Rental ID</label>
                <html:text property="rentalId" />
                <label>Return Quantity</label>
                <html:text property="quantity" />
                <html:submit styleClass="btn primary">Return</html:submit>
            </html:form>
        </div>
    </div>
</body>
</html>
