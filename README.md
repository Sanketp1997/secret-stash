# Secret Stash - Secure Note-Taking API

Secret Stash is a secure note-taking API where users can create, update, delete, and retrieve their private notes. Each user can only access their own notes, with authentication and security being top priorities.

## Features

- **JWT-based Authentication**: Secure user registration and login
- **CRUD Operations for Notes**: Create, read, update, and delete notes
- **Note Versioning**: All notes are versioned, and older versions can be retrieved
- **Self-destruction Timer**: Notes can be set to automatically delete after a specified time
- **Rate Limiting**: Protection against brute-force attacks
- **Secure Password Storage**: Passwords are securely hashed using BCrypt
- **Efficient Retrieval**: Optimized for retrieving the latest 5,000 notes sorted by creation date

## Technology Stack

- **Language**: Kotlin
- **Framework**: Spring Boot 3.2.5
- **Security**: Spring Security with JWT
- **Database**: PostgreSQL
- **Build Tool**: Gradle with Kotlin DSL
- **Rate Limiting**: resilience4j

## Prerequisites

- JDK 17
- PostgreSQL 15
- Gradle 8.0

## Getting Started

### Database Setup

1. Create a PostgreSQL database named `secretstash`
   ```sql
   CREATE DATABASE secretstash;
   ```

2. Update the database configuration in `src/main/resources/application.properties` if needed:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/secretstash
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```

### Building and Running the Application

1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/secret-stash.git
   cd secret-stash
   ```

2. Build the application
   ```bash
   ./gradlew build
   ```

3. Run the application
   ```bash
   ./gradlew bootRun
   ```

The application will start on `http://localhost:8080`.

## API Documentation

### Authentication

#### Register a new user

```
POST /api/auth/register
```

Request body:
```json
{
  "username": "user123",
  "password": "SecureP@ss123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "user123",
  "message": "User registered successfully"
}
```

#### Login

```
POST /api/auth/login
```

Request body:
```json
{
  "username": "user123",
  "password": "SecureP@ss123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "user123",
  "message": "Authentication successful"
}
```

### Notes

All note endpoints require authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

#### Create a new note

```
POST /api/notes
```

Request body:
```json
{
  "title": "My Secret Note",
  "content": "This is a confidential note.",
  "expiryTime": "2025-12-31T23:59:59" // Optional
}
```

Response:
```json
{
  "id": 1,
  "title": "My Secret Note",
  "content": "This is a confidential note.",
  "createdAt": "2025-05-19T19:30:00",
  "updatedAt": "2025-05-19T19:30:00",
  "expiryTime": "2025-12-31T23:59:59",
  "version": 1
}
```

#### Get a note by ID

```
GET /api/notes/{id}
```

Response:
```json
{
  "id": 1,
  "title": "My Secret Note",
  "content": "This is a confidential note.",
  "createdAt": "2025-05-19T19:30:00",
  "updatedAt": "2025-05-19T19:30:00",
  "expiryTime": "2025-12-31T23:59:59",
  "version": 1
}
```

#### Get all notes (paginated)

```
GET /api/notes?page=0&size=10
```

Response:
```json
{
  "content": [
    {
      "id": 1,
      "title": "My Secret Note",
      "content": "This is a confidential note.",
      "createdAt": "2025-05-19T19:30:00",
      "updatedAt": "2025-05-19T19:30:00",
      "expiryTime": "2025-12-31T23:59:59",
      "version": 1
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "first": true,
  "size": 10,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 1,
  "empty": false
}
```

#### Update a note

```
PUT /api/notes/{id}
```

Request body:
```json
{
  "title": "Updated Secret Note",
  "content": "This note has been updated.",
  "expiryTime": "2025-12-31T23:59:59"
}
```

Response:
```json
{
  "id": 1,
  "title": "Updated Secret Note",
  "content": "This note has been updated.",
  "createdAt": "2025-05-19T19:30:00",
  "updatedAt": "2025-05-19T19:45:00",
  "expiryTime": "2025-12-31T23:59:59",
  "version": 2
}
```

#### Delete a note

```
DELETE /api/notes/{id}
```

Response: 204 No Content

#### Get note versions

```
GET /api/notes/{id}/versions
```

Response:
```json
[
  {
    "id": 1,
    "title": "My Secret Note",
    "content": "This is a confidential note.",
    "createdAt": "2025-05-19T19:30:00",
    "versionNumber": 1,
    "noteId": 1
  }
]
```

## Security Considerations

### Password Complexity

Passwords must:
- Be at least 8 characters long
- Contain at least one digit
- Contain at least one lowercase letter
- Contain at least one uppercase letter
- Contain at least one special character
- Not contain whitespace

### Rate Limiting

The API implements rate limiting to prevent brute-force attacks:
- 10 requests per minute for authentication endpoints

### Data Protection

- All communication should be over HTTPS
- JWT tokens have a 24-hour expiration(can be reduced in production)
- Self-destructing notes are permanently deleted from the database

## Indexing Strategy

The application uses the following indexing strategy for efficient retrieval:
- Indexes on user_id for quick filtering of notes by user
- Indexes on created_at for efficient sorting by creation date
- Composite index on (user_id, created_at) for optimized queries

## Indexing & Performance

- Notes are efficiently paginated and sorted by creation date.
- Only the authenticated user's notes are queried and returned.
- Expired notes are periodically cleaned up.

## Running Tests

To run the full test suite (unit, service, repository, and controller tests):

```bash
./gradlew test
```

## API Documentation

Interactive API docs are available via Swagger UI:

- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

All endpoints and schemas are documented. You can authorize using a JWT token from the login endpoint.