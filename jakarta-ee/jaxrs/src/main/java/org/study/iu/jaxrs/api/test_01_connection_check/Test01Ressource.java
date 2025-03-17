package org.study.iu.jaxrs.api.test_01_connection_check;

import java.io.IOException;

import org.study.iu.jaxrs.classes.TestRessource;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("01")
public class Test01Ressource extends TestRessource {

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Override
    public void post(@Suspended AsyncResponse res, JsonObject req) throws IOException {
        if (req.containsKey("name")) {
            final JsonObject entity = executeTest(req);

            Response response = Response
                    .ok()
                    .entity(entity)
                    .build();

            res.resume(response);
        } else {
            res.resume(Response.noContent().build());
        }
    }
    
    @Override
    protected JsonObject executeTest(JsonObject jsonInput) {
        final String name = jsonInput.getString("name");

        return Json.createObjectBuilder()
                .add("result", "Hello " + name + "!")
                .build();
    }
}