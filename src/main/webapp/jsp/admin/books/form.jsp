<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<!DOCTYPE html>
<html>
<head>
    <title>Book Form</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body>
    <div class="container">
        <div class="card" style="max-width:700px; margin:40px auto;">
            <h2>Add Book</h2>
            <html:form action="/books/add">
                <label>ISBN</label>
                <html:text property="isbn" />
                <label>Title</label>
                <html:text property="title" />
                <label>Author</label>
                <html:text property="author" />
                <label>Category</label>
                <html:text property="category" />
                <label>Publisher</label>
                <html:text property="publisher" />
                <label>Publication Year</label>
                <html:text property="publicationYear" />
                <label>Summary</label>
                <html:textarea property="description" rows="5" />
                <label>Total Quantity</label>
                <html:text property="totalQuantity" />
                <html:submit styleClass="btn primary">Save</html:submit>
                <a href="${pageContext.request.contextPath}/books/list.do" class="btn secondary">Back</a>
            </html:form>
        </div>
    </div>
</body>
</html>
