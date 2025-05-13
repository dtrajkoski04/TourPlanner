# Tour Planner

A full-stack application for planning, managing, and analyzing bike, hike, running, or vacation tours. Built with Angular for the frontend, Spring Boot for the backend, and PostgreSQL for data persistence.

## Project Overview

The Tour Planner enables users to:
- Create and manage tours with route data fetched from OpenRouteService.
- Log completed tour activities with detailed statistics.
- View statistical summaries and generate PDF reports.
- Perform full-text search, including on computed attributes.
- Import/export tour data.
- Compute attributes like popularity and child-friendliness.

## Technologies Used

- **Angular** – Web-based frontend
- **Spring Boot** – Business and data access layers
- **PostgreSQL** – Persistent storage using Spring Data JPA
- **log4j** – Logging
- **OpenRouteService API + OpenStreetMap** – Route data and maps
- **Apache PDFBox / iText / similar** – PDF report generation
- **JUnit** – Unit testing
- **Jackson** – JSON serialization
- **application.yml** – External configuration

## Architecture

- **Frontend**: Angular using service-based component structure with RxJS observables and HTTP client.
- **Backend**: Spring Boot with MVVM pattern, service and repository layers.
- **Database**: PostgreSQL with JPA/Hibernate.
- **Design Patterns**: Singleton (config), Builder (Tour, TourLog), Observer (data updates with RxJS).

## Milestones

### Milestone 1: Project Setup
- Initialize Spring Boot backend with PostgreSQL integration.
- Set up Angular workspace and routing.
- Define entities: `Tour`, `TourLog`.
- Implement OpenRouteService integration for route data.

### Milestone 2: Core Features
- Implement backend REST APIs for tours and tour logs (CRUD).
- Angular components and services for managing tours and logs.
- Image upload and external storage.
- UI validation and form controls.

### Milestone 3: Search & Reporting
- Full-text search (including computed fields).
- Compute popularity and child-friendliness.
- PDF report generation (tour and summary).
- Import/export tour data (JSON or CSV format).

### Milestone 4: Finalization
- Logging with log4j.
- 20+ JUnit unit tests.
- Config management via `application.yml`.
- UI polish and responsive design.
- Documentation (UML, wireframes, protocol PDF).

## Running the Application

1. Ensure PostgreSQL is running and properly configured.
2. Update `application.yml` with DB credentials and base paths.
3. Start the Spring Boot server: `mvn spring-boot:run`.
4. Run Angular frontend: `ng serve`.

## Reports

- **Tour Report**: Complete detail of one tour and its logs.
- **Summary Report**: Averages of time, distance, and rating across tour logs.

## Unique Feature

> To be defined: