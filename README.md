🛡️ RevPasswordManager

RevPasswordManager is a secure, robust, and highly available microservices-based password management application built using Spring Boot and Spring Cloud.

It allows users to:

Securely store vault credentials

Automatically evaluate password strength

Generate highly complex passwords

Protect all sensitive information using a Master Password

The application is designed with scalability, security, and modular architecture in mind.

🏗️ Architecture Stack

The application uses a modern microservices architecture to ensure scalability, maintainability, and separation of concerns.

Backend Framework

Java 17

Spring Boot 3.2.3

Microservice Infrastructure

Spring Cloud Gateway

Netflix Eureka

Database

MySQL / MariaDB

Spring Data JPA

Security

Spring Security

JSON Web Tokens (JWT)

Communication Between Services

OpenFeign Clients

Testing

JUnit

Mockito

📦 Microservices Configuration

The system is composed of several independent microservices.

config-server (Port: 8888)
Centralized configuration management for all microservices.

eureka-server (Port: 8761)
Service discovery registry where all microservices register themselves.

api-gateway (Port: 8080)
Acts as the single entry point for frontend clients. Handles routing, rate limiting, and cross-origin requests.

user-service (Port: 8081)
Handles authentication, user registration, JWT generation, and Master Password verification.

security-service (Port: 8082)
Evaluates password strength and performs security audits.

vault-service (Port: 8083)
Manages AES-encrypted password vault entries including creation, retrieval, and updates.

generator-service (Port: 8084)
Generates complex passwords dynamically based on user-defined requirements such as length, symbols, and uppercase characters.

notification-service (Port: 8085)
Handles asynchronous notifications such as emails and security alerts.

✨ Key Features

Global Exception Handling

Provides consistent and clean ErrorResponse HTTP responses across all APIs.

Microservice Communication

Uses OpenFeign for seamless and secure inter-service communication.

Unit Testing Coverage

Service layers are tested using JUnit and Mockito.

Security Validation

Implements JWT token validation with proper HTTP status codes:

401 Unauthorized

403 Forbidden

Password Generation

Allows users to generate strong passwords with customizable parameters such as:

Length

Symbols

Uppercase characters

🚀 How to Run Locally
Prerequisites

Ensure the following are installed on your system:

JDK 17

Maven

MySQL / MariaDB

Also make sure your database credentials are configured correctly in the application properties.

⚙️ Application Startup Order

To ensure proper service registration and communication, start the services in the following order:

1. Config Server

Start the Config Server and wait until the console shows:

Started ConfigServerApplication
2. Eureka Server

Start the Eureka Server and wait until:

Started EurekaServerApplication
3. API Gateway

Start the API Gateway and confirm:

Started ApiGatewayApplication
4. Remaining Microservices

Now start the remaining services in any order:

user-service

security-service

vault-service

generator-service

notification-service

📊 Service Monitoring

After starting all services, open the Eureka Dashboard:

http://localhost:8761

You should see all services registered and displaying UP status.