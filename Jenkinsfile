pipeline {
    agent any

    options {
        disableConcurrentBuilds()
    }

    stages {
        stage('Test') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw -B test'
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
                sh 'docker build -t docker-demo:jenkins-$BUILD_NUMBER .'
            }
        }

        stage('Deploy local') {
            steps {
                sh '''
                    if docker container inspect docker-demo-deployed >/dev/null 2>&1; then
                        docker rm -f docker-demo-deployed
                    fi

                    docker run -d \
                        --name docker-demo-deployed \
                        -p 127.0.0.1:8080:8080 \
                        docker-demo:jenkins-$BUILD_NUMBER
                '''
            }
        }
        stage('Verify application') {
    steps {
        sh '''
            ready=false

            for attempt in $(seq 1 30); do
                if docker exec docker-demo-deployed \
                    bash -c 'exec 3<>/dev/tcp/127.0.0.1/8080
                    printf "GET /hello HTTP/1.1\\r\\nHost: localhost\\r\\nConnection: close\\r\\n\\r\\n" >&3
                    head -n 1 <&3' \
                    | grep -q 'HTTP/1.1 200'; then
                    ready=true
                    break
                fi

                sleep 2
            done

            if [ "$ready" != true ]; then
                docker logs --tail 50 docker-demo-deployed
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