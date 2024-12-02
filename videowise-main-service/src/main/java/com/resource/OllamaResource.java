package com.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.OllamaMessage;
import com.model.OllamaOptions;
import com.model.OllamaRequest;
import com.model.OllamaResponse;
import com.service.OllamaService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.microprofile.config.inject.ConfigProperty;


@Path("/api/ollama/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class OllamaResource {
    public static final String TEXT_EVENT_STREAM = "text/event-stream";

    @Inject
    OllamaService ollamaService;

    @ConfigProperty(name = "quarkus.ollama.\"ollama-model\"")
    String model;

    @ConfigProperty(name = "quarkus.ollama.\"context-length\"")
    int context_length;

    @ConfigProperty(name = "quarkus.ollama.\"max-pred-length\"")
    int max_prediction_length;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(TEXT_EVENT_STREAM)
    public Response chat(ArrayList<OllamaMessage> messages) {
        // Define Ollama configuration
        OllamaOptions options = new OllamaOptions();
        options.setNum_ctx(context_length);
        options.setNum_predict(max_prediction_length);

        OllamaRequest ollamaRequest = new OllamaRequest(model, messages, options);
        //System.out.println("Ollama request:" + ollamaRequest.model + ollamaRequest.messages + ollamaRequest.options);
        Response response = ollamaService.getOllamaResponse(ollamaRequest);
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            // Create a StreamingOutput to handle the response streaming
            StreamingOutput streamingOutput = output -> {
                try (InputStream inputStream = (InputStream) response.getEntity();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                    String jsonChunk;
                    while ((jsonChunk = reader.readLine()) != null) {
                        // Handle each JSON chunk
                        OllamaResponse ollamaResponse = parseJsonChunk(jsonChunk);
                        //System.out.println("Sending message part: " + ollamaResponse.getMessage().getContent());

                        // Send the content piece by piece
                        String messageContent = ollamaResponse.getMessage().getContent();
                        output.write(messageContent.getBytes());
                        output.flush(); // Ensure the data is sent immediately

                        // Break if the done flag is true
                        if (ollamaResponse.isDone()) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            return Response.ok(streamingOutput).build();
        } else {
            // Handle non-200 responses
            throw new RuntimeException("Failed to send chat request: " + response.getStatus());
        }
    }

    @GET
    @Path("/model/{model_name}")
    public Response changeOllamaModel(@PathParam("model_name") String modelName) {
        // Define Ollama configuration
        OllamaOptions options = new OllamaOptions();
        options.setNum_ctx(context_length);
        options.setNum_predict(max_prediction_length);

        // Create test message
        ArrayList<OllamaMessage> messages = new ArrayList<>();
        OllamaMessage testMessage = new OllamaMessage("user", "Are you there? Just answer YES or NO, nothing else");
        messages.add(testMessage);

        // Build the request
        OllamaRequest ollamaRequest = new OllamaRequest(modelName, messages, options);
        // Get response from the service
        try {
            Response response = ollamaService.getOllamaResponse(ollamaRequest);

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                model = modelName;
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Model is not available for chat")
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while processing the request: " + e.getMessage())
                    .build();
        }
    }

    private OllamaResponse parseJsonChunk(String jsonChunk) {
        // Use Jackson to parse the JSON string
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonChunk, OllamaResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON chunk", e);
        }
    }
}
