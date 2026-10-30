## Task 3 – JDBC, JdbcClient and transactions

### Line-count comparison

- `RawJdbcProductRepository.findAll()`: 15 lines
- `ProductRepository.findAll()` (JdbcClient): 4 lines

JdbcClient removes the connection handling, row mapping and checked exceptions.

### Duplicate name

Inserting an existing name throws `org.springframework.dao.DuplicateKeyException`
(cause: H2's `JdbcSQLIntegrityConstraintViolationException`).
The translation happens inside `JdbcClient`/`JdbcTemplate`: it catches the `SQLException`
and a `SQLExceptionTranslator` maps the H2 error code to Spring's `DataAccessException` hierarchy.