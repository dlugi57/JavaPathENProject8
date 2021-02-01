# TourGuide Application

## Overview:
TourGuide is a Spring Boot application of TripMaster's applications. It allows users to discover attractions near of their location and provides them discounts on hotel stays and reductions on ticket prices for shows.
Application is composed of three mini services TourGuide the main one, GpsUtil, and RewardCentral.
The main service depends on two of the other services.

## Prerequisite to run it

- Java 1.8 JDK (or +)
- Gradle 6.6.1 (or +)
- Docker

## Run app

Gradle
```
gradle bootRun
```

Spring Boot
```
mvn spring-boot:run (run app)
mvn spring-boot:stop (stop app)
```

## Docker deploiement:

Use the **Dockerfile** on the package root:
- `docker build -t name of image .`
- `docker run -d -p name of image`

To deploy all TourGuide microservices, use the **docker-compose.yml** on the package root

- `docker-compose up -d`

## API documentation

[POSTMAN](https://documenter.getpostman.com/view/11619210/TVzXBaa9)

## Testing

The app has unit tests and integration tests written. <br/>
You must launch `gradle test`
