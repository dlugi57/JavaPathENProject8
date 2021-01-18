# TourGuide Application

## Overview:
TourGuide is a Spring Boot application of TripMaster's applications. It allows users to discover attractions near of their location and provides them discounts on hotel stays and reductions on ticket prices for shows.

## Prerequisite to run it

- Java 1.8 JDK (or +)
- Gradle 6.6.1 (or +)
- Docker

## Run app (on local port 8080)

Gradle
```
gradle bootRun
```

Spring Boot
```
mvn spring-boot:run (run app)
mvn spring-boot:stop (stop app)
```

## API documentation

[POSTMAN](https://documenter.getpostman.com/view/11619210/TVzXBaa9)

## Testing

The app has unit tests and integration tests written. <br/>
You must launch `gradle test`
