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

