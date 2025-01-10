# Order Management System

A Kotlin-based order management system with a Kafka producer/consumer integration, an HTTP server, and an in-memory cache. This project demonstrates efficient order handling with a combination of asynchronous programming, database operations, and in-memory caching.

## Features

- **Order Management**: View orders, send test orders, and manage orders in a PostgreSQL database.
- **Kafka Integration**:
    - Kafka producer to send test orders.
    - Kafka consumer to process incoming orders.
- **HTTP Server**: A Vert.x-based HTTP server serving both API and static content.
- **In-Memory Cache**: A high-performance cache for quick access to order data.
- **Pagination**: View orders with pagination in the web interface.
- **Localization**: Multi-language support (English and Russian) using i18n.
- **Graceful Shutdown**: Handles application shutdown with proper cleanup.

## Technologies Used

- **Kotlin**: Core programming language.
- **Kafka**: Message broker for order communication.
- **Vert.x**: High-performance reactive toolkit for HTTP server.
- **PostgreSQL**: Database for persistent order storage.
- **Kotlinx.Serialization**: JSON serialization and deserialization.
- **Coroutines**: Asynchronous programming model.
- **Dotenv**: Configuration management.
- **HTML/JavaScript**: Frontend interface with interactive elements.

## Requirements

- Java 11 or higher.
- Kotlin 1.8+.
- Docker (for Kafka and PostgreSQL setup).
- PostgreSQL database.

## Setup

### Environment Variables

Create a `.env` file in the root directory with the following variables:

```env
DB_HOST=localhost
DB_PORT=5432
DB_USER=your_db_user
DB_PASSWORD=your_db_password
DB_NAME=your_db_name

KAFKA_BROKERS=localhost:9092
KAFKA_TOPIC=orders
KAFKA_GROUP_ID=order-consumer-group

HTTP_PORT=8081
SHUTDOWN_TIMEOUT=5S
```

### Project Structure
```
src/
├── main/
│   ├── kotlin/
│   │   ├── app/                   # Main entry point
│   │   ├── cache/                 # In-memory caching logic
│   │   ├── config/                # Configuration loader
│   │   ├── db/                    # Database initialization and migrations
│   │   ├── kafka/                 # Kafka producer and consumer
│   │   ├── model/                 # Data models
│   │   ├── repository/            # Database repository logic
│   │   ├── server/                # HTTP server logic
│   │   ├── service/               # Business logic
│   │   └── util/                  # Utils
│   └── resources/
│       ├── web/                   # Static web content (index.html, JS, CSS)
│       └── migrations/            # SQL migrations
│
├── test/                          # Tests
├── docker-compose.yml
└── README.md
```

## API Endpoints

### **GET /order/:id**
Fetch order details by `order_uid`.

- **URL Parameters**:
  - `id`: The unique identifier of the order.

- **Response**:
  - `200 OK`: Returns the order details as JSON.
  - `404 Not Found`: If the order does not exist.

---

### **POST /api/send-test-order**
Send a randomly generated test order.

- **Request Body**:
  - None.

- **Response**:
  - `200 OK`: Test order sent successfully.
  - `500 Internal Server Error`: If the server fails to process the request.

---

### **GET /api/orders**
Fetch all orders with pagination.

- **Query Parameters**:
  - `page` (optional): The page number (default: 1).
  - `size` (optional): Number of orders per page (default: 10).

- **Response**:
  - `200 OK`: Returns a JSON array of orders.
  - `404 Not Found`: If no orders are available.

---

## Frontend

The web interface is served at [http://localhost:8080/](http://localhost:8080/). It includes the following features:

- **Viewing an order by ID**:
  Enter an order UID in the input field and click "Show Order" to retrieve order details.

- **Sending a test order to Kafka**:
  Click the "Send Test Order" button to generate and send a test order to Kafka.

- **Viewing a paginated list of orders**:
  Click "Show Orders" to display a paginated list of orders, with navigation buttons to move between pages.

## Development
### Run the Project
Build the project: ```./gradlew build```
Run the application:

### Run Tests:
Execute all unit tests using: ```./gradlew test```

### Static Analysis:
Run checks for coding standards: ```./gradlew check```