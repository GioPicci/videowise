package com.service;

import com.client.OllamaClient;
import com.model.OllamaRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class OllamaService {

    @RestClient
    OllamaClient ollamaClient;

    public Response getOllamaResponse(OllamaRequest ollamaRequest) {
        return ollamaClient.sendChatRequest(ollamaRequest);
    }
}
