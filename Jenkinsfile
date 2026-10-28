// Declarative CI/CD pipeline: Checkout -> Build -> Test -> Docker Build -> Push -> Deploy
//
// Jenkins prerequisites (see README "Jenkins setup"):
//   - Credentials: "Username with password" with ID 'dockerhub-credentials'
//                  (Docker Hub username + an access token as the password)
//   - Plugins:     Git, GitHub, Pipeline, JUnit, Credentials Binding, Email Extension,
//                  HTML Publisher, Timestamper
//   - Email:       SMTP configured under Manage Jenkins -> System -> Extended E-mail Notification

pipeline {
    agent any

    options {
        skipDefaultCheckout(true)         // checkout happens explicitly in the Checkout stage
        disableConcurrentBuilds()         // never run two deployments at the same time
        buildDiscarder(logRotator(numToKeepStr: '15'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    triggers {
        // Primary: GitHub webhook (repo Settings -> Webhooks -> <jenkins-url>/github-webhook/)
        githubPush()
        // Fallback: poll every ~2 min, for when GitHub cannot reach Jenkins (e.g. localhost without a tunnel)
        pollSCM('H/2 * * * *')
    }

    environment {
        // Jenkins on macOS (Homebrew/launchd) starts with a minimal PATH; Docker Desktop lives in /usr/local/bin
        PATH = "/usr/local/bin:/opt/homebrew/bin:${env.PATH}"

        IMAGE_NAME   = 'calculator-devops'
        IMAGE_TAG    = "${env.BUILD_NUMBER}"
        NOTIFY_EMAIL = 'agam.roy@iiitb.ac.in'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                }
                echo "Building commit ${env.GIT_COMMIT_SHORT}"
            }
        }

        stage('Build') {
            steps {
                // Maven wrapper: the agent only needs a JDK, not a Maven install
                sh './mvnw -B clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                // Non-zero exit on any test failure (or coverage below 80%) fails the build here
                sh './mvnw -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    publishHTML(target: [
                        reportName           : 'JaCoCo Coverage',
                        reportDir            : 'target/site/jacoco',
                        reportFiles          : 'index.html',
                        keepAll              : true,
                        alwaysLinkToLastBuild: true,
                        allowMissing         : true
                    ])
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t "$IMAGE_NAME:$IMAGE_TAG" -t "$IMAGE_NAME:latest" .'
            }
        }

        stage('Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials',
                                                  usernameVariable: 'DOCKERHUB_USER',
                                                  passwordVariable: 'DOCKERHUB_TOKEN')]) {
                    sh '''
                        # Log in using a throwaway Docker config directory, so the Jenkins credentials
                        # never touch (or log out) the agent's own Docker Desktop login. The daemon
                        # address is read first because it is stored in the normal config.
                        export DOCKER_HOST="$(docker context inspect --format '{{.Endpoints.docker.Host}}')"
                        export DOCKER_CONFIG="$(mktemp -d)"
                        trap 'rm -rf "$DOCKER_CONFIG"' EXIT

                        echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USER" --password-stdin

                        # Docker Hub namespace comes from the credential, so no username is hard-coded
                        REPO="$DOCKERHUB_USER/$IMAGE_NAME"
                        for TAG in "$IMAGE_TAG" latest; do
                            docker tag "$IMAGE_NAME:$IMAGE_TAG" "$REPO:$TAG"
                            docker push "$REPO:$TAG"
                        done
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                // The calculator image (calculator-devops:$IMAGE_TAG) was built above; only the
                // proxy image is built here so a change to nginx.conf is picked up
                sh 'docker compose build nginx'

                // Idempotent: Compose only recreates containers whose image or config changed.
                // --wait blocks until every container's health check passes, and fails otherwise.
                sh 'docker compose up -d --remove-orphans --wait --wait-timeout 180'

                // End-to-end smoke test through the proxy. Retries cover nginx's short DNS cache
                // still pointing at the replaced containers for a few seconds.
                sh '''
                    for i in $(seq 1 10); do
                        if docker compose exec -T nginx wget -qO- "http://127.0.0.1/api/v1/add?a=2&b=3" | grep -q '"result":5}'; then
                            echo "Smoke test passed"
                            exit 0
                        fi
                        echo "Smoke test attempt $i failed, retrying..."
                        sleep 3
                    done
                    exit 1
                '''
                sh 'docker compose ps'
            }
            post {
                failure {
                    sh 'docker compose ps; docker compose logs --tail=100'
                }
            }
        }
    }

    post {
        always {
            notifyByEmail()
        }
    }
}

def notifyByEmail() {
    def status = currentBuild.currentResult
    emailext(
        to: env.NOTIFY_EMAIL,
        subject: "${status}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
        mimeType: 'text/html',
        attachLog: status != 'SUCCESS',
        body: """
            <p>Pipeline <b>${env.JOB_NAME} #${env.BUILD_NUMBER}</b> finished with status <b>${status}</b>.</p>
            <ul>
              <li>Commit: ${env.GIT_COMMIT_SHORT ?: 'n/a'}</li>
              <li>Image: ${env.IMAGE_NAME}:${env.IMAGE_TAG}</li>
              <li>Duration: ${currentBuild.durationString.replace(' and counting', '')}</li>
              <li><a href="${env.BUILD_URL}console">Console output</a> &middot;
                  <a href="${env.BUILD_URL}testReport">Test results</a></li>
            </ul>
        """
    )
}
