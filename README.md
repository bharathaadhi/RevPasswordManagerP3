# Rev Password Manager

**Rev Password Manager** is a secure, scalable, and highly available **microservices-based password management application** built using **Spring Boot and Spring Cloud**.

The system allows users to securely store account credentials, generate strong passwords, and evaluate password strength while ensuring protection through a **Master Password and JWT-based authentication**.

The application follows a **modern microservices architecture**, ensuring modular design, scalability, and secure service communication.

---

## Project Overview

Rev Password Manager enables users to:

- Securely store credentials in an encrypted password vault  
- Automatically evaluate password strength  
- Generate highly complex passwords  
- Protect vault data using a master password  
- Manage passwords across multiple accounts securely  

The system is designed with **enterprise-grade security practices**, including encryption, token-based authentication, and modular service architecture.

---

## Key Features

### Authentication & Security

- Secure User Registration and Login  
- JWT-based Authentication and Authorization  
- Master Password Protection  
- Two-Factor Authentication (2FA) Support  
- Secure API Authorization  
- HTTP Security Status Handling (401 / 403)

---

### Password Vault Management

- Secure storage of user credentials  
- AES encryption for vault data  
- Create new vault entries  
- Retrieve stored passwords securely  
- Update vault credentials  
- Delete stored passwords safely  

---

### Password Generation

- Generate strong passwords dynamically  
- Customizable password parameters  
- Adjustable password length  
- Uppercase and symbol inclusion  
- Automatic password complexity handling  

---

### Security Audit

- Password strength evaluation  
- Weak password detection  
- Internal security validation  

---

### Notification System

- Email notifications for security alerts  
- Account activity notifications  
- Asynchronous notification processing  

---

## Tech Stack

### Frontend

| Technology | Usage |
|------------|------|
| Angular | User Interface |
| Angular Router | Application Navigation |
| HttpClient | API Communication |
| JWT Interceptor | Secure API Requests |
| Custom CSS | UI Styling |

---

### Backend

| Technology | Usage |
|------------|------|
| Java 17 | Programming Language |
| Spring Boot 3.2.3 | Backend Framework |
| Spring Security | Authentication |
| JWT | Authorization |
| Spring Data JPA | ORM |
| Hibernate | Persistence |
| MySQL / MariaDB | Database |
| BCrypt | Password Hashing |
| AES Encryption | Vault Security |
| OpenFeign | Microservice Communication |

---

## Microservices Architecture

The application consists of several independent microservices working together.

| Service | Port | Description |
|--------|------|-------------|
| config-server | 8888 | Centralized configuration management |
| eureka-server | 8761 | Service discovery registry |
| api-gateway | 8080 | Entry point for frontend clients |
| user-service | 8081 | User authentication and JWT generation |
| security-service | 8082 | Password strength evaluation |
| vault-service | 8083 | AES encrypted password vault storage |
| generator-service | 8084 | Password generation service |
| notification-service | 8085 | Email and alert notifications |

---

## Architecture

The backend follows a **microservices architecture** where each service performs a dedicated responsibility.

System flow:

Frontend → API Gateway → Eureka Service Discovery → Microservices → Database

Implemented using:

- Spring Cloud Config Server  
- Netflix Eureka Service Registry  
- Spring Cloud Gateway Routing  
- OpenFeign Inter-service Communication  

---

## Project Structure

### Backend Structure


src/main/java/com/rev
│
├── config-server
├── eureka-server
├── api-gateway
│
├── user-service
├── security-service
├── vault-service
├── generator-service
└── notification-service


---

### Frontend Structure


src/app
│
├── core
│ ├── services
│ ├── guards
│ └── interceptors
│
├── features
│ ├── auth
│ ├── dashboard
│ ├── vault
│ ├── generator
│ └── profile
│
└── shared


---

## Setup and Installation

### Prerequisites

Ensure the following are installed:

- Java JDK 17+
- Maven
- Node.js
- Angular CLI
- MySQL / MariaDB
- IntelliJ IDEA or VS Code

---

## Backend Setup

Navigate to the project directory:


cd RevPasswordManagerP3


---

## Configuration Management

This project uses **Spring Cloud Config Server** for centralized configuration management.

Instead of storing configuration inside each microservice, all configuration files are placed inside the **Config Server** under the `configs` directory.

### Config Server Structure


config-server
└── src/main/resources
└── configs
├── api-gateway.properties
├── user-service.properties
├── security-service.properties
├── vault-service.properties
├── generator-service.properties
└── notification-service.properties


Each microservice loads its configuration from the **Config Server** during application startup.

Example microservice configuration:


spring.application.name=user-service
spring.config.import=optional:configserver:http://localhost:8888

eureka.client.service-url.defaultZone=http://localhost:8761/eureka


---

## Environment Variables (.env)

To protect sensitive information such as database passwords, the project uses a **`.env` file**.

Database credentials are **not stored directly in configuration files**.

Example `.env` file:


DB_PASSWORD=your_database_password


The `.env` file is excluded from version control using `.gitignore`.


.env


This ensures that sensitive credentials are **never exposed in the repository**.

---

## Database Configuration

Database passwords are injected into configuration files using **environment variables**.

Example configuration from `user-service.properties`:


spring.datasource.url=jdbc:mysql://localhost:3306/user_service_db
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true


Using environment variables ensures:

- Sensitive credentials are not stored in source code  
- Database passwords remain secure  
- Configuration is portable across environments  

---

## Config Server Endpoint

The Config Server runs on:


http://localhost:8888


You can verify configuration loading by opening:


http://localhost:8888/user-service/default


This endpoint returns the configuration properties for the **user-service**.

---

## Run Backend Services

Run each microservice using Maven or IntelliJ.


mvn spring-boot:run


---

## Startup Order

To ensure correct service registration, start the services in the following order.

### 1️⃣ Config Server

Wait until:


Started ConfigServerApplication


### 2️⃣ Eureka Server

Wait until:


Started EurekaServerApplication


### 3️⃣ API Gateway

Wait until:


Started ApiGatewayApplication


### 4️⃣ Remaining Services

- user-service  
- security-service  
- vault-service  
- generator-service  
- notification-service  

---

## Service Monitoring

Open the **Eureka Dashboard**:


http://localhost:8761


You should see all services registered with status:


UP


---

## Frontend Setup

Navigate to the frontend directory:


cd FrontEnd


### Install Dependencies


npm install


### Run the Application


ng serve


---

## Open Browser


http://localhost:4200


---

## Important API Endpoints

| Method | Endpoint | Description |
|------|-----------|-------------|
| POST | /api/auth/login | User Login |
| POST | /api/auth/register | Register User |
| GET | /api/vault | Retrieve Vault Data |
| POST | /api/vault | Add Password |
| PUT | /api/vault/{id} | Update Password |
| POST | /api/generator | Generate Password |
| POST | /api/security/check | Check Password Strength |

---

## Security Implementation

- AES Encryption for vault storage  
- JWT protected APIs  
- Master password verification  
- Secure service-to-service communication  
- HTTP authentication status handling  

---

## Testing Performed

- Authentication Flow  
- Vault CRUD Operations  
- Password Generation  
- Security Evaluation  
- Microservice Communication  

---

## Future Enhancements

- Cloud Backup Integration  
- Browser Extension Support  
- Password Auto-fill  
- Biometric Authentication  
- Mobile Application  

---
