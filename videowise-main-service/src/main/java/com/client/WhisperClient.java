package com.client;

import com.pojo.HtmlConversionRequest;
import com.pojo.TranscriptionResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
@RegisterRestClient(configKey = "whisper-api")
public interface WhisperClient {
    @Path("/transcribe")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    TranscriptionResponse transcribe(String transcriptionRequest);

    @Path("/check_status/{transcription_token}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    TranscriptionResponse checkStatus(@PathParam("transcription_token") String transcriptionToken);

    @Path("/retrieve_output/{transcription_token}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    TranscriptionResponse retrieveOutput(@PathParam("transcription_token") String transcriptionToken);

    @Path("/html_to_pdf")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response convertHTMLtoPDF(HtmlConversionRequest htmlConversionRequest);

    @Path("/html_to_word")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response convertHTMLtoWORD(HtmlConversionRequest htmlConversionRequest);
}

