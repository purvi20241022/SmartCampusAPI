package com.mycompany.smartcampusapi;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api/v1")
public class SmartCampusApp extends Application {
    // Jersey auto-discovers @Path and @Provider classes in this package
}