package org.study.iu.mongodb.jaxrs.api;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("api")
public class RestApplication extends Application {
    // Needed to enable Jakarta REST and specify path.
}