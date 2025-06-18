## Order Service API Documentation

This service handles all customer order-related operations. Its primary responsibilities include placing new orders, performing stock validation by communicating with the **Inventory Service**, and retrieving order data.

---

### 1. Place Order

Creates a new customer order after validating product availability.

`POST /api/order`

**Description**
This is a multi-step transactional endpoint. When an order is placed:
1.  A unique `orderNumber` (UUID) is generated.
2.  It communicates with the **Inventory Service** by calling `GET /api/inventory` to check if all `skuCode`s in the order exist and have sufficient stock.
3.  If any product is not found, is out of stock, or has insufficient quantity, the entire order is rejected with an error.
4.  If stock is validated, the order is saved to the database.
5.  A `PUT` request is sent to the **Inventory Service** at `/api/inventory/reduce-stock` to decrement the stock levels.
6.  An `OrderPlacedEvent` is published to a Kafka topic named `notificationTopic` to notify other services.

**Request Body**
The request body must be a JSON object containing a list of line items.

* `orderLineItemsDtoList` (List, required): An array of objects, where each object represents a product in the order.
    * `skuCode` (string, required): The unique identifier for the product.
    * `price` (number, required): The price of a single unit of the product.
    * `quantity` (integer, required): The number of units being ordered.

***Example Request:***
```json
{
  "orderLineItemsDtoList": [
    {
      "skuCode": "iphone-15-pro",
      "price": 1849.00,
      "quantity": 1
    },
    {
      "skuCode": "airpods-pro",
      "price": 399.00,
      "quantity": 2
    }
  ]
}
```

**Responses**
* **`201 Created`**: The order was successfully placed and validated. The response body is a simple string.
  ***Example Response:***
  ```
  Order Placed
  ```
* **`400 Bad Request`**: Thrown as an `IllegalArgumentException` if any product is not found, is out of stock, or the requested quantity is unavailable.
* **`500 Internal Server Error`**: If the service fails to connect to the Inventory Service or the database.

---

### 2. Get All Orders

Retrieves a list of every order that has been placed.

`GET /api/order`

**Description**
Fetches a complete list of all orders from the database.

**Responses**
* **`200 OK`**: Successfully retrieved the list of all orders. The response is a JSON array of `Order` objects.

***Example Response:***
```json
[
  {
    "id": 1,
    "orderNumber": "e8a3a3e4-8b7f-4c1e-9b6f-0b8a1c3d0f2e",
    "orderLineItemsList": [
      {
        "id": 1,
        "skuCode": "iphone-15-pro",
        "price": 1849.00,
        "quantity": 1
      }
    ]
  },
  {
    "id": 2,
    "orderNumber": "f9b4c2d1-7a6e-3d0d-8a5e-1c7b2a9d1e4f",
    "orderLineItemsList": [
      {
        "id": 2,
        "skuCode": "macbook-pro-m3",
        "price": 4299.00,
        "quantity": 2
      }
    ]
  }
]
```

---

### 3. Get Order by Order Number

Finds and retrieves a single order by its unique order number.

`GET /api/order/{orderNumber}`

**Description**
Looks up an order using the `orderNumber` (UUID string) assigned to it upon creation.

**Path Parameters**
* `orderNumber` (string, required): The unique identifier of the order.

***Example Request:***
`/api/order/e8a3a3e4-8b7f-4c1e-9b6f-0b8a1c3d0f2e`

**Responses**
* **`200 OK`**: An order with the specified number was found. The response is a single `Order` object.
* **`500 Internal Server Error`**: Thrown as a `RuntimeException` if no order matching the `orderNumber` is found.

---

### 4. Get Orders by SKU Code

Retrieves a list of all orders that contain a specific product.

`GET /api/order/sku/{skuCode}`

**Description**
Searches all orders and returns those that have a line item matching the provided `skuCode`. This is useful for tracking all sales of a particular product.

**Path Parameters**
* `skuCode` (string, required): The product SKU to search for within orders.

***Example Request:***
`/api/order/sku/iphone-15-pro`

**Responses**
* **`200 OK`**: Successfully retrieved the list of orders containing the SKU. The response is a JSON array of `Order` objects. If no orders contain the SKU, an empty list `[]` is returned.

---

### 5. Get Recent Orders

Retrieves a list of the most recently placed orders.

`GET /api/order/recent`

**Description**
Fetches a list of recent orders, sorted from newest to oldest. The number of orders returned can be controlled with a query parameter.

**Query Parameters**
* `limit` (int, optional, `defaultValue = "5"`): The maximum number of recent orders to return.

***Example Request:***
`/api/order/recent?limit=3`

**Responses**
* **`200 OK`**: Successfully retrieved the list of recent orders. The response is a JSON array of `Order` objects.