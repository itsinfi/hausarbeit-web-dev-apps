package org.study.iu.jaxrs.classes;

import java.io.IOException;

import jakarta.json.JsonObject;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;

public abstract class TestRessource {

    protected abstract void post(@Suspended AsyncResponse res, JsonObject req) throws IOException;
    
    protected abstract JsonObject executeTest(JsonObject jsonInput) throws IOException;
}
