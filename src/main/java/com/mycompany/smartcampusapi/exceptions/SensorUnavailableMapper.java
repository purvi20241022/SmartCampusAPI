package com.mycompany.smartcampusapi.exceptions;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "403 Forbidden");
        error.put("message", ex.getMessage());
        error.put("sensorId", ex.getSensorId());
        return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(error).build();
    }
}