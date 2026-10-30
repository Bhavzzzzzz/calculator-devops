pipeline {
    agent any

    triggers {
        // GitHub webhook (needs a publicly reachable Jenkins), with SCM polling as a fallback
        githubPush()
        pollSCM('H/2 * * * *')
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        DOCKER_IMAGE = 'bhavyajain1098/calculator'
        IMAGE_TAG    = "${env.BUILD_NUMBER}"
        // Host where the Compose stack is published, as seen from the Jenkins container
        DEPLOY_HOST  = 'host.docker.internal'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log -1 --oneline'
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw -B clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                // verify = unit + integration tests + JaCoCo 80% coverage gate; any failure fails the build
                sh './mvnw -B verify'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco execPattern: 'target/jacoco.exec',
                           classPattern: 'target/classes',
                           sourcePattern: 'src/main/java'
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE:$IMAGE_TAG -t $DOCKER_IMAGE:latest .'
            }
        }

        stage('Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'DOCKERHUB_USER',
                                                  passwordVariable: 'DOCKERHUB_TOKEN')]) {
                    sh '''
                        echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USER" --password-stdin
                        docker push $DOCKER_IMAGE:$IMAGE_TAG
                        docker push $DOCKER_IMAGE:latest
                    '''
                }
            }
            post {
                always {
                    sh 'docker logout || true'
                }
            }
        }

        stage('Deploy') {
            steps {
                // Re-running is safe: Compose only recreates containers whose image/config changed
                sh '''
                    docker compose build nginx
                    docker compose up -d --wait --remove-orphans
                    docker compose ps
                '''
                sh '''
                    for i in $(seq 1 20); do
                        if curl -fs "http://$DEPLOY_HOST/actuator/health" | grep -q '"UP"'; then
                            echo "Health check passed"
                            curl -fs "http://$DEPLOY_HOST/api/v1/add?a=2&b=3"
                            exit 0
                        fi
                        sleep 3
                    done
                    echo "Health check failed"
                    exit 1
                '''
            }
        }
    }

    post {
        success {
            emailext to: env.NOTIFY_EMAIL,
                     subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                     body: """Build #${env.BUILD_NUMBER} succeeded.
Image: ${env.DOCKER_IMAGE}:${env.IMAGE_TAG}
Details: ${env.BUILD_URL}"""
        }
        failure {
            emailext to: env.NOTIFY_EMAIL,
                     subject: "FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                     body: """Build #${env.BUILD_NUMBER} failed.
Console log: ${env.BUILD_URL}console""",
                     attachLog: true
        }
        always {
            // Drop old dangling layers so repeated builds don't fill the disk
            sh 'docker image prune -f || true'
        }
    }
}
