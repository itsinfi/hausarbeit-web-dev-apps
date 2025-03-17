package org.study.iu.jaxrs.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("api")
public class RestApplication extends Application {
    // Needed to enable Jakarta REST and specify path.
}