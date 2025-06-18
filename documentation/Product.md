## Product Service API Documentation

This service is the source of truth for all product information. It is responsible for creating, retrieving, updating, and deleting product catalog data.

---

### 1. Create Product

Creates a new product in the catalog.

`POST /api/product`

**Description**
Adds a new product to the database. Before creation, it checks if a product with the same `skuCode` already exists. [cite_start]If it does, the request will fail to ensure SKU uniqueness. [cite: 1]

**Request Body**
The request body must be a JSON object containing the product's details.

* `name` (string, required): The name of the product.
* `description` (string, required): A detailed description of the product.
* `price` (number, required): The price of the product.
* `skuCode` (string, required): The unique stock keeping unit identifier.

***Example Request:***
```json
{
  "name": "MacBook Pro 16-inch",
  "description": "The M3 Max chip, with a 16-core CPU and 40-core GPU, provides exceptional performance for the most demanding workflows.",
  "price": 4299.00,
  "skuCode": "macbook-pro-m3"
}
```

**Responses**
* **`201 Created`**: The product was successfully created. [cite_start]The response body is empty. [cite: 4]
* [cite_start]**`500 Internal Server Error`**: Thrown as a `RuntimeException` if a product with the same `skuCode` already exists. [cite: 1]

---

### 2. Get All Products

Retrieves a list of all available products.

`GET /api/product`

**Description**
[cite_start]Fetches a complete list of all products in the catalog. [cite: 1, 4]

**Responses**
* [cite_start]**`200 OK`**: Successfully retrieved the list of all products. [cite: 4]

***Example Response:***
```json
[
  {
    "id": "60c72b2f9b1d8e001f8e4b2d",
    "name": "MacBook Pro 16-inch",
    "description": "The M3 Max chip, with a 16-core CPU and 40-core GPU, provides exceptional performance for the most demanding workflows.",
    "price": 4299.00,
    "skuCode": "macbook-pro-m3"
  },
  {
    "id": "60c72b2f9b1d8e001f8e4b2e",
    "name": "iPhone 15 Pro",
    "description": "A17 Pro chip. A monster win for gaming.",
    "price": 1849.00,
    "skuCode": "iphone-15-pro"
  }
]
```

---

### 3. Get Product by ID

Retrieves a single product by its unique database ID.

`GET /api/product/{id}`

**Description**
[cite_start]Finds and returns a single product using its unique identifier. [cite: 1, 4]

**Path Parameters**
* `id` (string, required): The unique ID of the product.

***Example Request:***
`/api/product/60c72b2f9b1d8e001f8e4b2d`

**Responses**
* [cite_start]**`200 OK`**: A product with the specified ID was found. [cite: 4] The response is a single `ProductResponse` object.
* [cite_start]**`500 Internal Server Error`**: Thrown as a `RuntimeException` if no product matching the `id` is found. [cite: 1]

---

### 4. Get Product by SKU

Retrieves a single product by its unique SKU code.

`GET /api/product/sku/{skuCode}`

**Description**
[cite_start]Finds and returns a single product using its unique `skuCode`. [cite: 1, 4]

**Path Parameters**
* `skuCode` (string, required): The SKU code of the product.

***Example Request:***
`/api/product/sku/macbook-pro-m3`

**Responses**
* [cite_start]**`200 OK`**: A product with the specified SKU was found. [cite: 4] The response is a single `ProductResponse` object.
* [cite_start]**`500 Internal Server Error`**: Thrown as a `RuntimeException` if no product matching the `skuCode` is found. [cite: 1]

---

### 5. Update Product

Updates the details of an existing product.

`PUT /api/product/{id}`

**Description**
Modifies the properties of a product specified by its ID. [cite_start]The request body should contain all fields for the product, as it performs a full update. [cite: 1, 4]

**Path Parameters**
* `id` (string, required): The unique ID of the product to be updated.

**Request Body**
The request body must be a JSON object containing the new details for the product.

***Example Request:***
```json
{
  "name": "MacBook Pro 16-inch (Updated)",
  "description": "An updated description for the M3 Max chip model.",
  "price": 4399.00,
  "skuCode": "macbook-pro-m3-v2"
}
```

**Responses**
* [cite_start]**`200 OK`**: The product was successfully updated. [cite: 4] The response body is empty.
* [cite_start]**`500 Internal Server Error`**: Thrown as a `RuntimeException` if no product matching the `id` is found. [cite: 1]

---

### 6. Delete Product

Deletes a product from the catalog.

`DELETE /api/product/{id}`

**Description**
[cite_start]Permanently removes a product from the database using its unique ID. [cite: 1, 4]

**Path Parameters**
* `id` (string, required): The unique ID of the product to be deleted.

***Example Request:***
`/api/product/60c72b2f9b1d8e001f8e4b2e`

**Responses**
* [cite_start]**`204 No Content`**: The product was successfully deleted. [cite: 4] The response body is empty.