package org.study.iu.mysql.jaxrs.api.rest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.study.iu.mysql.jaxrs.db.DataSourceProvider;

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

    private static final String PARAM1 = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.\nDuis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis a";
    private static final Integer PARAM2 = 42;
    private static final String PARAM3 = "2025-01-01";

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
        int amount = req.getInt("amount", -1);

        if (amount <= 0) {
            JsonObject entity = Json
                    .createObjectBuilder()
                    .add("error", "Invalid or missing amount value")
                    .build();
                
            Response response = Response
                    .ok()
                    .entity(entity)
                    .build();
    
            asyncResponse.resume(response);
            return;
        }
        
        String query = "INSERT INTO example_table (param1, param2, param3) VALUES ";

        ArrayList<String> placeholders = new ArrayList<String>();
        ArrayList<Object> values = new ArrayList<Object>();

        for (int i = 0; i < amount; i++) {
            placeholders.add("(?, ?, ?)");
            values.add(PARAM1);
            values.add(PARAM2);
            values.add(PARAM3);
        }

        query += String.join(", ", placeholders);

        ArrayList<Integer> insertIds = new ArrayList<Integer>();

        try (
            Connection conn = DataSourceProvider.getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ) {
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }

            int affectedRows = stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            while (generatedKeys.next()) {
                insertIds.add(generatedKeys.getInt(1));
            }

            JsonObject entity = Json
                    .createObjectBuilder()
                    .add("msg", affectedRows + " new entries created successfully")
                    .add("insertedIds", Json.createArrayBuilder(insertIds))
                    .build();
                
            Response response = Response
                    .ok()
                    .entity(entity)
                    .build();
    
            asyncResponse.resume(response);
            return;
        } catch (SQLException e) {
            e.printStackTrace();

            JsonObject entity = Json
                    .createObjectBuilder()
                    .add("error", "Failed to insert data.")
                    .build();
            
            Response response = Response
                    .ok()
                    .entity(entity)
                    .build();
    
            asyncResponse.resume(response);
            return;
        }
    }
}
