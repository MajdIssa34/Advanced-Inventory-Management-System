# 🏬 Advanced Inventory Management System

An **enterprise-grade microservices backend** built with **Spring Boot**, **Docker**, **Kafka**, and **Keycloak**. Designed for scalability, modularity, and production-level readiness.

---

## 🔧 Tech Stack

- **Java 21**, **Spring Boot 3**
- **Docker & Docker Compose**
- **Apache Kafka** for async messaging
- **MongoDB** (Product Service)
- **PostgreSQL** (Inventory & Order Services)
- **Keycloak** (Authentication & Authorization)
- **Eureka Discovery Server**
- **Spring Cloud Gateway**

---

## 🧱 Microservices

| Service               | Description                                                   |
|-----------------------|---------------------------------------------------------------|
| `product-service`     | Manages products stored in MongoDB                            |
| `inventory-service`   | Tracks stock levels for products using PostgreSQL             |
| `order-service`       | Places orders, checks stock, publishes events to Kafka        |
| `notification-service`| Consumes Kafka events and sends notifications/logs them       |
| `discovery-server`    | Eureka-based service registry                                 |
| `api-gateway`         | Routes API calls to appropriate services                      |
| `keycloak`            | Manages users, roles, and authentication                      |

---

## 📁 Project Structure

```
.
├── product-service/
├── inventory-service/
├── order-service/
├── notification-service/
├── api-gateway/
├── discovery-server/
├── docker-compose.yml
├── realms/ (Keycloak config)
└── README.md
```

---

## 🚀 Getting Started

### 1. Build All JARs

```bash
mvn clean install -DskipTests
```

### 2. Start Docker Containers

```bash
docker-compose up -d --build
```

---

## 🔐 Keycloak Setup

- Access: `http://localhost:8080`
- Admin: `admin / admin`
- Realm: `inventory-realm`
- Client: `inventory-client`

> Realm is auto-imported from `./realms/` on first startup.

---

## 🧪 Sample API Calls (via API Gateway on `localhost:8181`)

### ➕ Create Product

`POST /api/product`

```json
{
  "name": "MacBook Pro",
  "description": "M3 Chip - 14 inch",
  "price": 2999.99,
  "skuCode": "macbook-m3-14"
}
```

### ➕ Add Inventory

`POST /api/inventory`

```json
{
  "skuCode": "macbook-m3-14",
  "quantity": 100
}
```

### 🛒 Place Order

`POST /api/order` (requires token from Keycloak)

```json
{
  "orderLineItemsDtoList": [
    {
      "skuCode": "macbook-m3-14",
      "price": 2999.99,
      "quantity": 1
    }
  ]
}
```

---

## 📦 Dockerized Services

All services run inside Docker:

- Databases: MongoDB, PostgreSQL (x2), MySQL (Keycloak)
- Brokers: Kafka + Zookeeper
- Backend services (Java)
- Auth: Keycloak
- Discovery: Eureka
- API Entry: Spring Cloud Gateway

---

## 📊 System Diagram

> *(Insert system architecture image here once hosted or committed)*

---

## 👨‍💻 Author

**Majd Issa**  
Backend Developer | Cloud & Microservices Enthusiast  
🌏 Sydney, Australia  
🌐 [https://majdissa.net](https://majdissa.net)

---

## 📎 GitHub Repository

[🔗 Open Project on GitHub](https://github.com/MajdIssa34/Advanced-Inventory-Management-System)