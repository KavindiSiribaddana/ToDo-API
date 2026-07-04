# Todo API - Spring Boot Java 17

A simple Spring Boot REST API for learning CRUD operations.

## Tech Stack

- Java 17
- Spring Boot 3.5.15
- Spring Web
- Spring Data JPA
- H2 in-memory database
- Maven

## How to Run in IntelliJ IDEA

1. Extract this project ZIP.
2. Open IntelliJ IDEA.
3. Select **File > Open**.
4. Choose the extracted `todo-api` folder.
5. Wait until Maven dependencies are loaded.
6. Make sure Project SDK is **Java 17** or newer.
7. Run `TodoApiApplication.java`.

The API will run at:

```text
http://localhost:8080
```

## Run from Terminal

```bash
mvn spring-boot:run
```

## H2 Console

Open:

```text
http://localhost:8080/h2-console
```

Use this JDBC URL:

```text
jdbc:h2:mem:todo_db
```

Username:

```text
sa
```

Password is empty.

## API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/todos` | Get all todos |
| GET | `/api/todos/{id}` | Get one todo by id |
| POST | `/api/todos` | Create a todo |
| PUT | `/api/todos/{id}` | Update a todo |
| PATCH | `/api/todos/{id}/status` | Update only completed status |
| DELETE | `/api/todos/{id}` | Delete a todo |

## Sample JSON Bodies

### Create Todo

```json
{
  "title": "Learn Spring Boot",
  "description": "Build a simple CRUD API"
}
```

### Update Todo

```json
{
  "title": "Learn Spring Boot CRUD",
  "description": "Update existing todo using PUT",
  "completed": false
}
```

### Update Todo Status

```json
{
  "completed": true
}
```

## Postman

Import the included file:

```text
postman/Todo API.postman_collection.json
```

The collection uses this variable:

```text
baseUrl = http://localhost:8080
```

After running **Create Todo**, the collection automatically saves the created todo id into the `todoId` variable.
