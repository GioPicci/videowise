package com.resource;

import com.client.FileSystemClient;
import com.model.Chat;
import com.model.SimplifiedVideo;
import com.model.Video;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.logging.Log;
import io.smallrye.context.api.NamedInstance;
import io.vertx.core.json.JsonObject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Path("/api/videos")
@ApplicationScoped
public class VideoResource implements PanacheRepository<Video> {

    @RestClient
    FileSystemClient fileSystemClient;

    @ConfigProperty(name = "quarkus.\"filesystem-streaming-api\"")
    String filesystemUrl;

    @Inject
    @NamedInstance("myExecutor")
    ManagedExecutor executor;

    @Inject
    EntityManager em;


    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadVideo(@MultipartForm MultipartFormDataInput input) throws IOException {
        String title;
        String metadata;
        long chatId;

        System.out.println("Starting uploadVideo method...");

        // Extract title
        try {
            //System.out.println("Attempting to read 'title' from form data...");
            List<InputPart> titleParts = input.getFormDataMap().get("title");
            title = titleParts.get(0).getBodyAsString();
            //System.out.println("Title extracted: " + title);
        } catch (Exception e) {
            System.out.println("Error reading 'title' from form data: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error reading form data: " + e.getMessage())
                    .build();
        }

        // Extract metadata
        try {
            List<InputPart> metadataParts = input.getFormDataMap().get("metadata");
            metadata = metadataParts.get(0).getBodyAsString();
        } catch (Exception e) {
            System.out.println("Error reading 'metadata' from form data: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error reading form data: " + e.getMessage())
                    .build();
        }

        // Extract chat ID
        try {
            List<InputPart> chatIdParts = input.getFormDataMap().get("chat_id");
            String chatIdString = chatIdParts.get(0).getBodyAsString();
            chatId = Long.parseLong(chatIdString);
        } catch (Exception e) {
            System.out.println("Error reading 'chat_id' from form data: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error reading form data (chat_id): " + e.getMessage())
                    .build();
        }

        // Fetch the Chat in a separate transaction
        Chat chat = findChatById(chatId);
        if (chat == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Chat not found")
                    .build();
        }

        // Generate unique filename in a separate transaction
        String filename = generateUniqueFileName(title);

        // Extract file stream
        InputStream fileStream;
        try {
            List<InputPart> fileParts = input.getFormDataMap().get("file");
            fileStream = fileParts.get(0).getBody(new GenericType<InputStream>() {});
        } catch (Exception e) {
            System.out.println("Error reading 'file' from form data: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error reading form data: " + e.getMessage())
                    .build();
        }

        // Create and persist the Video in a separate transaction
        Video video = createAndPersistVideo(filename, chat, title, metadata);

        // Trigger async upload
        System.out.println("Starting asynchronous upload for video ID: " + video.getId());
        uploadVideoAsync(video.getId(), fileStream, filename);

        // Return success response
        System.out.println("Upload process initiated successfully. Returning response...");
        return Response.ok(new JsonObject()
                .put("id", video.getId())
                .put("filename", video.getVideoPath())
                .put("status", video.getStatus()))
                .build();
    }


    // Method to find Chat entity in a separate transaction
    @Transactional
    public Chat findChatById(long chatId) {
        System.out.println("Checking if chat with ID " + chatId + " exists...");
        return Chat.findById(chatId);
    }


    // Method to generate a unique filename in a separate transaction
    @Transactional
    public String generateUniqueFileName(String title) {
        System.out.println("Checking and modifying filename if necessary...");
        String title_no_spaces = title.replaceAll(" ", "_").toLowerCase();
        return checkAndModifyFileNameDB(title_no_spaces);
    }


    // Method to create and persist Video in a separate transaction
    @Transactional
    public Video createAndPersistVideo(String filename, Chat chat, String title, String metadata) {
        System.out.println("Creating new Video entity...");
        Video video = new Video();
        video.setVideoPath(filename);
        video.setChat(chat);
        video.setVideoTitle(title);
        video.setVideoMetadata(metadata);
        video.setStatus("UPLOADING");

        System.out.println("Persisting Video entity to the database...");
        video.persist();
        System.out.println("Video persisted with ID: " + video.getId());
        return video;
    }


    @GET
    @Path("/{video_id}")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVideoStreamUrl(@PathParam("video_id") Long videoId) {
        Video video = Video.findById(videoId);
        if (video == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK)
                    .entity(filesystemUrl + "/stream/" + video.getVideoPath())
                    .build();
    }


    @GET
    @Path("/{video_id}/export")
    @Transactional
    @Produces("application/zip")
    public Response exportVideoWithSubtitles(@PathParam("video_id") Long videoId) {
        try {
            Video video = Video.findById(videoId);
            if (video == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Video not found")
                        .build();
            }
            String videoFilename = video.getVideoPath();
            String subtitlesFilename = videoFilename.substring(0, videoFilename.lastIndexOf('.')) + ".ass";

            // Download video file
            Response videoResponse = fileSystemClient.download(videoFilename);
            if (videoResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Video file not found in remote storage")
                    .build();
            }

            // Download subtitles file
            Response subtitlesResponse = fileSystemClient.download(subtitlesFilename);
            if (subtitlesResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Subtitles file not found in storage")
                    .build();
            }

            final Response finalVideoResponse = videoResponse;
            final Response finalSubtitlesResponse = subtitlesResponse;

             // Create a StreamingOutput to write the zip file
            StreamingOutput streamingOutput  = new StreamingOutput() {
                @Override
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    try (
                            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(output));
                            InputStream videoStream = (InputStream) finalVideoResponse.getEntity();
                            BufferedInputStream bufferedVideoStream = new BufferedInputStream(videoStream);
                            InputStream subtitlesStream = (InputStream) finalSubtitlesResponse.getEntity();
                            BufferedInputStream bufferedSubtitlesStream = new BufferedInputStream(subtitlesStream)
                    ) {
                        // Add video file to zip
                        ZipEntry videoEntry = new ZipEntry(videoFilename);
                        zos.putNextEntry(videoEntry);
                        byte[] buffer = new byte[8192];
                        int length;
                        while ((length = bufferedVideoStream.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }
                        zos.closeEntry();

                        // Add subtitles file to zip
                        ZipEntry subtitlesEntry = new ZipEntry(subtitlesFilename);
                        zos.putNextEntry(subtitlesEntry);
                        while ((length = bufferedSubtitlesStream.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }
                        zos.closeEntry();

                    } catch (IOException e) {
                        throw new WebApplicationException("Error creating zip file", e);
                    } finally {
                        // Close the response objects to release resources
                        finalVideoResponse.close();
                        finalSubtitlesResponse.close();
                    }
                }
            };

            // Generate zip filename
            String zipFilename = videoFilename.substring(0, videoFilename.lastIndexOf('.')) + "_with_subtitles.zip";

            // Return streaming response
            return Response.ok(streamingOutput)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFilename + "\"")
                .build();

        } catch (Exception e) {
            return  Response.serverError()
                    .entity("Error exporting video: " + e.getMessage())
                    .build();
        }
    }


    @GET
    @Path("/status/{video_id}")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVideoStatus(@PathParam("video_id") Long videoId) {
        Video video = Video.findById(videoId);
        if (video == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK)
                    .entity(video.getStatus())
                    .build();
    }


    @GET
    @Path("/chat/{chat_id}") // New endpoint for getting videos by chat ID
    @Transactional
    public Response getVideosByChatId(@PathParam("chat_id") Long chatId) {
        List<Video> videos = Video.find("chat.id", chatId).list(); // Fetch videos based on chat ID
        if (videos.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("No videos found for chat ID: " + chatId)
                           .build();
        }

        List<SimplifiedVideo> simplifiedVideos = videos.stream()
                .map(video -> new SimplifiedVideo(
                        video.getId(),
                        video.getVideoPath(),
                        video.getVideoTitle(),
                        video.getVideoMetadata(),
                        video.getStatus()
                ))
                .toList();
        return Response.ok(simplifiedVideos).build();
    }


    private String checkAndModifyFileNameDB(String originalTitle) {
        int count = 1;
        String modifiedTitle = originalTitle;
        // Loop until the modified video filename is unique
        while (Video.existsByVideoPath(modifiedTitle)) {
            modifiedTitle = appendCounterToTitle(modifiedTitle, count);
            count++;
        }
        return modifiedTitle;
    }


    private String appendCounterToTitle(String title, int count) {
        // Assuming the title can contain an extension (like .mp4), split the title and extension
        String extension = "";
        int dotIndex = title.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = title.substring(dotIndex); // Get the file extension
            title = title.substring(0, dotIndex); // Get the title without the extension
        }
        if (count > 1)
            return title.substring(0, title.length() - 2) + "_" + count + extension;
        return title + "_" + count + extension; // Append the counter and re-add the extension
    }


    public void uploadVideoAsync(Long videoId, InputStream videoStream, String filename) {
        executor.submit(() -> {
            try {
                // Attempt upload
                System.out.println("Starting async video upload...");
                Response response = fileSystemClient.uploadStream(filename, videoStream);
                System.out.println("Video upload completed.");

                // Determine new status based on upload response
                String newStatus = (response.getStatus() == Response.Status.OK.getStatusCode()) ?
                    "UPLOADED" : "UPLOAD_FAILED";

                updateVideoStatus(videoId, newStatus);
            } catch (Exception e) {
                System.out.println("Video upload failed due to: " + e.getMessage());
                updateVideoStatus(videoId, "UPLOAD_FAILED");
            } finally {
                try {
                    videoStream.close();
                } catch (IOException e) {
                    Log.error("Error closing file stream", e);
                }
            }
        });
    }


    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateVideoStatus(Long videoId, String status) {
        try {
            Video video = em.find(Video.class, videoId);
            if (video != null) {
                video.setStatus(status);
                em.persist(video);
            }
        } catch (Exception e) {
            Log.error("Failed to update video status", e);
            throw e; // Rethrow to ensure transaction rollback
        }
    }
}