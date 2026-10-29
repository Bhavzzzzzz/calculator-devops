# Calculator DevOps

A Calculator REST API built with Java (Spring Boot + Maven), wrapped in an end-to-end CI/CD pipeline
using Git/GitHub, Jenkins, Docker and Docker Compose.

CSE 816 – Software Production Engineering, Mini-Project.

## Prerequisites

- JDK 17+
- No local Maven install needed — the project ships with the Maven Wrapper (`mvnw` / `mvnw.cmd`).

## Build, test and run

```bash
./mvnw clean package -DskipTests   # build the executable JAR -> target/calculator.jar
./mvnw test                        # run unit + integration tests (reports in target/surefire-reports)
java -jar target/calculator.jar    # start the API on http://localhost:8080
```

On Windows use `mvnw.cmd` instead of `./mvnw`. The port can be changed with the `SERVER_PORT` environment variable.

## API

All operations take two numeric query parameters `a` and `b`.

| Operation | Endpoint                      | Result                            |
|-----------|-------------------------------|-----------------------------------|
| Add       | `GET /api/v1/add?a=&b=`       | `a + b`                           |
| Subtract  | `GET /api/v1/subtract?a=&b=`  | `a - b`                           |
| Multiply  | `GET /api/v1/multiply?a=&b=`  | `a * b`                           |
| Divide    | `GET /api/v1/divide?a=&b=`    | `a / b` (`b = 0` → HTTP 400)      |
| Health    | `GET /actuator/health`        | `{"status":"UP"}`                 |

### Example

```bash
curl "http://localhost:8080/api/v1/add?a=2&b=3"
```

```json
{ "operation": "add", "a": 2.0, "b": 3.0, "result": 5.0 }
```

### Errors

Invalid or missing parameters, non-finite numbers (`NaN`, `Infinity`) and division by zero return HTTP 400:

```bash
curl "http://localhost:8080/api/v1/divide?a=1&b=0"
```

```json
{
  "timestamp": "2026-10-29T16:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Division by zero is not allowed",
  "path": "/api/v1/divide"
}
```

## Project structure

```
src/main/java/com/spe/calculator/
├── CalculatorApplication.java      # Spring Boot entry point
├── controller/                     # REST endpoints
├── service/                        # arithmetic + validation
├── dto/                            # JSON response records
└── exception/                      # error types and global handler
src/test/java/com/spe/calculator/   # JUnit 5 unit tests + MockMvc integration tests
```

## Roadmap

- [x] Spring Boot API with validation and JUnit tests
- [ ] Multi-stage Dockerfile
- [ ] Docker Compose deployment with Nginx reverse proxy
- [ ] Jenkins declarative pipeline with GitHub trigger and email notifications
