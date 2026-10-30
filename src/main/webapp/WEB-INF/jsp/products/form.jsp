<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html><head><title>Add Product</title><link rel="stylesheet" href="/css/site.css"></head>
<body>
<h1>Add Product (${currency})</h1>
<form:form method="post" action="/products" modelAttribute="product">
    <div>
        <label>Name:</label>
        <form:input path="name"/>
        <form:errors path="name" cssClass="error"/>
    </div>
    <div>
        <label>Price:</label>
        <form:input path="price"/>
        <form:errors path="price" cssClass="error"/>
    </div>
    <div>
        <label>Stock:</label>
        <form:input path="stock"/>
        <form:errors path="stock" cssClass="error"/>
    </div>
    <button type="submit">Save</button>
</form:form>
<a href="/products">Back</a>
</body></html>
