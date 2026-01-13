# Hotel Booking System (Microservices) — Spring Boot + Spring Cloud

Проект: микросервисная система бронирования отелей с API Gateway, Eureka Service Discovery, JWT-аутентификацией, двухшаговой согласованностью (saga) между Booking и Hotel сервисами, идемпотентностью и обработкой конфликтов при параллельных бронированиях.

## Стек
- Java 17+
- Spring Boot 3.5.x
- Spring Cloud (BOM, совместимый со Spring Boot)
- Spring Cloud Eureka (Service Discovery)
- Spring Cloud Gateway (API Gateway)
- Spring Security + JWT (HS256)
- Spring Data JPA + H2 (in-memory)
- Lombok, MapStruct
- Swagger/OpenAPI (springdoc)
- Тестирование: JUnit + MockMvc/WebTestClient (в зависимости от модуля)

---

## Архитектура и компоненты

### Сервисы
1. **eureka-server**  
   <img width="1879" height="937" alt="{5820BB2E-E7CC-49C2-850E-34F1B61720C0}" src="https://github.com/user-attachments/assets/1ce66d17-f734-4bbf-ab4b-9c4d6dbde751" />

2. **api-gateway**  
   Единая точка входа. Маршрутизация `/api/...` запросов в backend-сервисы через Eureka (LB). Прокси-передача `Authorization: Bearer <JWT>` в сервисы.

3. **hotel-service** (Hotel Management Service)  
   - CRUD отелей и номеров  
   - выдача доступных номеров на период
   - рекомендация номеров (сортировка по `times_booked`, затем по `id`)
   - подтверждение доступности номера в шаге согласованности (`confirm-availability`)
   - компенсирующее снятие блокировки (`release`) **не публикуется через Gateway**
   <img width="1386" height="680" alt="image" src="https://github.com/user-attachments/assets/04414fd2-40f0-445c-b0f9-a99964662081" />


4. **booking-service** (Booking Service)  
   - регистрация/авторизация пользователей (JWT)
   - создание бронирований (в т.ч. autoSelect)
   - история и получение бронирования
   - отмена бронирования (компенсация)
   - двухшаговая согласованность с hotel-service: `PENDING -> CONFIRMED`, при сбое `PENDING -> CANCELLED`
   <img width="1485" height="772" alt="image" src="https://github.com/user-attachments/assets/a485524c-9d1d-4f71-b587-049aa6565bbf" />

---

## Модель данных (минимальная схема)

### booking-service (H2 in-memory)
- `users`: `id`, `username`, `password`, `role`
- `bookings`: `id`, `user_id`, `room_id`, `start_date`, `end_date`, `status` (PENDING/CONFIRMED/CANCELLED), `created_at`, `request_id`

### hotel-service (H2 in-memory)
- `hotels`: `id`, `name`, `address`
- `rooms`: `id`, `hotel_id`, `number`, `available`, `times_booked`

> `available` отражает операционную доступность номера (ремонт/вывод), **не** используется для проверки занятости по датам.
> Занятость по датам определяется бронированиями и/или временной блокировкой слота в рамках двухшаговой согласованности.

---

## Безопасность
- JWT (HS256), срок жизни токена: **1 час**
- Роли:
  - `USER`: личные операции (создание/просмотр/отмена своих бронирований, просмотр отелей/номеров)
  - `ADMIN`: CRUD отелей/номеров/пользователей, статистика
- Каждый сервис валидирует JWT самостоятельно (Resource Server). Gateway выполняет маршрутизацию.

---

## Двухшаговая согласованность (Saga) при создании бронирования

1) Booking Service (локальная транзакция) создаёт `Booking(status=PENDING)` и фиксирует в своей БД  
2) Booking Service вызывает Hotel Service `POST /api/rooms/{id}/confirm-availability`
   - при успехе: Booking Service переводит бронь в `CONFIRMED`
   - при ошибке/тайм-ауте: Booking Service выполняет компенсацию:
     - переводит бронь в `CANCELLED`
     - вызывает `POST /api/rooms/{id}/release` (если успела установиться временная блокировка)

### Надёжность вызовов
Для удалённых вызовов задан:
- timeout
- ограниченное число retry с backoff
- при исчерпании — компенсация

### Идемпотентность
Во все команды, меняющие состояние (create/cancel/confirm/release), передаётся `requestId`.
Повторный запрос с тем же `requestId` не создаёт дублей и не приводит к повторной смене доступности/статуса.

---

## Алгоритм планирования занятости (равномерная загрузка)
- Hotel Service ведёт статистику `times_booked` для номера.
- Hotel Service отдаёт список свободных номеров на период.
- Endpoint рекомендаций сортирует свободные номера:
  1) по `times_booked` по возрастанию
  2) при равенстве — по `id`
- Booking Service при `autoSelect=true` выбирает **первый** номер из рекомендованного списка (равномерное распределение).

---

## Порты и endpoints

### Порты
- Eureka: `http://localhost:8761`
- API Gateway: `http://localhost:8080`
- Booking Service: `http://localhost:8081`
- Hotel Service: `http://localhost:8082`

### Swagger / OpenAPI
- Booking: `http://localhost:8081/swagger-ui/index.html`
- Hotel: `http://localhost:8082/swagger-ui/index.html`
- Через gateway можно вызывать API, но swagger открыт на самих сервисах.

---

## Как запустить

### Требования
- Java 17+
- Maven

### Сборка
Из корня проекта:
```bash
mvn -DskipTests clean package
mvn -pl eureka-server spring-boot:run
mvn -pl hotel-service spring-boot:run
mvn -pl booking-service spring-boot:run
mvn -pl api-gateway spring-boot:run
