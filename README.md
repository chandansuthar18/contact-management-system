 Contact Management System

A full-stack web-based Contact Management System built using Java Spring Boot and React.js.  
This application allows users to securely manage their contacts with features like authentication, CRUD operations, search/filter, and profile management.

Project Overview

The Contact Management System enables users to:

- Register and log in securely
- Create, update, delete, and view contacts
- Search and filter contacts efficiently
- Manage profile and change password
- Access data through a responsive React.js UI

The backend is developed using Spring Boot with JPA/Hibernate, and the frontend uses React.js for a modern user experience.

Tech Stack

### Backend
- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- SQL Server
- Slf4J & Logback (Logging)
- JUnit & Mockito (Testing)

### Frontend
- React.js
- HTML/CSS/JavaScript
- Axios (API calls)

### Tools & DevOps
- Git & GitHub
- SonarQube (Code Quality Analysis)

## Key Features

### Authentication & Authorization
- User registration (email/phone)
- Secure login system
- Change password functionality
- Session-based access control

### Contact Management
- Add new contacts
- Update existing contacts
- Delete contacts with confirmation
- View contact details
- Paginated contact list
- Search & filter by name

Each contact includes:
- First Name
- Last Name
- Title
- Email Addresses (work, personal, etc.)
- Phone Numbers (home, work, personal, etc.)

## Logging
- Implemented using Slf4J + Logback
- Logs important events, errors, and user activities

## Exception Handling
- Global exception handling using Spring Boot
- User-friendly error messages
- Proper logging of exceptions

## Testing
- Unit testing with JUnit
- Mocking using Mockito
- Covers service, controller, and repository layers


## SonarQube Integration
- Code quality analysis
- Detection of bugs, vulnerabilities, and code smells
- Enforced coding standards for Java and JavaScript


## Application Screens

### Login & Registration
- Login form
- Registration form
- Redirect to dashboard after success

### Contact Dashboard
- Paginated contact list
- Create / Update / Delete contacts (via modals)
- Search & filter functionality

### User Profile
- View user details
- Change password
- Logout functionality

## Setup Instructions

### 1. Clone Repository
```bash
git clone https://github.com/your-username/contact-management-system.git
2. Backend Setup (Spring Boot)
cd backend
mvn clean install
mvn spring-boot:run
3. Frontend Setup (React.js)
cd frontend
npm install
npm start
4. Database Setup
Create SQL Server database
Update application.properties with DB credentials
Project Structure
contact-management-system/

├── backend/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── config/

├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   └── services/

└── README.md
Future Enhancements
Export/Import contacts (CSV/Excel)
Two-factor authentication
Advanced filtering (tags, groups)
Mobile application version
Dark mode UI
Author

Made by Chandan Kumar
Java & Android Developer
