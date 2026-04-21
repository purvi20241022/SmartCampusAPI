# Smart Campus Sensor & Room Management API

**Module:** 5COSC022C.2 — Client-Server Architectures (2025)  
**Student:** Purvi Kaluarachchi 
**Student ID:** 20241022 
**GitHub Repo:** https://github.com/purvi20241022/SmartCampusAPI

---

## Table of Contents

1. [API Overview](#api-overview)
2. [Project Structure](#project-structure)
3. [Build Instructions](#build-instructions)
4. [How to Run from NetBeans](#how-to-run-from-netbeans)
5. [API Endpoints Reference](#api-endpoints-reference)
6. [Sample curl Commands](#sample-curl-commands)
7. [Report — Written Answers to Coursework Questions](#report--written-answers-to-coursework-questions)

---

## API Overview

The Smart Campus API is a RESTful web service built using JAX-RS (Jersey 2.x) that manages Rooms and Sensors across a university campus. It provides a scalable backend for campus facilities managers to monitor and control smart devices such as CO2 monitors, occupancy trackers, and temperature sensors.

The API follows REST architectural principles including:
- Resource-based URL design with a logical hierarchy
- Stateless communication using standard HTTP methods
- JSON as the data exchange format
- Meaningful HTTP status codes for all outcomes
- Hypermedia links (HATEOAS) on the discovery endpoint

**Base URL:** `http://localhost:8080/api/v1`

**Primary Resources:**

| Resource | Base Path | Description |
|---|---|---|
| Discovery | `GET /api/v1` | API metadata and navigation links |
| Rooms | `/api/v1/rooms` | Campus room management |
| Sensors | `/api/v1/sensors` | Sensor registration and monitoring |
| Readings | `/api/v1/sensors/{id}/readings` | Historical sensor reading logs |

**Data Models:**

- **Room** — id, name, capacity, sensorIds list
- **Sensor** — id, type, status (ACTIVE / MAINTENANCE / OFFLINE), currentValue, roomId
- **SensorReading** — id (UUID), timestamp (epoch ms), value

---

## Project Structure

```
SmartCampusAPI/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/
                └── smartcampus/
                    ├── Main.java
                    ├── SmartCampusApp.java
                    ├── model/
                    │   ├── Room.java
                    │   ├── Sensor.java
                    │   └── SensorReading.java
                    ├── store/
                    │   └── DataStore.java
                    ├── resource/
                    │   ├── DiscoveryResource.java
                    │   ├── RoomResource.java
                    │   ├── SensorResource.java
                    │   └── SensorReadingResource.java
                    └── exception/
                        ├── RoomNotEmptyException.java
                        ├── RoomNotEmptyExceptionMapper.java
                        ├── LinkedResourceNotFoundException.java
                        ├── LinkedResourceNotFoundExceptionMapper.java
                        ├── SensorUnavailableException.java
                        ├── SensorUnavailableExceptionMapper.java
                        └── GlobalExceptionMapper.java
```

---

## Build Instructions

### Prerequisites

- Java JDK 11 or 17
- Apache NetBeans 21 or later
- Apache Maven 3.8+ (bundled with NetBeans)
- Internet connection (for Maven to download dependencies on first build)

### Step 1 — Clone the repository

```bash
git clone https://github.com/[your-username]/SmartCampusAPI.git
cd SmartCampusAPI
```

### Step 2 — Open in NetBeans

1. Open NetBeans
2. Go to **File → Open Project**
3. Navigate to the cloned folder and select it
4. NetBeans will detect the Maven project automatically

### Step 3 — Download dependencies

NetBeans will automatically resolve Maven dependencies on first open. If it does not:

1. Right-click the project in the Projects panel
2. Select **Build with Dependencies**

This downloads Jersey, Grizzly, and Jackson from Maven Central.

### Step 4 — Build the project

Right-click the project → **Clean and Build**

You should see `BUILD SUCCESS` in the Output window.

---

## How to Run from NetBeans

1. Open the **Projects** panel on the left
2. Expand **Source Packages → com.mycompany.smartcampusapi**
3. Open `SmartCampusAPI.java`
4. Right-click anywhere in the editor → **Run File** (or press Shift + F6)
5. The Output window will show:

```
Smart Campus API started.
Server running at: http://localhost:8080/api/v1
Press ENTER to stop the server...
```

6. Open a browser or Postman and visit `http://localhost:8080/api/v1` to confirm the API is running
7. Press **ENTER** in the Output window to stop the server when done

> **Note:** Always right-click `Main.java` → Run File rather than using the toolbar Run button, to ensure the correct entry point is used.

---

## API Endpoints Reference

### Discovery
| Method | Path | Description | Response |
|---|---|---|---|
| GET | `/api/v1` | API metadata and links | 200 |

### Rooms
| Method | Path | Description | Response |
|---|---|---|---|
| GET | `/api/v1/rooms` | List all rooms | 200 |
| POST | `/api/v1/rooms` | Create a new room | 201 |
| GET | `/api/v1/rooms/{roomId}` | Get room by ID | 200 / 404 |
| DELETE | `/api/v1/rooms/{roomId}` | Delete room (only if no sensors) | 204 / 409 |

### Sensors
| Method | Path | Description | Response |
|---|---|---|---|
| GET | `/api/v1/sensors` | List all sensors (optional `?type=` filter) | 200 |
| POST | `/api/v1/sensors` | Register a new sensor | 201 / 422 |

### Sensor Readings (Sub-resource)
| Method | Path | Description | Response |
|---|---|---|---|
| GET | `/api/v1/sensors/{sensorId}/readings` | Get reading history | 200 |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading | 201 / 403 |

---

## Sample curl Commands

### 1. Get API discovery information
```bash
curl -X GET http://localhost:8080/api/v1 \
  -H "Accept: application/json"
```
**Expected response (200 OK):**
```json
{
  "version": "1.0",
  "adminContact": "admin@smartcampus.ac.uk",
  "links": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

---

### 2. Create a new room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Library Quiet Study",
    "capacity": 50
  }'
```
**Expected response (201 Created):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Library Quiet Study",
  "capacity": 50,
  "sensorIds": []
}
```

---

### 3. Register a sensor linked to a room
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 412.5,
    "roomId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  }'
```
**Expected response (201 Created):**
```json
{
  "id": "e5f6g7h8-...",
  "type": "CO2",
  "status": "ACTIVE",
  "currentValue": 412.5,
  "roomId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

---

### 4. Get all sensors filtered by type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2" \
  -H "Accept: application/json"
```
**Expected response (200 OK):**
```json
[
  {
    "id": "e5f6g7h8-...",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 412.5,
    "roomId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  }
]
```

---

### 5. Post a new sensor reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/e5f6g7h8-.../readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 430.2
  }'
```
**Expected response (201 Created):**
```json
{
  "id": "r9s0t1u2-...",
  "timestamp": 1745123456789,
  "value": 430.2
}
```

---

### 6. Attempt to delete a room that still has sensors assigned (409 error case)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Accept: application/json"
```
**Expected response (409 Conflict):**
```json
{
  "error": "409 Conflict",
  "message": "Room LIB-301 still has sensors assigned to it.",
  "roomId": "LIB-301"
}
```

---

### 7. Attempt to post a reading to a MAINTENANCE sensor (403 error case)
```bash
curl -X POST http://localhost:8080/api/v1/sensors/[maintenance-sensor-id]/readings \
  -H "Content-Type: application/json" \
  -d '{ "value": 25.0 }'
```
**Expected response (403 Forbidden):**
```json

{
  "error": "403 Forbidden",
  "message": "Sensor TEMP-001 is under MAINTENANCE and cannot accept readings.",
  "sensorId": "TEMP-001"
}
```

---

### 8. Attempt to register a sensor with a non-existent roomId (422 error case)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 21.5,
    "roomId": "does-not-exist-id"
  }'
```
**Expected response (422 Unprocessable Entity):**
```json
{
  "error": "422 Unprocessable Entity",
  "message": "Room not found with id: does-not-exist-id",
  "invalidRoomId": "does-not-exist-id"
}
```

---

## Report — Written Answers to Coursework Questions

---

### Part 1 — Service Architecture & Setup

#### Question 1.1: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance created per request or is it a singleton? How does this affect in-memory data management and synchronisation?

By default, JAX-RS creates a **new instance of a resource class for every incoming HTTP request**. This is known as the per-request lifecycle. Each request gets its own fresh object, which means any instance variables declared on the resource class are discarded the moment the request completes.

This has a critical implication for in-memory data storage. If rooms or sensors were stored as instance variables directly inside the resource class (e.g. `private Map<String, Room> rooms = new HashMap<>()`), that data would be lost after every single request — the next call would see an empty map. This is not acceptable for an API that needs to persist data across requests.

To solve this, the data store must exist outside the resource class lifecycle. This project uses a **singleton DataStore class** with a private constructor and a static `getInstance()` method that always returns the same shared object. Since there is only one DataStore instance for the entire lifetime of the server, all resource class instances — regardless of how many are created — share the same data.

Furthermore, because multiple HTTP requests can arrive simultaneously, thread safety is essential. A standard `HashMap` is not thread-safe and can produce corrupt or inconsistent data under concurrent access (e.g. two threads writing to the same map simultaneously can cause data loss). This project therefore uses `ConcurrentHashMap`, which is specifically designed for concurrent read/write operations without requiring explicit synchronisation blocks, preventing race conditions while maintaining high throughput.

---

#### Question 1.2: Why is Hypermedia (HATEOAS) considered a hallmark of advanced RESTful design? How does it benefit client developers compared to static documentation?

HATEOAS — Hypermedia as the Engine of Application State — is the principle that API responses should include navigational links to related resources and available actions, rather than requiring clients to construct URLs themselves or rely on external documentation.

For example, a response to `GET /api/v1` in this project includes `"rooms": "/api/v1/rooms"` and `"sensors": "/api/v1/sensors"`. A client can discover the entire API surface by following these links from the root, without prior knowledge of URL structures.

This benefits client developers in several important ways. First, it **reduces coupling** between client and server. If the server reorganises its URL structure, a HATEOAS client that follows links adapts automatically, whereas a client with hardcoded URLs breaks and requires a code change and redeployment. Second, it makes the API **self-documenting at runtime** — a developer can explore the API live rather than reading a potentially outdated document. Third, it enables the server to **communicate available actions contextually**: for example, if a room has sensors assigned, the server can omit the delete link, signalling to the client that deletion is not currently valid — without the client needing to know the business rule. This is more powerful than static documentation, which describes what is theoretically possible rather than what is valid right now for a specific resource in its current state.

---

### Part 2 — Room Management

#### Question 2.1: What are the implications of returning only IDs versus full room objects when listing rooms? Consider bandwidth and client-side processing.

**Returning only IDs** produces a very small response payload. For a campus with thousands of rooms, a list of string IDs is lightweight and fast to transmit. However, the client must then make a separate HTTP GET request for each room to retrieve its details — this is the N+1 problem. A client rendering a table of 200 rooms would need to make 201 HTTP requests, introducing significant latency, placing heavy load on the server, and degrading user experience.

**Returning full room objects** in the list response allows the client to render the entire interface with a single request. There is a larger payload to transmit, but modern networks handle this easily for typical collection sizes. The client receives all the data it needs immediately and can begin processing without any follow-up requests.

For this API, returning full room objects is the correct design. The expected clients — facilities managers and automated building systems — will always need the complete room data. The latency cost of multiple round trips far outweighs any bandwidth saving from returning IDs only. Should performance become a concern at scale, a `?fields=id,name` projection parameter could be introduced later to allow clients to request partial objects, offering the best of both approaches.

---

#### Question 2.2: Is the DELETE operation idempotent in your implementation? Justify your answer with what happens on multiple identical DELETE requests.

In this implementation, DELETE is **not strictly idempotent**, though it is close. The REST standard defines idempotency as: invoking the same operation multiple times produces the same outcome and response every time.

The first `DELETE /api/v1/rooms/{roomId}` on an existing, empty room removes it and returns `204 No Content`. A second identical request — after the room is already deleted — finds nothing in the data store and returns `404 Not Found`. The server state is the same after both calls (the room is absent), but the HTTP response codes differ (204 vs 404).

This is a widely accepted and pragmatic implementation of DELETE. Returning 404 on the second call is semantically honest and informative — the resource genuinely does not exist. An alternative design could return 204 on all DELETE calls regardless of whether the room existed, achieving strict idempotency at the cost of hiding the fact that nothing was actually deleted. The 404 approach is preferred in this project because it gives clients accurate feedback and is easier to debug in production.

---

### Part 3 — Sensor Operations & Linking

#### Question 3.1: What are the technical consequences if a client sends data in a format other than application/json to an endpoint annotated with @Consumes(APPLICATION_JSON)?

When a JAX-RS endpoint is annotated with `@Consumes(MediaType.APPLICATION_JSON)`, the framework inspects the `Content-Type` header of every incoming request before deciding which method to invoke. If a client sends a request with `Content-Type: text/plain` or `Content-Type: application/xml`, the JAX-RS runtime determines that no registered method is capable of consuming that media type and immediately returns an **HTTP 415 Unsupported Media Type** response. The resource method body is never reached and no business logic executes.

This behaviour is handled entirely by the JAX-RS framework with no custom code required. The `@Consumes` annotation acts as a gate at the routing layer. This is beneficial for several reasons: it rejects malformed or unexpected input before it can cause errors inside business logic, it enforces a consistent and explicit data contract between client and server, and it provides a clear and standardised error signal to client developers indicating exactly what went wrong and what they need to fix.

---

#### Question 3.2: Why is the @QueryParam approach generally superior to embedding the filter in the URL path for searching and filtering collections?

The `@QueryParam` approach (`GET /api/v1/sensors?type=CO2`) and the path-based approach (`GET /api/v1/sensors/type/CO2`) differ fundamentally in what they communicate semantically.

In REST, a URL path identifies a **resource** — a specific, addressable entity. The path `/api/v1/sensors/type/CO2` implies that `CO2` is a distinct resource with its own identity nested under the sensors collection, in the same way that `/api/v1/sensors/{sensorId}` identifies a specific sensor. This is semantically incorrect because `CO2` is not a resource; it is a filter criterion applied to a collection.

Query parameters are designed precisely for **filtering, searching, sorting, and paginating** collections. They are optional by nature — omitting `?type=CO2` returns all sensors, which is the correct unfiltered behaviour. URL path segments are not optional; their presence changes the meaning of the URL itself.

Query parameters are also more extensible. Future filtering requirements such as `?type=CO2&status=ACTIVE&roomId=LIB-301` can be layered on without changing the URL structure or adding new routes. A path-based approach would require a separate route for every combination of filters, making the API exponentially harder to maintain and document. Query parameters are the idiomatic REST solution for collection filtering.

---

### Part 4 — Deep Nesting with Sub-Resources

#### Question 4.1: Discuss the architectural benefits of the Sub-Resource Locator pattern compared to defining all nested paths in one controller class.

The Sub-Resource Locator pattern allows a resource class to delegate handling of a URL segment to a separate, dedicated class. In this project, `SensorResource` contains a locator method annotated only with `@Path("{sensorId}/readings")` — no HTTP method annotation — which returns an instance of `SensorReadingResource`. JAX-RS calls this method to obtain the delegate object and then routes the actual HTTP verb (GET, POST) to the matching method on that object.

The primary benefit is **separation of concerns**. Without this pattern, every endpoint across rooms, sensors, and readings would need to live inside one massive resource class. As the API grows, this class becomes increasingly difficult to read, modify, and test — a classic violation of the Single Responsibility Principle. By splitting responsibility into dedicated classes, each class has one clearly defined role.

This also greatly improves **testability**. `SensorReadingResource` can be instantiated directly in a unit test with a known sensorId, and its methods can be tested in complete isolation from the rest of the API. There is no need to spin up the full HTTP server or mock unrelated resource classes.

It enables **reusability and composability**. If reading history were needed from a different parent resource in a future version, `SensorReadingResource` could be returned by multiple locators without duplicating any logic.

Finally, the pattern allows the URL hierarchy (`/sensors/{id}/readings`) to mirror the real-world ownership relationship in code, making both the API and the codebase intuitive for developers to navigate. Large APIs built with a single-controller approach become unmaintainable; the locator pattern is the standard solution to this scaling problem.

---

### Part 5 — Advanced Error Handling & Exception Mapping

#### Question 5.1: Why is HTTP 422 often considered more semantically accurate than HTTP 404 when the issue is a missing reference inside a valid JSON payload?

HTTP 404 Not Found communicates that the **URL itself** does not correspond to any known resource on the server. The client constructed a path that the server has no handler for.

HTTP 422 Unprocessable Entity communicates that the **request URL was valid and understood**, the JSON body was syntactically well-formed and successfully parsed, but the **semantic content of the body** contains a logical error that prevents the request from being fulfilled.

When a client POSTs a new sensor with a `roomId` that does not exist in the system, the endpoint `/api/v1/sensors` is perfectly valid — the sensors collection exists and is reachable. The problem is not the URL. The problem is that the `roomId` field inside the JSON payload references an entity that the system has no record of. Returning 404 would incorrectly suggest to the client that the `/api/v1/sensors` endpoint itself was not found, which is false and misleading.

Returning 422 correctly communicates: "I found your endpoint, I parsed your JSON, but the reference inside it is logically invalid." This is more precise, more actionable, and more honest. The client developer can immediately understand they need to correct the `roomId` value in the request body rather than fix their URL, saving debugging time and reducing confusion.

---

#### Question 5.2: From a cybersecurity standpoint, explain the risks of exposing internal Java stack traces to external API consumers. What specific information could an attacker gather?

Exposing raw Java stack traces in API responses is a serious security vulnerability with several concrete attack surfaces.

**Framework and library version disclosure:** A stack trace reveals the exact names and versions of every framework in the call chain, for example `org.glassfish.jersey 2.41.0`, `com.fasterxml.jackson.databind 2.15.2`. An attacker can search CVE databases for known, unpatched vulnerabilities in those precise versions and craft targeted exploits with minimal effort.

**Internal package and class structure disclosure:** Stack traces expose the full package hierarchy and class names of the application, such as `com.smartcampus.store.DataStore.getSensor(DataStore.java:47)`. This gives the attacker a detailed architectural map of the codebase — which classes exist, what their responsibilities are, and approximately how data flows through the system. This dramatically reduces the reconnaissance work required before an attack.

**Server file path disclosure:** Stack traces commonly include absolute file system paths, such as `/home/ubuntu/SmartCampusAPI/src/main/java/...`. This reveals the operating system, the username of the account running the server, the deployment directory structure, and potentially the presence of other files. This information assists in directory traversal attacks, privilege escalation attempts, and social engineering.

**Business logic and data structure disclosure:** Error messages embedded in exceptions may inadvertently reveal SQL query fragments, internal identifiers, configuration keys, or algorithm details that were never intended to be visible externally, providing insight into exploitable logic flaws.

**The mitigation** implemented in this project is the global `ExceptionMapper<Throwable>` catch-all mapper, which intercepts every unhandled exception and returns only a generic `500 Internal Server Error` JSON response with a safe, uninformative message. The full exception and stack trace are written to a server-side log file accessible only to authorised developers, ensuring that no internal implementation details are ever transmitted to external API consumers.

---

