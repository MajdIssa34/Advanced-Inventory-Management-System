This document provides detailed API documentation for the **Inventory Service**.

## Inventory Service API Documentation

This service is responsible for managing product stock levels. It handles creating inventory items, checking stock, reducing stock for orders, restocking, and reporting.

-----

### 1\. Create Inventory Item

Creates a new inventory record for a product.

`POST /api/inventory`

**Description**
This endpoint adds a new item to the inventory system. It requires a SKU code and an initial quantity.

**Request Body**
The request body must be a JSON object containing the `skuCode` and `quantity` of the new inventory item.

  * `skuCode` (string, required): The unique stock keeping unit identifier for the product.
  * `quantity` (integer, required): The initial stock quantity for the product.

***Example Request:***

```json
{
  "skuCode": "macbook-pro-m3",
  "quantity": 50
}
```

**Responses**

  * **`201 Created`**: The inventory item was successfully created. The response body is empty.
  * **`400 Bad Request`**: The request body is malformed or missing required fields.

-----

### 2\. Get All Inventory

Retrieves a list of all items in the inventory.

`GET /api/inventory/all`

**Description**
Fetches a complete list of all inventory items and their current stock status.

**Responses**

  * **`200 OK`**: Successfully retrieved the list of all inventory items.

***Example Response:***

```json
[
  {
    "skuCode": "macbook-pro-m3",
    "isInStock": true,
    "quantity": 45
  },
  {
    "skuCode": "iphone-15-pro",
    "isInStock": true,
    "quantity": 120
  },
  {
    "skuCode": "apple-watch-9",
    "isInStock": false,
    "quantity": 0
  }
]
```

-----

### 3\. Check Stock for Multiple Items

Checks the availability and quantity of one or more specific items by their SKU codes.

`GET /api/inventory`

**Description**
This endpoint is used to query the stock status of a list of products. It is useful for checking product availability before adding items to a cart.

**Query Parameters**

  * `skuCode` (List\<String\>, required): A list of SKU codes to check.

***Example Request:***
`/api/inventory?skuCode=macbook-pro-m3&skuCode=iphone-15-pro`

**Responses**

  * **`200 OK`**: Successfully retrieved the stock status for the requested items. The response is a list of `InventoryResponse` objects. If a requested SKU code does not exist in the database, it will be omitted from the response.

***Example Response:***

```json
[
  {
    "skuCode": "macbook-pro-m3",
    "isInStock": true,
    "quantity": 45
  },
  {
    "skuCode": "iphone-15-pro",
    "isInStock": true,
    "quantity": 120
  }
]
```

-----

### 4\. Reduce Stock

Reduces the stock for one or more items, typically after an order has been placed.

`PUT /api/inventory/reduce-stock`

**Description**
This endpoint processes a list of ordered items and decrements their quantities from the inventory. This operation is transactional; if any item in the list cannot be fulfilled, the entire operation fails.

**Request Body**
The request body must be a JSON array of items to be reduced.

  * `skuCode` (string, required): The SKU of the product.
  * `quantity` (integer, required): The number of units to be removed from stock.

***Example Request:***

```json
[
  {
    "skuCode": "iphone-15-pro",
    "quantity": 2
  },
  {
    "skuCode": "macbook-pro-m3",
    "quantity": 1
  }
]
```

**Responses**

  * **`200 OK`**: Stock for all items was successfully reduced. The response body is empty.
  * **`400 Bad Request`**: Thrown as an `IllegalArgumentException` if the requested `quantity` for any item is greater than the available stock.
  * **`500 Internal Server Error`**: Thrown as a `RuntimeException` if an item with the specified `skuCode` is not found in the inventory.

-----

### 5\. Restock Inventory

Increases the quantity of a specific inventory item.

`PUT /api/inventory/restock`

**Description**
This endpoint is used to add stock to an existing inventory item, for example, when a new shipment arrives.

**Request Body**
The request body must be a JSON object containing the `skuCode` and the `quantity` to add.

  * `skuCode` (string, required): The SKU of the product to restock.
  * `quantity` (integer, required): The number of units to add to the existing stock.

***Example Request:***

```json
{
  "skuCode": "apple-watch-9",
  "quantity": 30
}
```

**Responses**

  * **`200 OK`**: The item was successfully restocked. The response body is empty.
  * **`500 Internal Server Error`**: Thrown as a `RuntimeException` if an item with the specified `skuCode` is not found in the inventory.

-----

### 6\. Get Low Stock Items

Retrieves a list of items whose stock quantity is at or below a specified threshold.

`GET /api/inventory/low-stock`

**Description**
Provides a report of items that are running low on stock, allowing for proactive restocking.

**Query Parameters**

  * `threshold` (Integer, optional, `defaultValue = "10"`): The stock level at or below which an item is considered "low stock".

***Example Request:***
`/api/inventory/low-stock?threshold=5`

**Responses**

  * **`200 OK`**: Successfully retrieved the list of low stock items. The response format is identical to the `GET /api/inventory/all` endpoint.

***Example Response (for threshold=5):***

```json
[
    {
        "skuCode": "airpods-pro",
        "isInStock": true,
        "quantity": 3
    }
]
```

-----

### 7\. Delete Inventory Item

Deletes an inventory item from the system entirely.

`DELETE /api/inventory`

**Description**
Permanently removes a product's inventory record. This should be used with caution, for instance, when a product is discontinued.

**Query Parameters**

  * `skuCode` (String, required): The SKU code of the item to be deleted.

***Example Request:***
`/api/inventory?skuCode=old-product-sku`

**Responses**

  * **`200 OK`**: The inventory item was successfully deleted. The response body is empty.
  * **`500 Internal Server Error`**: Thrown as a `RuntimeException` if an item with the specified `skuCode` is not found.