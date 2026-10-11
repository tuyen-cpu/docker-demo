pipeline {
    agent any

    options {
        disableConcurrentBuilds()
    }

    stages {
        stage('Test') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw -B clean test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml',
                          allowEmptyResults: true
                }
            }
        }

        stage('Package') {
            steps {
                sh './mvnw -B package -DskipTests'
            }
        }

        stage('Build Docker image') {
            steps {
                sh 'docker build -f Dockerfile.jenkins -t docker-demo:jenkins-$BUILD_NUMBER .'
            }
        }

        stage('Deploy and verify') {
            steps {
                sh '''
                    set -eu

                    APP=docker-demo-deployed
                    NEW_IMAGE="docker-demo:jenkins-$BUILD_NUMBER"
                    OLD_IMAGE=""

                    if docker container inspect "$APP" >/dev/null 2>&1; then
                        OLD_IMAGE=$(docker inspect --format '{{.Image}}' "$APP")
                    fi

                    start_app() {
                        docker run -d \
                            --name "$APP" \
                            -p 127.0.0.1:8080:8080 \
                            "$1"
                    }

                    verify_app() {
                        for attempt in $(seq 1 30); do
                            if docker exec "$APP" \
                                timeout 5 bash -c '
                                    exec 3<>/dev/tcp/127.0.0.1/8080
                                    printf "GET /hello HTTP/1.1\\r\\nHost: localhost\\r\\nConnection: close\\r\\n\\r\\n" >&3
                                    head -n 1 <&3
                                ' 2>/dev/null | grep -q 'HTTP/1.1 200'; then
                                return 0
                            fi

                            sleep 2
                        done

                        return 1
                    }

                    if docker container inspect "$APP" >/dev/null 2>&1; then
                        docker rm -f "$APP"
                    fi

                    if start_app "$NEW_IMAGE" && verify_app; then
                        echo "Deploy thanh cong: $NEW_IMAGE"
                    else
                        echo "Ban moi that bai."
                        docker logs --tail 50 "$APP" || true

                        if docker container inspect "$APP" >/dev/null 2>&1; then
                            docker rm -f "$APP"
                        fi

                        if [ -n "$OLD_IMAGE" ]; then
                            echo "Rollback ve image: $OLD_IMAGE"

                            if start_app "$OLD_IMAGE" && verify_app; then
                                echo "Rollback thanh cong."
                            else
                                echo "Rollback that bai, can kiem tra thu cong."
                                docker logs --tail 50 "$APP" || true
                            fi
                        else
                            echo "Khong co image cu de rollback."
                        fi

                        exit 1
                    fi
                '''
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'target/*.jar',
                             fingerprint: true
        }
    }
}