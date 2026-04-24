# AI-Powered Smart Contact Management System

This project is a full-stack contact management system built with Java Spring Boot, Oracle Database, and a vanilla HTML/CSS/JS frontend.

## Project Architecture
The project follows a **Monolithic Architecture**:
- **Backend**: Spring Boot 3.2.4 (Java 17)
- **Database**: Oracle Database (via JPA/Hibernate)
- **Frontend**: Vanilla HTML5, CSS3, and JavaScript, served as static resources by the Spring Boot server.

This architecture eliminates CORS issues and simplifies deployment by serving everything from a single port (8080).

## Prerequisites
- **Java 17 LTS**: Required to run the Spring Boot application.
- **Oracle Database**: A running instance of Oracle (e.g., 19c, 21c, or XE).
- **Maven**: (Optional if using a wrapper) Required to build and run the project.

## Installation & Setup

### 1. Database Configuration
Open `backend/src/main/resources/application.properties` and update the following lines with your Oracle credentials:

```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 2. Build and Run
Navigate to the `backend` directory and run:

```bash
mvn spring-boot:run
```

If you don't have Maven installed, please install it from [maven.apache.org](https://maven.apache.org/download.cgi).

### 3. Access the Application
Once the server starts, open your browser and go to:
[http://localhost:8080/index.html](http://localhost:8080/index.html)

## Folder Structure
- `backend/`: Contains all Spring Boot source code and configurations.
  - `src/main/java/com/smartcontact/`: Backend logic (Controllers, Services, Repositories, Models).
  - `src/main/resources/static/`: Frontend files (HTML, CSS, JS).
  - `src/main/resources/application.properties`: Configuration file.

## Troubleshooting
- **Connection Refused**: Ensure Oracle DB is running and accessible on port 1521.
- **Table Not Found**: The application is configured with `ddl-auto=update`, which should create tables automatically. Ensure your DB user has sufficient permissions.
- **Port 8080 occupied**: Change `server.port` in `application.properties` if 8080 is already in use.

## Production Readiness Audit
The following areas are currently implemented as "AI-generated placeholders" and should be reviewed for production use:
1. **Security**: No authentication/authorization is implemented. Use Spring Security for production.
2. **CORS**: Currently configured to allow all origins in `SmartContactApplication.java`. Restrict this to your specific domain.
3. **Validation**: Basic `@Valid` annotations are missing on many REST endpoints.
4. **Error Handling**: No global exception handler. REST responses might expose stack traces on error.
5. **Categorization Logic**: The "AI Categorization" is currently a simple string-matching algorithm in `ContactService.java`. For true AI, integrate with an LLM or NLP service.
6. **Relationship Scoring**: Hardcoded logic in `ContactService.java`. This should be more dynamic and based on actual interaction frequency and sentiment.
