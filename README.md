# Hotel Booking System (Microservices)

Состав:
- Eureka Server (порт 8761)
- API Gateway (порт 8080)
- Hotel Management Service (порт 8082)
- Booking Service (порт 8081)

## Запуск

```bash
mvn -q -DskipTests package
# в отдельных терминалах:
mvn -pl eureka-server spring-boot:run
mvn -pl hotel-service spring-boot:run
mvn -pl booking-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

## Swagger
- Booking: http://localhost:8081/swagger-ui.html
- Hotel:   http://localhost:8082/swagger-ui.html

## INTERNAL маршруты (не через Gateway)
- POST http://localhost:8082/internal/rooms/{id}/confirm-availability
- POST http://localhost:8082/internal/rooms/{id}/release

## Тестовые пользователи (Booking Service)
- admin / admin (ADMIN)
- user / user (USER)
