package com.mycompany.smartcampusapi;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import java.net.URI;
import java.io.IOException;

@ApplicationPath("/api/v1")
public class SmartCampusAPI extends Application {

    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    public static HttpServer startServer() {
        ResourceConfig config = new ResourceConfig()
            .packages("com.mycompany.smartcampusapi.resources",
                      "com.mycompany.smartcampusapi.exceptions");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = startServer();
        System.out.println("====================================");
        System.out.println("Smart Campus API is running!");
        System.out.println("URL: " + BASE_URI);
        System.out.println("Press ENTER to stop the server...");
        System.out.println("====================================");
        System.in.read();
        server.stop();
    }
}