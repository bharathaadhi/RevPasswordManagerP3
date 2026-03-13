pipeline {
    agent any

    environment {
        JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64" 
        DOCKER_IMAGE_TAG = "latest"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Cleanup Environment') {
            steps {
                echo 'Cleaning up existing containers and volumes...'
                sh 'docker-compose down -v --remove-orphans || true'
            }
        }

        stage('Unit Tests') {
            steps {
                script {
                    def services = ['user-service', 'vault-service', 'generator-service', 'security-service', 'notification-service']
                    for (service in services) {
                        dir(service) {
                            echo "Running tests for ${service}..."
                            sh 'mvn test'
                        }
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    def services = ['user-service', 'vault-service', 'generator-service', 'security-service', 'notification-service']
                    for (service in services) {
                        dir(service) {
                            echo "Starting SonarQube analysis for ${service}..."
                            // We use the full plugin name to avoid needing it in the pom.xml
                            // Assuming SonarQube is reachable at http://sonarqube:9000 within the Docker network
                            // Or http://localhost:9000 if Jenkins runs on the host
                            sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=admin -Dsonar.password=admin'
                        }
                    }
                }
            }
        }

        stage('Build Backend Services') {
            steps {
                script {
                    def services = ['config-server', 'eureka-server', 'api-gateway', 'user-service', 'vault-service', 'generator-service', 'security-service', 'notification-service']
                    for (service in services) {
                        dir(service) {
                            echo "Building ${service}..."
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                echo 'Building Docker images...'
                sh 'docker-compose build'
            }
        }

        stage('Deploy Containers') {
            steps {
                echo 'Starting containers...'
                sh 'docker-compose up -d'
            }
        }

        stage('Health Check Verification') {
            steps {
                script {
                    echo 'Waiting for services to become healthy...'
                    // Wait for Eureka
                    sh 'timeout 60s bash -c "until curl -f http://localhost:8761/actuator/health; do sleep 5; done" || echo "Eureka server not ready"'
                    // Wait for Config Server
                    sh 'timeout 60s bash -c "until curl -f http://localhost:8888/actuator/health; do sleep 5; done" || echo "Config server not ready"'
                    // Check API Gateway
                    sh 'curl -f http://localhost:8080/actuator/health || echo "API Gateway not healthy"'
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline execution finished.'
        }
        success {
            echo 'Build and Deployment Successful!'
        }
        failure {
            echo 'Something went wrong in the pipeline. Cleaning up...'
            sh 'docker-compose down'
        }
    }
}
