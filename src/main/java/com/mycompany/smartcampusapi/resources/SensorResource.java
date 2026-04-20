package com.mycompany.smartcampusapi.resources;

import com.mycompany.smartcampusapi.data.DataStore;
import com.mycompany.smartcampusapi.exceptions.LinkedResourceNotFoundException;
import com.mycompany.smartcampusapi.models.Sensor;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // -------------------------------------------------------------------------
    // GET /sensors?type={type} — return all sensors, optionally filtered by type
    //
    // @QueryParam is correct here because:
    //   - /sensors            → all sensors (param is absent, not a path segment)
    //   - /sensors?type=CO2   → filtered view of the same collection
    // A path design like /sensors/type/CO2 would imply CO2 is a named
    // sub-resource with its own identity — semantically wrong. Query params
    // are optional by nature; path segments are not.
    // -------------------------------------------------------------------------
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>(store.getSensors().values());

        if (type != null && !type.isBlank()) {
            result.removeIf(s -> !s.getType().equalsIgnoreCase(type));
        }

        return Response.ok(result).build();
    }

    // -------------------------------------------------------------------------
    // POST /sensors — register a new sensor
    //
    // Flow:
    //   1. Validate roomId — throw LinkedResourceNotFoundException (404) if the
    //      referenced room does not exist in DataStore
    //   2. Generate UUID id, persist sensor
    //   3. Add sensorId to the room's sensorIds list (bidirectional link)
    //   4. Initialise an empty readings list keyed by sensorId
    //   5. Return 201 Created with Location header → /sensors/{id}
    // -------------------------------------------------------------------------
    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {

        // Step 1 — validate that the referenced room exists
        if (sensor.getRoomId() == null || !store.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(sensor.getRoomId());
        }

        // Step 2 — assign identity and persist
        sensor.setId(UUID.randomUUID().toString());
        store.getSensors().put(sensor.getId(), sensor);

        // Step 3 — keep the room's sensorIds list in sync
        store.getRooms().get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        // Step 4 — initialise empty reading history for this sensor
        store.getReadings().put(sensor.getId(), new ArrayList<>());

        // Step 5 — return 201 with Location: /sensors/{id}
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(sensor.getId())
                .build();

        return Response.created(location).entity(sensor).build();
    }

    // -------------------------------------------------------------------------
    // GET /sensors/{sensorId} — fetch a single sensor by ID
    // Throws NotFoundException (auto-mapped to 404) if not found
    // -------------------------------------------------------------------------
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }
        return Response.ok(sensor).build();
    }

    // -------------------------------------------------------------------------
    // Sub-resource locator — /sensors/{sensorId}/readings
    //
    // JAX-RS delegates all /readings/* requests to SensorReadingResource.
    // No @GET/@POST here — the sub-resource class owns those verbs.
    // The sensorId is passed in so the sub-resource can scope its DataStore
    // lookups to just this sensor's reading history.
    // -------------------------------------------------------------------------
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
