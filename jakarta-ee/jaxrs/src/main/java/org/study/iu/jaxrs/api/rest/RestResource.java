package org.study.iu.jaxrs.api.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("rest")
public class RestResource {

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public void get(@Suspended AsyncResponse asyncResponse) {
        JsonObject entity = Json
                .createObjectBuilder()
                .add("msg", "Hello World!")
                .build();
            
        Response response = Response
                .ok()
                .entity(entity)
                .build();

        asyncResponse.resume(response);
        return;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public void post(@Suspended AsyncResponse asyncResponse, JsonObject req) {
        JsonObject entity = Json
                .createObjectBuilder()
                .add("msg", "Hello World!")
                .build();

        Response response = Response
                .ok()
                .entity(entity)
                .build();

        asyncResponse.resume(response);
        return;
    }
}
