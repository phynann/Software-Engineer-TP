# Task 4: API Session Log

This document records HTTP requests and responses exercising all REST endpoints of `/api/products`, including validation and error cases using RFC 7807/9457 `ProblemDetail`.

---

### 1. List Products (GET /api/products)
```bash
$ curl -i http://localhost:8080/api/products
```
```http
HTTP/1.1 200 OK
Content-Type: application/json
Transfer-Encoding: chunked

[
  {"id":1,"name":"MILK","price":20.00,"stock":50},
  {"id":2,"name":"HAM","price":12.00,"stock":20},
  {"id":3,"name":"COCA COLA","price":3.50,"stock":70},
  {"id":4,"name":"WATER","price":1.50,"stock":90},
  {"id":5,"name":"CHOCOLATE","price":12.00,"stock":20}
]
```

---

### 2. Search Products by Name Query (GET /api/products?q=ham)
```bash
$ curl -i "http://localhost:8080/api/products?q=ham"
```
```http
HTTP/1.1 200 OK
Content-Type: application/json
Transfer-Encoding: chunked

[
  {"id":2,"name":"HAM","price":12.00,"stock":20}
]
```

---

### 3. Get Product by ID (GET /api/products/1)
```bash
$ curl -i http://localhost:8080/api/products/1
```
```http
HTTP/1.1 200 OK
Content-Type: application/json
Transfer-Encoding: chunked

{"id":1,"name":"MILK","price":20.00,"stock":50}
```

---

### 4. Get Product Not Found (GET /api/products/999)
```bash
$ curl -i http://localhost:8080/api/products/999
```
```http
HTTP/1.1 404 Not Found
Content-Type: application/problem+json
Transfer-Encoding: chunked

{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Product 999 not found",
  "instance": "/api/products/999"
}
```

---

### 5. Create Product with Validation Errors (POST /api/products)
```bash
$ curl -i -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name": "", "price": 0, "stock": -1}'
```
```http
HTTP/1.1 400 Bad Request
Content-Type: application/problem+json
Transfer-Encoding: chunked

{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/products",
  "errors": [
    {"field": "stock", "rejectedValue": "-1", "message": "must be greater than or equal to 0"},
    {"field": "name", "rejectedValue": "", "message": "must not be blank"},
    {"field": "price", "rejectedValue": "0", "message": "must be greater than or equal to 0.01"}
  ]
}
```

---

### 6. Create Product Successfully (POST /api/products)
```bash
$ curl -i -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"USB-C cable","price":7.90,"stock":25}'
```
```http
HTTP/1.1 201 Created
Location: /api/products/6
Content-Type: application/json
Transfer-Encoding: chunked

{"id":6,"name":"USB-C cable","price":7.90,"stock":25}
```

---

### 7. Duplicate Key Handling (POST /api/products)
```bash
$ curl -i -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"USB-C cable","price":7.90,"stock":10}'
```
```http
HTTP/1.1 409 Conflict
Content-Type: application/problem+json
Transfer-Encoding: chunked

{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "A product with this name already exists",
  "instance": "/api/products"
}
```

---

### 8. Update Stock Successfully (PATCH /api/products/6/stock)
```bash
$ curl -i -X PATCH http://localhost:8080/api/products/6/stock \
  -H "Content-Type: application/json" \
  -d '{"delta": 5}'
```
```http
HTTP/1.1 200 OK
Content-Type: application/json
Transfer-Encoding: chunked

{"id":6,"name":"USB-C cable","price":7.90,"stock":30}
```

---

### 9. Update Stock with Insufficient Quantity (PATCH /api/products/6/stock)
```bash
$ curl -i -X PATCH http://localhost:8080/api/products/6/stock \
  -H "Content-Type: application/json" \
  -d '{"delta": -35}'
```
```http
HTTP/1.1 409 Conflict
Content-Type: application/problem+json
Transfer-Encoding: chunked

{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Product 6 has 30 units, cannot remove 35",
  "instance": "/api/products/6/stock"
}
```

---

### 10. Delete Product Successfully (DELETE /api/products/6)
```bash
$ curl -i -X DELETE http://localhost:8080/api/products/6
```
```http
HTTP/1.1 204 No Content
```

---

### 11. Delete Non-Existent Product (DELETE /api/products/999)
```bash
$ curl -i -X DELETE http://localhost:8080/api/products/999
```
```http
HTTP/1.1 404 Not Found
Content-Type: application/problem+json
Transfer-Encoding: chunked

{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Product 999 not found",
  "instance": "/api/products/999"
}
```
