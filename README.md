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

## CI/CD pipeline (Jenkins)

The declarative [Jenkinsfile](Jenkinsfile) runs on every push to GitHub:

```
GitHub push ─► Checkout ─► Build ─► Test ─► Docker Build ─► Push ─► Deploy ─► email
                           mvnw     mvnw     :BUILD_NUMBER   Docker   compose up
                           package  verify   + :latest       Hub      + health check
```

| Stage        | What it does                                                                                   |
|--------------|------------------------------------------------------------------------------------------------|
| Checkout     | Pulls the latest commit from GitHub                                                            |
| Build        | `./mvnw clean package -DskipTests`                                                             |
| Test         | `./mvnw verify` – tests + 80% coverage gate; publishes JUnit results and the JaCoCo report     |
| Docker Build | Builds `bhavyajain1098/calculator` tagged with the build number and `latest`                   |
| Push         | Logs in with the `dockerhub-creds` Jenkins credential and pushes both tags                     |
| Deploy       | `docker compose up -d --wait`, then checks `/actuator/health` through Nginx                    |

The pipeline is triggered by a GitHub webhook (`githubPush`) with `pollSCM` every 2 minutes as a fallback,
and emails the build result (success or failure) to `NOTIFY_EMAIL`. Re-runs are safe: Compose only recreates
containers whose image or config changed.

### Running Jenkins

Jenkins runs as a container that uses the host's Docker daemon through the mounted socket:

```bash
docker compose -f jenkins/docker-compose.yml up -d --build      # Jenkins on http://localhost:8080
docker compose -f jenkins/docker-compose.yml exec jenkins \
  cat /var/jenkins_home/secrets/initialAdminPassword           # first-run unlock password
```

One-time setup in the Jenkins UI:

1. Unlock Jenkins, choose **Select plugins to install → None** (the required plugins are pre-installed), create an admin user.
2. **Manage Jenkins → Credentials → Global → Add**: *Username with password*, ID `dockerhub-creds`,
   username = Docker Hub user, password = a Docker Hub access token.
3. **Manage Jenkins → System**:
   - *Global properties → Environment variables*: add `NOTIFY_EMAIL` = address for build notifications.
   - *Extended E-mail Notification*: SMTP server `smtp.gmail.com`, port `465`, *Use SSL*, credentials =
     Gmail address + a Gmail **app password**. Set the same under *E-mail Notification* to send a test mail.
4. **New Item → Pipeline**: *Pipeline script from SCM* → Git → `https://github.com/Bhavzzzzzz/calculator-devops.git`,
   branch `*/main`, script path `Jenkinsfile`. Run **Build Now** once so the triggers are registered.
5. Optional webhook: expose Jenkins (e.g. `ngrok http 8080`) and add
   `https://<public-url>/github-webhook/` under the GitHub repo's *Settings → Webhooks*.

## Project structure

```
Jenkinsfile                         # declarative CI/CD pipeline
jenkins/                            # Jenkins controller image + compose file
nginx/                              # reverse-proxy image and config
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
- [x] Jenkins declarative pipeline with GitHub trigger and email notifications
