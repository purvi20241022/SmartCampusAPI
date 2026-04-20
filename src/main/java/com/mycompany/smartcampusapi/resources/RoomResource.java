package com.mycompany.smartcampusapi.resources;

import com.mycompany.smartcampusapi.data.DataStore;
import com.mycompany.smartcampusapi.exceptions.RoomNotEmptyException;
import com.mycompany.smartcampusapi.models.Room;
import com.mycompany.smartcampusapi.models.Sensor;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // -------------------------------------------------------------------------
    // GET /rooms — return all rooms as a JSON array
    // -------------------------------------------------------------------------
    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(store.getRooms().values());
        return Response.ok(roomList).build();
    }

    // -------------------------------------------------------------------------
    // POST /rooms — create a new room
    // Validates that name is present, assigns a UUID, saves, returns 201 + Location
    // -------------------------------------------------------------------------
    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room.getName() == null || room.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Room name is required\"}")
                    .build();
        }

        room.setId(UUID.randomUUID().toString());
        store.getRooms().put(room.getId(), room);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(room.getId())
                .build();

        return Response.created(location).entity(room).build();
    }

    // -------------------------------------------------------------------------
    // GET /rooms/{roomId} — fetch a single room by ID
    // Throws NotFoundException (auto-mapped to 404) if not found
    // -------------------------------------------------------------------------
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found: " + roomId);
        }
        return Response.ok(room).build();
    }

    // -------------------------------------------------------------------------
    // DELETE /rooms/{roomId} — remove a room with safety checks
    //
    // Flow:
    //   1. 404 NotFoundException      — room does not exist in DataStore
    //   2. 409 RoomNotEmptyException  — at least one sensor still assigned
    //   3. 204 No Content             — room removed successfully
    //
    // Idempotency note:
    //   This DELETE is NOT strictly idempotent by HTTP status code:
    //     - 1st call on a valid empty room  → 204 No Content
    //     - 2nd call on the same roomId    → 404 Not Found
    //   The server state IS idempotent (room is absent after both calls), but
    //   the response code differs. This is a deliberate design choice:
    //   returning 404 on the second call gives the caller accurate information
    //   about what happened, which is more useful in a campus monitoring system
    //   where operators need to know whether a DELETE actually did work or was
    //   a no-op. A strictly idempotent 204-always variant is noted below.
    // -------------------------------------------------------------------------
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {

        // Step 1 — existence check
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            // Chosen design: 404 on repeat deletes (informative, not strictly idempotent)
            // Strict-idempotent alternative: return Response.noContent().build();
            throw new NotFoundException("Room not found: " + roomId);
        }

        // Step 2 — safety check: reject if any sensor is still assigned to this room
        for (Sensor sensor : store.getSensors().values()) {
            if (roomId.equals(sensor.getRoomId())) {
                throw new RoomNotEmptyException(roomId);
                // RoomNotEmptyException must be mapped to 409 Conflict
                // via an ExceptionMapper<RoomNotEmptyException>
            }
        }

        // Step 3 — safe to remove
        store.getRooms().remove(roomId);
        return Response.noContent().build();
    }
}
