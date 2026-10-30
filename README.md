# Calculator DevOps

A Calculator REST API built with Java (Spring Boot + Maven), wrapped in an end-to-end CI/CD pipeline
using Git/GitHub, Jenkins, Docker and Docker Compose.

CSE 816 – Software Production Engineering, Mini-Project.

- **GitHub:** https://github.com/Bhavzzzzzz/calculator-devops
- **Docker Hub:** https://hub.docker.com/r/bhavyajain1098/calculator

## Prerequisites

- JDK 17+
- No local Maven install needed — the project ships with the Maven Wrapper (`mvnw` / `mvnw.cmd`).

## Build, test and run

```bash
./mvnw clean package -DskipTests   # build the executable JAR -> target/calculator.jar
./mvnw test                        # run unit + integration tests (reports in target/surefire-reports)
./mvnw verify                      # tests + JaCoCo coverage gate (min 80% line coverage)
java -jar target/calculator.jar    # start the API on http://localhost:8080
```

The JaCoCo coverage report is written to `target/site/jacoco/index.html`.

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
{ "operation": "add", "a": 2, "b": 3, "result": 5 }
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

## Docker

The multi-stage [Dockerfile](Dockerfile) builds the JAR in a Maven/JDK 17 stage and copies only the JAR into a
slim `eclipse-temurin:17-jre-alpine` runtime that runs as a non-root user, with a built-in health check.

```bash
docker pull bhavyajain1098/calculator:latest            # or build locally: docker build -t bhavyajain1098/calculator .
docker run -d -p 8080:8080 bhavyajain1098/calculator
curl "http://localhost:8080/api/v1/add?a=2&b=3"
```

## Docker Compose deployment

[docker-compose.yml](docker-compose.yml) brings up the full stack on a dedicated `calc-net` bridge network:

```
client ──► nginx :80 ──► calculator replica 1 :8080
                    └──► calculator replica 2 :8080
```

- **calculator** – 2 replicas of the API, health-checked via `/actuator/health`, `restart: unless-stopped`
- **nginx** – reverse proxy that load-balances across the replicas; starts only once the API is healthy

```bash
docker compose up -d --build        # build and launch the stack
docker compose ps                   # all three containers should be (healthy)
curl -i "http://localhost/api/v1/multiply?a=6&b=7"   # X-Served-By header shows which replica answered
docker compose down                 # stop everything
```

| Variable       | Default      | Purpose                                          |
|----------------|--------------|--------------------------------------------------|
| `DOCKER_IMAGE` | `bhavyajain1098/calculator` | Docker Hub image name             |
| `IMAGE_TAG`    | `latest`     | Image tag (Jenkins sets this to the build number) |
| `HTTP_PORT`    | `80`         | Host port Nginx is published on                  |

## Project structure

```
nginx/nginx.conf                    # reverse-proxy config
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
- [x] Multi-stage Dockerfile
- [x] Docker Compose deployment with Nginx reverse proxy
- [ ] Jenkins declarative pipeline with GitHub trigger and email notifications
