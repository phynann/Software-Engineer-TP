# Shop Gradle Kotlin

Spring Boot application generated with Gradle Kotlin DSL.

## Requirements

- JDK 26
- Gradle Wrapper included in this repository

## Run the application

```bash
./gradlew bootRun
```

The application starts on port 8080 by default.

## Hello endpoint

```bash
curl "http://localhost:8080/hello?name=Alice"
```

The response contains a greeting message and the current timestamp.

## Health endpoint

```bash
curl "http://localhost:8080/actuator/health"
```

## Build and test

```bash
./gradlew clean build
```

The Gradle Wrapper files are committed so other developers can build the project using `./gradlew`. Generated `.gradle/` and `build/` directories are excluded from Git.
