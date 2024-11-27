package com.client;

import com.form.AudioTranscriptionForm;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;

import java.io.InputStream;

@Path("/")
@RegisterRestClient(configKey = "filesystem-api")
public interface FileSystemClient {
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response upload(MultipartFormDataOutput multipartForm);

    @GET
    @Path("/delete/{filename}")
    @Produces(MediaType.TEXT_PLAIN)
    Response delete(@PathParam("filename") String filename);

    @POST
    @Path("/upload_stream")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    Response uploadStream(@HeaderParam("title") String title, InputStream fileStream);

    @POST
    @Path("/extract_audio")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response extractAudio(@MultipartForm AudioTranscriptionForm audioTranscriptionForm);

    @GET
    @Path("/download/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response download(@PathParam("filename") String filename);
}