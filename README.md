# README

1. Clone the repository:
   `git clone https://github.com/dtrajkoski04/mtcg.git`
2. Configure the Docker Container:
   - `docker pull postgres:latest`
   - `docker run --name TOUR -e POSTGRES_USER=TOUR -e POSTGRES_PASSWORD=TOUR -e POSTGRES_DB=TOUR -p 5432:5432 -d postgres:latest`
   - `docker exec -it TOUR psql -U TOUR -d TOUR`
4. Or use your own Docker Container and change the credentials in the `application.properties`
5. Run the Backend with: `mvn spring-boot:run`
6. Run the Frontend with: `ng serve`