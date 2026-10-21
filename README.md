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


```text
[INFO] Scanning for projects...
[INFO] Loaded 24175 auto-discovered prefixes for remote repository central (prefixes-central.txt)
[INFO] 
[INFO] --------------------------< edu.itc.se:shop >---------------------------
[INFO] Building shop 0.0.1-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- dependency:3.10.0:tree (default-cli) @ shop ---
[INFO] Loaded 74 auto-discovered prefixes for remote repository apache.snapshots (prefixes-apache.snapshots.txt)
[INFO] edu.itc.se:shop:jar:0.0.1-SNAPSHOT
[INFO] +- org.springframework.boot:spring-boot-h2console:jar:4.1.1:compile
[INFO] |  \- org.springframework.boot:spring-boot:jar:4.1.1:compile
[INFO] |     +- org.springframework:spring-core:jar:7.0.9:compile
[INFO] |     |  \- commons-logging:commons-logging:jar:1.3.6:compile
[INFO] |     \- org.springframework:spring-context:jar:7.0.9:compile
[INFO] |        +- org.springframework:spring-aop:jar:7.0.9:compile
[INFO] |        +- org.springframework:spring-beans:jar:7.0.9:compile
[INFO] |        \- org.springframework:spring-expression:jar:7.0.9:compile
[INFO] +- org.springframework.boot:spring-boot-starter-actuator:jar:4.1.1:compile
[INFO] |  +- org.springframework.boot:spring-boot-starter:jar:4.1.1:compile
[INFO] |  |  +- org.springframework.boot:spring-boot-starter-logging:jar:4.1.1:compile
[INFO] |  |  |  +- ch.qos.logback:logback-classic:jar:1.5.38:compile
[INFO] |  |  |  |  \- ch.qos.logback:logback-core:jar:1.5.38:compile
[INFO] |  |  |  +- org.apache.logging.log4j:log4j-to-slf4j:jar:2.25.5:compile
[INFO] |  |  |  |  \- org.apache.logging.log4j:log4j-api:jar:2.25.5:compile
[INFO] |  |  |  \- org.slf4j:jul-to-slf4j:jar:2.0.18:compile
[INFO] |  |  +- jakarta.annotation:jakarta.annotation-api:jar:3.0.0:compile
[INFO] |  |  \- org.yaml:snakeyaml:jar:2.6:compile
[INFO] |  +- org.springframework.boot:spring-boot-starter-micrometer-metrics:jar:4.1.1:compile
[INFO] |  |  \- org.springframework.boot:spring-boot-micrometer-metrics:jar:4.1.1:compile
[INFO] |  |     \- org.springframework.boot:spring-boot-micrometer-observation:jar:4.1.1:compile
[INFO] |  +- org.springframework.boot:spring-boot-actuator-autoconfigure:jar:4.1.1:compile
[INFO] |  |  \- org.springframework.boot:spring-boot-actuator:jar:4.1.1:compile
[INFO] |  +- org.springframework.boot:spring-boot-health:jar:4.1.1:compile
[INFO] |  +- io.micrometer:micrometer-observation:jar:1.17.1:compile
[INFO] |  |  +- org.jspecify:jspecify:jar:1.0.1:compile
[INFO] |  |  \- io.micrometer:micrometer-commons:jar:1.17.1:compile
[INFO] |  \- io.micrometer:micrometer-jakarta9:jar:1.17.1:compile
[INFO] |     \- io.micrometer:micrometer-core:jar:1.17.1:compile
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

JAR size: **27M     target/shop-0.0.1-SNAPSHOT.jar**

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
