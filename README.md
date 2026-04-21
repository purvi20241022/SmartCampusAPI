# Smart Campus Sensor & Room Management API

A RESTful API built with JAX-RS (Jersey 2) and Grizzly HTTP server for managing rooms and sensors across a university smart campus. This project was developed as coursework for the 5COSC022W Client-Server Architectures module at the University of Westminster.

---

## API Overview

The Smart Campus API provides a versioned REST interface at `/api/v1`. It manages three core resources:

- **Rooms** — physical spaces on campus (e.g. labs, libraries)
- **Sensors** — hardware deployed inside rooms (temperature, CO2, occupancy)
- **Sensor Readings** — historical measurement logs per sensor

The API follows RESTful principles including resource-based URLs, correct HTTP status codes, JSON request/response bodies, and structured error handling via custom exception mappers.

### Resource Hierarchy

```
/api/v1
├── /rooms
│   ├── GET    /              → list all rooms
│   ├── POST   /              → create a new room
│   ├── GET    /{roomId}      → get a specific room
│   └── DELETE /{roomId}      → delete a room (blocked if sensors exist)
├── /sensors
│   ├── GET    /              → list all sensors (optional ?type= filter)
│   ├── POST   /              → register a new sensor (validates roomId)
│   ├── GET    /{sensorId}    → get a specific sensor
│   └── /{sensorId}/readings
│       ├── GET  /            → get reading history for sensor
│       └── POST /            → add a new reading (updates sensor currentValue)
```

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 11+ |
| JAX-RS Implementation | Jersey 2.41.0 |
| HTTP Server | Grizzly2 |
| JSON Serialisation | Jackson (jersey-media-json-jackson) |
| Build Tool | Apache Maven |
| IDE | Apache NetBeans 28 |

---

## How to Build and Run

### Prerequisites

- Java JDK 11 or higher installed
- Apache Maven 3.6+ installed
- Apache NetBeans IDE (recommended) or any Maven-compatible IDE
- Internet connection (to download Maven dependencies on first run)

### Option 1 — Run from NetBeans (recommended)

1. Open NetBeans
2. Go to **File → Open Project**
3. Navigate to the `SmartCampusAPI` folder and open it
4. Wait for Maven to download all dependencies (first time only — check the Output window)
5. Right-click the project in the Projects panel → **Clean and Build**
6. Right-click the project → **Run**
7. The Output window will show:

```
Smart Campus API is running!
URL: http://localhost:8080/api/v1/
Press ENTER to stop the server...
```

8. The API is now live at `http://localhost:8080/api/v1/`

### Option 2 — Run from terminal

```bash
# Clone the repository
git clone https://github.com/purvi20241022/SmartCampusAPI.git
cd SmartCampusAPI

# Build the project
mvn clean package

# Run the server
mvn exec:java -Dexec.mainClass="com.mycompany.smartcampusapi.SmartCampusAPI"
```

### Stopping the server

Press **ENTER** in the NetBeans Output window (or terminal) to stop the Grizzly server gracefully.

---

## Sample Data (Pre-loaded)

The API comes with sample data loaded at startup for immediate testing:

| Type | ID | Details |
|---|---|---|
| Room | `LIB-301` | Library Quiet Study, capacity 50 |
| Room | `LAB-101` | Computer Lab, capacity 30 |
| Sensor | `TEMP-001` | Temperature, ACTIVE, in LIB-301 |
| Sensor | `CO2-001` | CO2, ACTIVE, in LAB-101 |

---

## curl Command Examples

### 1. Discovery endpoint — GET /api/v1/

```bash
curl -X GET http://localhost:8080/api/v1/
```

Expected response (200 OK):
```json
{
  "version": "1.0",
  "description": "Smart Campus Sensor & Room Management API",
  "links": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

---

### 2. Get all rooms — GET /api/v1/rooms

```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

Expected response (200 OK):
```json
[
  { "id": "LIB-301", "name": "Library Quiet Study", "capacity": 50, "sensorIds": ["TEMP-001"] },
  { "id": "LAB-101", "name": "Computer Lab", "capacity": 30, "sensorIds": ["CO2-001"] }
]
```

---

### 3. Create a new room — POST /api/v1/rooms

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Physics Lab", "capacity": 40}'
```

Expected response (201 Created):
```json
{
  "id": "a3f2c1d0-...",
  "name": "Physics Lab",
  "capacity": 40,
  "sensorIds": []
}
```

---

### 4. Filter sensors by type — GET /api/v1/sensors?type=CO2

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

Expected response (200 OK):
```json
[
  { "id": "CO2-001", "type": "CO2", "status": "ACTIVE", "currentValue": 400.0, "roomId": "LAB-101" }
]
```

---

### 5. Register a new sensor — POST /api/v1/sensors

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "Occupancy", "status": "ACTIVE", "currentValue": 0.0, "roomId": "LIB-301"}'
```

Expected response (201 Created):
```json
{
  "id": "b7e9d2f1-...",
  "type": "Occupancy",
  "status": "ACTIVE",
  "currentValue": 0.0,
  "roomId": "LIB-301"
}
```

---

### 6. Post a sensor reading — POST /api/v1/sensors/TEMP-001/readings

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 23.7}'
```

Expected response (201 Created):
```json
{
  "id": "c4a1b2d3-...",
  "timestamp": 1745123456789,
  "value": 23.7
}
```

---

### 7. Get reading history — GET /api/v1/sensors/TEMP-001/readings

```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

Expected response (200 OK):
```json
[
  { "id": "c4a1b2d3-...", "timestamp": 1745123456789, "value": 23.7 }
]
```

---

### 8. Delete a room with sensors (expect 409) — DELETE /api/v1/rooms/LIB-301

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

Expected response (409 Conflict):
```json
{
  "error": "409 Conflict",
  "message": "Room LIB-301 still has sensors assigned to it.",
  "roomId": "LIB-301"
}
```

---

### 9. Register sensor with invalid roomId (expect 422) — POST /api/v1/sensors

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "Temperature", "status": "ACTIVE", "currentValue": 20.0, "roomId": "FAKE-999"}'
```

Expected response (422 Unprocessable Entity):
```json
{
  "error": "422 Unprocessable Entity",
  "message": "Room not found with id: FAKE-999",
  "invalidRoomId": "FAKE-999"
}
```

---

### 10. Post reading to MAINTENANCE sensor (expect 403)

First update the sensor status to MAINTENANCE (via Postman or a PUT if implemented), then:

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 30.0}'
```

Expected response (403 Forbidden):
```json
{
  "error": "403 Forbidden",
  "message": "Sensor TEMP-001 is under MAINTENANCE and cannot accept readings.",
  "sensorId": "TEMP-001"
}
```

---

## Report: Written Question Answers

### Part 1.1 — JAX-RS Resource Class Lifecycle

By default, JAX-RS creates a **new instance of every resource class for each incoming HTTP request**. This is called per-request scope. The runtime instantiates the class, processes the request, and then discards the instance. There is no shared state between requests at the resource class level.

This has a critical implication for in-memory data storage. If data were stored as instance fields inside a resource class (e.g. `private Map<String, Room> rooms = new HashMap<>()`), that map would be created fresh on every request and immediately discarded — no data would ever persist between calls.

To solve this, the DataStore is implemented as a **singleton** using the classic `getInstance()` pattern with a `private static` instance and a `synchronized` method to prevent race conditions during initialisation. The `ConcurrentHashMap` is used instead of `HashMap` because multiple threads (one per request) may access or modify the map simultaneously. `ConcurrentHashMap` provides thread-safe reads and writes without requiring explicit `synchronized` blocks on every operation, making it both safe and performant for this concurrent access pattern.

---

### Part 1.2 — HATEOAS (Hypermedia as the Engine of Application State)

HATEOAS is a constraint of REST architecture where API responses include hyperlinks that guide clients to related resources and available actions. Rather than relying on external documentation, clients discover what they can do next from the links embedded in each response.

For example, the discovery endpoint at `GET /api/v1` returns links to `/api/v1/rooms` and `/api/v1/sensors`. A client that receives this response does not need to know these paths in advance — it reads them from the response and navigates dynamically.

The key benefit over static documentation is **decoupling**. If the API relocates an endpoint (e.g. from `/rooms` to `/spaces`), clients following HATEOAS links adapt automatically without code changes. Static documentation requires client developers to update hardcoded URLs, redeploy, and retest. HATEOAS also makes APIs self-describing and exploratory — a new developer can start at the root and discover the entire API structure just by following links, similar to browsing a website.

---

### Part 2.1 — Returning IDs vs Full Objects in Lists

When returning a list of rooms, two approaches exist: returning only IDs (e.g. `["LIB-301", "LAB-101"]`) or returning full room objects with all fields.

Returning **only IDs** minimises payload size, which benefits performance when there are thousands of rooms and clients only need to display a list of names or perform a lookup. However, it forces clients to make a second request per room (`GET /rooms/{id}`) to retrieve details, multiplying network round-trips and increasing latency significantly — known as the N+1 problem.

Returning **full objects** increases payload size but allows clients to render a complete list view from a single request. This is the preferred approach for this API since the Room object is small and campus facility managers typically need name, capacity, and sensor count at a glance. For very large datasets, pagination (`?page=1&size=20`) would be the appropriate mitigation rather than ID-only responses.

---

### Part 2.2 — Idempotency of DELETE

The DELETE operation in this implementation is **not strictly idempotent** by HTTP response code, though the server state is idempotent.

- First DELETE on a valid empty room → **204 No Content** (room removed)
- Second DELETE on the same roomId → **404 Not Found** (room already gone)

Strict idempotency would require the same response code both times (204). However, returning 404 on the second call is a deliberate design choice: it gives the caller accurate information about whether the DELETE actually performed work or was a no-op. In a campus monitoring system where operators manage hundreds of rooms, knowing that a room was already absent (404) versus freshly removed (204) is operationally useful — it helps diagnose duplicate requests and audit trails. The underlying server state IS idempotent: after either call, the room does not exist in DataStore.

---

### Part 3.1 — @Consumes Mismatch Behaviour

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells Jersey that the POST method only accepts requests with a `Content-Type: application/json` header.

If a client sends data with a different content type — for example `Content-Type: text/plain` or `Content-Type: application/xml` — Jersey cannot find a matching method that accepts that media type. It immediately returns **HTTP 415 Unsupported Media Type** before the method body is ever executed. Jackson's `MessageBodyReader` is not invoked, and no deserialisation is attempted. The client receives a clear error indicating it must send JSON. This protects the API from malformed or unexpected input formats without any manual checking inside the method.

---

### Part 3.2 — @QueryParam vs Path-Based Filtering

Using `@QueryParam("type")` for filtering sensors (`GET /sensors?type=CO2`) is semantically superior to a path-based design (`GET /sensors/type/CO2`) for several reasons.

Query parameters are **optional by design** — their absence means "no filter, return all". Path segments are mandatory structural components that imply a distinct resource identity. `/sensors/type/CO2` suggests that `CO2` is a named sub-resource of a `type` collection, which is semantically wrong — CO2 is a filter criterion, not a resource.

Query parameters also compose naturally: `?type=CO2&status=ACTIVE` is intuitive and standard. Path-based filters cannot compose without increasingly complex URL structures. Additionally, REST caching and URL semantics treat path segments as resource identifiers — using them for filtering pollutes the resource namespace and makes the API harder to evolve. The query parameter approach follows established REST conventions used by major APIs including GitHub, Twitter, and Google.

---

### Part 4.1 — Sub-Resource Locator Pattern Benefits

The sub-resource locator pattern delegates routing to a separate class by returning an object instance from a method annotated only with `@Path` — no HTTP method annotation is present. JAX-RS calls this method to obtain the delegate, then routes the actual HTTP verb (GET, POST, etc.) to that object's methods.

The architectural benefit is **separation of concerns**. In a large API with many nested resources, placing every path variant (`/sensors/{id}/readings`, `/sensors/{id}/readings/{rid}`, `/sensors/{id}/calibrations`, etc.) in a single `SensorResource` class creates a massive, hard-to-maintain controller. Each sub-resource locator delegates to a focused class that only handles one slice of the API surface.

This also improves testability — `SensorReadingResource` can be unit-tested independently by constructing it directly with a `sensorId`, without needing to bootstrap the full JAX-RS runtime. It mirrors standard software design principles: single responsibility, high cohesion, low coupling. As the Smart Campus API grows to include calibration records, maintenance logs, and alert thresholds per sensor, each concern gets its own resource class rather than accumulating in one monolithic file.

---

### Part 5.2 — HTTP 422 vs 404 for Missing roomId Reference

When a client POSTs a new sensor with a `roomId` that does not exist in the system, returning **422 Unprocessable Entity** is semantically more accurate than 404.

HTTP **404 Not Found** means the requested URL resource does not exist — the path `/api/v1/sensors` is perfectly valid and was found. The problem is not the URL.

HTTP **422 Unprocessable Entity** means the request was syntactically correct JSON, was successfully parsed, but contains a semantic error — a field value that violates a business rule. In this case, the `roomId` field references an entity that does not exist. The JSON is valid, the endpoint exists, but the payload cannot be processed as-is because of the broken reference.

422 gives client developers a precise signal: "your JSON is fine but something inside it is wrong." This directs debugging to the payload content rather than the URL, making API errors easier to act on. 404 would be misleading because it implies the URL was wrong, sending developers to check the wrong thing.

---

### Part 5.4 — Security Risks of Exposing Stack Traces

Exposing raw Java stack traces to external API consumers creates several concrete security risks:

**Framework and version disclosure** — a stack trace reveals the exact library names and versions in use (e.g. `org.glassfish.jersey 2.41`, `com.fasterxml.jackson 2.15`). Attackers cross-reference these against published CVE databases to find known vulnerabilities and exploit them directly.

**Internal package structure** — fully qualified class names reveal the internal architecture of the application (e.g. `com.mycompany.smartcampusapi.data.DataStore`). This tells an attacker exactly how the code is organised, making it easier to reason about attack surfaces and craft targeted exploits.

**File system paths** — stack traces often include absolute file paths (e.g. `C:\Users\ASUS\Documents\...`). These reveal the operating system, directory structure, and usernames on the server, aiding privilege escalation or social engineering attacks.

**Business logic leakage** — method names and class hierarchies in a trace reveal what the code is trying to do, exposing logic that should remain opaque to external consumers.

The `GlobalExceptionMapper` solves this by catching all `Throwable` instances and returning a generic 500 JSON response with no internal detail — the exception is logged server-side where only authorised personnel can see it, while clients receive only a safe, generic message.

---

## GitHub Repository

[https://github.com/purvi20241022/SmartCampusAPI](https://github.com/purvi20241022/SmartCampusAPI)

## Author

purvi20241022 — University of Westminster, 5COSC022W Client-Server Architectures, 2025/26