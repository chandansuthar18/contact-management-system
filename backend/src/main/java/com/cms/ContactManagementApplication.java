package com.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the Contact Management System Spring Boot application.
 *
 * STEP TO RUN:
 *   mvn spring-boot:run
 *   OR in IntelliJ: Right-click → Run 'ContactManagementApplication'
 *
 * API base URL: http://localhost:8080/api/v1
 */
@SpringBootApplication
public class ContactManagementApplication {

    private static final Logger log = LoggerFactory.getLogger(ContactManagementApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ContactManagementApplication.class, args);
        log.info("=======================================================");
        log.info("  Contact Management System Started Successfully");
        log.info("  API:  http://localhost:8080/api/v1");
        log.info("  Docs: http://localhost:8080/api/v1/swagger-ui.html");
        log.info("=======================================================");
    }
}
