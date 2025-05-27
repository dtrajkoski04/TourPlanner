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


## Frontend Architecture


## Backend Architecture

```plaintext
Backend
│
├── controller
│   └── TourController.java
│   └── TourLogController.java
│
├── service
│   ├── TourService.java
│   ├── TourLogService.java
│   │
│   ├── impl
│   │   └── TourServiceImpl.java
│   │   └── TourLogServiceImpl.java
│   │
│   ├── dto
│   │   └── TourDto.java
│   │   └── TourLogDto.java
│   │
│   └── mapper
│       └── TourMapper.java
│       └── TourLogMapper.java
│
└── persistence
    ├── entity
    │   └── Tour.java
    │   └── TourLog.java
    │
    └── repository
        └── TourRepository.java
        └── TourLogRepository.java
   ```


## Database Architecture

### Table: `tours`

| Column               | Datatype           | Description                                                    |
|----------------------|--------------------|----------------------------------------------------------------|
| `id`                 | `SERIAL`           | Primary key, unique identifier for each tour                   |
| `name`               | `TEXT`             | Name of the tour                                               |
| `description`        | `TEXT`             | Description of the tour                                        |
| `start_location`     | `TEXT`             | Starting point of the tour                                     |
| `end_location`       | `TEXT`             | Destination of the tour                                        |
| `transport_type`     | `TEXT`             | Means of transport (e.g. bike, hiking, etc.)                   |
| `distance`           | `DOUBLE PRECISION` | Tour distance in kilometers                                    |
| `estimated_time`     | `INTERVAL`         | Estimated duration of the tour                                 |
| `map_image_path`     | `TEXT`             | Path to the externally stored image of the route               |
| `popularity`         | `INTEGER`          | Popularity (calculated based on the number of associated logs) |
| `child_friendliness` | `DOUBLE PRECISION` | Child-friendliness rating (calculated)                         |

---

### Table: `tour_logs`

| Column           | Datatype           | Description                                                 |
|------------------|--------------------|-------------------------------------------------------------|
| `id`             | `SERIAL`           | Primary key, unique identifier for each log                 |
| `tour_id`        | `INTEGER`          | Foreign key referencing the tour (`tours.id`)               |
| `log_date`       | `TIMESTAMP`        | Date and time of the completed tour                         |
| `comment`        | `TEXT`             | User comment                                                |
| `difficulty`     | `TEXT`             | Difficulty level (e.g. easy, medium, hard)                  |
| `total_distance` | `DOUBLE PRECISION` | Actual distance covered                                     |
| `total_time`     | `INTERVAL`         | Actual time taken                                           |
| `rating`         | `INTEGER`          | Rating of the tour (e.g. on a scale from 1 to 5)            |

---

### 🔗 Relationship

- A **tour** can have **multiple tour logs** (1:n relationship).
- When a tour is deleted, all associated logs are automatically deleted (`ON DELETE CASCADE`).


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
2. Update `application.properties` with DB credentials and base paths.
3. Start the Spring Boot server: `mvn spring-boot:run`.
4. Run Angular frontend: `ng serve`.

## Reports

- **Tour Report**: Complete detail of one tour and its logs.
- **Summary Report**: Averages of time, distance, and rating across tour logs.

## Unique Feature

> To be defined: