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
}
    }

    post {
        success {
            archiveArtifacts artifacts: 'target/*.jar',
                             fingerprint: true
        }
    }
}