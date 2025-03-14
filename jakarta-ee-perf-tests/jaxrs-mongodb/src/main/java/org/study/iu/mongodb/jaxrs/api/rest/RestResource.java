package org.study.iu.mysql.jaxrs.api.rest;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertManyOptions;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                    .status(Response.Status.BAD_REQUEST)
                    .entity(entity)
                    .build();

            asyncResponse.resume(response);
            return;
        }
        
        ArrayList<Document> documents = new ArrayList<Document>();
        for (int i = 0; i < amount; i++) {
            documents.add(
                    new Document("param1", PARAM1)
                            .append("param2", PARAM2)
                            .append("param3", PARAM3)
            );
        }

        try (MongoClient client = DataSourceProvider.getMongoClient()) {
            MongoDatabase database = client.getDatabase(DataSourceProvider.DB_NAME);
            MongoCollection<Document> collection = database.getCollection(DataSourceProvider.COLLECTION_NAME);
            
            
            List<ObjectId> insertedIds = collection.insertMany(documents)
                    .getInsertedIds()
                    .values()
                    .stream()
                    .map(v -> v.asObjectId().getValue())
                    .collect(Collectors.toList());
            
            JsonObject entity = Json
                    .createObjectBuilder()
                    .add("msg", amount + " new entries created successfully")
                    .add("insertedIds", Json.createArrayBuilder(
                        insertedIds.stream().map(ObjectId::toHexString).toList()
                    ))
                    .build();
                
            Response response = Response
                    .ok()
                    .entity(entity)
                    .build();

            asyncResponse.resume(response);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());

            JsonObject entity = Json
                    .createObjectBuilder()
                    .add("error", "Failed to insert data.")
                    .build();
            
            Response response = Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(entity)
                    .build();

            asyncResponse.resume(response);
            return;
        }
    }
}
