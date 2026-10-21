# Shop — Spring Boot Maven Project

## 1. Maven configuration

### Parent POM

The `spring-boot-starter-parent` in `pom.xml` provides default Maven configuration, recommended plugin settings, and dependency management for Spring Boot projects. This reduces the amount of configuration we need to write ourselves.

### Dependency version management

Spring Boot manages compatible dependency versions through its parent POM. Therefore, Spring Boot starters usually do not need an explicit `<version>` element. The parent supplies the managed versions, helping keep dependencies compatible.

### Spring Boot Maven plugin

The `spring-boot-maven-plugin` supports running the application with Maven and packaging it as an executable JAR. At package time, it repackages the application and its runtime dependencies into an executable archive that can be started with `java -jar`.

## 2. Dependency tree

Command:

```bash
./mvnw dependency:tree | head -40
```

Dependency tree excerpt:

**Replace this section with the actual first 40 lines printed by the command.**

```text
PASTE YOUR ACTUAL OUTPUT HERE
```

## 3. Hello endpoint

The application provides `GET /hello`, which returns a JSON greeting. The optional `name` query parameter defaults to `World`.

Example request:

```bash
curl -i "http://localhost:8080/hello?name=Alice"
```

Example response body:

```json
{
  "message": "Hello, Alice!",
  "at": "2026-10-21T18:00:00Z"
}
```

The timestamp is generated using `Instant.now()` and will vary with each request.

Spring Boot DevTools supports automatic application restarts during development when compiled classpath files change. Changing the controller message and rebuilding or triggering compilation allows the application to restart automatically.

## 4. Packaging and executable JAR

Build the application:

```bash
./mvnw -q package
```

Run the packaged JAR on port 9090:

```bash
java -jar target/shop-0.0.1-SNAPSHOT.jar --server.port=9090
```

Test the endpoint:

```bash
curl -i "http://localhost:9090/hello?name=Alice"
```

JAR size: **Replace with the actual size reported by `du -h target/shop-0.0.1-SNAPSHOT.jar`.**

The executable JAR contains the compiled application, required runtime dependencies, and Spring Boot launcher. This makes it possible to run the application without manually assembling its dependency classpath.

## 5. Actuator health

Request:

```bash
curl -i http://localhost:9090/actuator/health
```

Expected response:

```json
{"status":"UP"}
```

The `UP` status indicates that the health endpoint reports the application as healthy.

## 6. Auto-configuration condition report

Start the application with debug logging enabled:

```bash
java -jar target/shop-0.0.1-SNAPSHOT.jar --server.port=9090 --debug
```

The condition evaluation report showed that `DataSourceAutoConfiguration` matched because the required JDBC classes were available and no existing `DataSource` bean prevented the default configuration.

Relevant report entries included:

```text
DataSourceAutoConfiguration matched:
- @ConditionalOnClass found required classes 'javax.sql.DataSource',
  'org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType'
- @ConditionalOnMissingBean (types: javax.sql.DataSource,
  javax.sql.XADataSource; SearchStrategy: all) did not find any beans

DataSourceConfiguration.Hikari matched:
- @ConditionalOnClass found required class 'com.zaxxer.hikari.HikariDataSource'
- @ConditionalOnMissingBean (types: javax.sql.DataSource;
  SearchStrategy: all) did not find any beans
```

Spring Boot therefore configured a Hikari connection pool for the H2 database. The successful H2 connection in the application logs confirms that the database connection was established.
