<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"    uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html><head><title>Add Product</title><link rel="stylesheet" href="/css/site.css"></head>
<body>
<h1>Add Product (${currency})</h1>
<form:form method="post" action="/products" modelAttribute="createProduct">
  <div>
    <label for="name">Name:</label>
    <form:input path="name" id="name"/>
    <form:errors path="name" cssClass="error"/>
  </div>
  <div>
    <label for="price">Price:</label>
    <form:input path="price" id="price"/>
    <form:errors path="price" cssClass="error"/>
  </div>
  <div>
    <label for="stock">Stock:</label>
    <form:input path="stock" id="stock"/>
    <form:errors path="stock" cssClass="error"/>
  </div>
  <div>
    <button type="submit">Save</button>
  </div>
</form:form>
<p><a href="/products">Back to product list</a></p>
</body></html>
