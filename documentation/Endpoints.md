# Advanced Inventory Management System – API Documentation

This document outlines the core REST APIs exposed by the **Advanced Inventory Management System**, a fully dockerized Spring Boot microservices project using Keycloak for authentication and Kafka for asynchronous messaging.

---

## 🔐 Authentication

* **Base URL:** `/auth/realms/your-realm-name/protocol/openid-connect/token`
* **Method:** `POST`
* **Headers:**

  * `Content-Type: application/x-www-form-urlencoded`
* **Body:**

  * `client_id=<client_id>`
  * `client_secret=<client_secret>`
  * `username=<username>`
  * `password=<password>`
  * `grant_type=password`

---

## 📦 Product Service (`/api/product`)

### ➕ Create Product

* **POST** `/api/product`
* **Body:**

```json
{
  "name": "iPhone 15",
  "description": "Apple smartphone",
  "price": 1500.00,
  "skuCode": "IPHONE15"
}
```

### 📄 Get All Products

* **GET** `/api/product`

---

## 📊 Inventory Service (`/api/inventory`)

### ✅ Check Inventory

* **GET** `/api/inventory?skuCode=IPHONE15`
* **Returns:**

```json
[
  {
    "skuCode": "IPHONE15",
    "isInStock": true
  }
]
```

### ➕ Add Inventory

* **POST** `/api/inventory`
* **Body:**

```json
{
  "skuCode": "IPHONE15",
  "quantity": 50
}
```

### ➖ Reduce Inventory

* **PUT** `/api/inventory/reduce-stock`
* **Body:**

```json
[
  {
    "skuCode": "IPHONE15",
    "price": 1500.00,
    "quantity": 1
  }
]
```

### ❌ Delete Inventory

* **DELETE** `/api/inventory?skuCode=IPHONE15`

---

## 📝 Order Service (`/api/order`)

### 🛒 Place Order

* **POST** `/api/order`
* **Body:**

```json
{
  "orderLineItemsDtoList": [
    {
      "skuCode": "IPHONE15",
      "price": 1500.00,
      "quantity": 1
    }
  ]
}
```

---

## 🔔 Notification Service

* No public endpoints (Kafka event-based)
* Subscribed to Kafka topic: `notificationTopic`
* Sends notification when an order is successfully placed.

---

## ⚙️ Service Discovery (Eureka)

* Web UI at: `http://localhost:8761`

## 🌐 API Gateway

* All endpoints are routed through `http://localhost:8181`

---

## 🐳 Deployment

* All services are dockerized
* Launch with: `docker-compose up --build -d`

---

For more details, visit the GitHub repository:
🔗 [https://github.com/MajdIssa34/Advanced-Inventory-Management-System](https://github.com/MajdIssa34/Advanced-Inventory-Management-System)
