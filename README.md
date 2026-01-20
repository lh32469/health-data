# Health Data

A Spring Boot application for parsing, storing, and visualizing Apple Health export data using PrimeFaces JSF for the UI and RavenDB for data persistence.

## Overview

A running instance of the application can be found at: https://health-data.mcquarrie.cc

This application provides a web-based interface to import Apple Health export data and visualize workout metrics through interactive charts and tables. It parses Apple's XML health export format and stores the data in RavenDB for efficient querying and analysis.

## Technology Stack

- **Java 11** - Programming language
- **Spring Boot 2.6.0** - Application framework
- **PrimeFaces 4.5.6** - JSF component library for rich UI
- **RavenDB 5.4.4** - NoSQL document database
- **Jackson 2.19.0** - XML/JSON processing
- **Jasypt** - Encryption for sensitive configuration
- **Maven** - Build and dependency management
- **Lombok** - Reduce boilerplate code

## Features

- **Apple Health Data Import** - Parse and import Apple Health export XML files
- **Workout Visualization** - View workout data organized by day, month, and year
- **Interactive Charts** - Scatter plots and charts for workout metrics analysis
- **Secure Authentication** - User authentication with encrypted credentials
- **RESTful API** - REST endpoints for programmatic access
- **Actuator Monitoring** - Spring Boot Actuator endpoints for application health and metrics
- **Prometheus Integration** - Metrics export for monitoring

## Prerequisites

- Java 11 or higher
- Maven 3.x
- RavenDB instance (local or remote)
- Apple Health export data (from iOS Health app)

## Building the Application

```bash
# Clone the repository
git clone https://github.com/yourusername/health-data.git
cd health-data

# Build with Maven
mvn clean package

# Run the application
java -jar target/watch-1.0.1.jar
```

## Configuration

The application uses `application.yml` for configuration. Key settings:

- **Application Port**: 8085 (HTTP/2 enabled)
- **Management Port**: 8090 (Actuator endpoints)
- **Database**: RavenDB - database name "HealthData"
- **Max Upload Size**: 1000MB (for large Apple Health exports)

### Environment Variables

- `BRANCH` - Application instance identifier (defaults to "dev")

### Jasypt Encryption

Sensitive properties can be encrypted using the Jasypt tool:
- Algorithm: `PBEWITHHMACSHA512ANDAES_256`
- Use `JasyptEncryptorTool` test class for encrypting values

## Running the Application

```bash
mvn spring-boot:run
```

Access the application at: http://localhost:8085

Management endpoints available at: http://localhost:8090/actuator

## Project Structure

```
src/main/java/org/gpc4j/health/watch/
├── db/              # Database configuration and utilities
├── jsf/             # JSF managed beans (UI controllers)
├── repository/      # Data repositories
├── rest/            # REST API controllers
├── security/        # Security configuration
└── xml/             # XML parsing for Apple Health data

src/main/resources/
├── META-INF/resources/  # XHTML pages (JSF views)
└── application.yml      # Application configuration
```

## Usage

1. Start the application
2. Navigate to http://localhost:8085
3. Upload your Apple Health export XML file via the upload page
4. Browse workout data through the various views:
   - **Workouts** - Main dashboard
   - **Day/Month/Year** - Time-based views
   - **Scatter** - Chart analysis

## Version

Current version: **1.0.1**

## License

[Add your license information here]
