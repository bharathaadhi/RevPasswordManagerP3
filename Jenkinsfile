pipeline {
    agent any

    environment {
        DOCKER_IMAGE_TAG = "latest"
    }

    stages {
        stage('Cleanup Environment') {
            steps {
                echo 'Cleaning up existing containers and volumes...'
                bat 'docker-compose down -v --remove-orphans || cmd /c "exit /b 0"'
            }
        }

        stage('Unit Tests') {
            steps {
                script {
                    def services = ['user-service', 'vault-service', 'generator-service', 'security-service', 'notification-service']
                    for (service in services) {
                        dir(service) {
                            echo "Running tests for ${service}..."
                            bat 'mvn test'
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
                            bat 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=admin -Dsonar.password=admin'
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
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                echo 'Building Docker images...'
                bat 'docker-compose build'
            }
        }

        stage('Deploy Containers') {
            steps {
                echo 'Starting containers...'
                bat 'docker-compose up -d'
            }
        }

        stage('Health Check Verification') {
            steps {
                script {
                    def checkHealth = { url, name ->
                        echo "Waiting for ${name} at ${url}..."
                        for (int i = 0; i < 12; i++) {
                            def result = bat(script: "curl -f ${url}", returnStatus: true)
                            if (result == 0) {
                                echo "${name} is healthy!"
                                return true
                            }
                            echo "${name} not ready, retrying in 5s... (${i+1}/12)"
                            sleep 5
                        }
                        echo "${name} failed health check!"
                        return false
                    }
                    checkHealth('http://localhost:8761/actuator/health', 'Eureka')
                    checkHealth('http://localhost:8888/actuator/health', 'Config Server')
                    checkHealth('http://localhost:8080/actuator/health', 'API Gateway')
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
            bat 'docker-compose down'
        }
    }
}
