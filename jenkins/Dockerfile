FROM jenkins/jenkins:lts-jdk17

USER root
# Docker CLI + buildx/compose plugins; the daemon is the host's, reached through the mounted socket
COPY --from=docker:28-cli /usr/local/bin/docker /usr/local/bin/docker
COPY --from=docker:28-cli /usr/local/libexec/docker/cli-plugins /usr/local/libexec/docker/cli-plugins
USER jenkins

# Plugins used by the Jenkinsfile, pre-installed so the setup wizard can be skipped straight to "Select plugins -> None"
RUN jenkins-plugin-cli --plugins \
    workflow-aggregator \
    git \
    github \
    credentials-binding \
    junit \
    jacoco \
    email-ext \
    timestamper \
    ws-cleanup \
    pipeline-stage-view
