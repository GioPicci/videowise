package com.resource;

import com.client.FileSystemClient;
import com.client.WhisperClient;
import com.form.AudioTranscriptionForm;
import com.form.TranscriptionForm;
import com.model.Chat;
import com.model.Video;
import com.pojo.TranscriptionResponse;
import com.service.VideoStatusPollingService;
import com.model.VideoTranscription;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.context.api.NamedInstance;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.transaction.Transactional;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/api/transcriptions")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TranscriptionResource {

    @Inject
    @NamedInstance("myExecutor")
    ManagedExecutor executor;

    @Inject
    EntityManager em;

    @RestClient
    FileSystemClient fileSystemClient;

    @RestClient
    WhisperClient whisperClient;

    @Inject
    VideoStatusPollingService videoStatusPollingService;

    @Inject
    VideoResource videoResource;

    private static final Logger logger = Logger.getLogger(TranscriptionResource.class.getName());
    private static final String model_name = "large-v2";


    @POST
    @Path("/transcribe")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response transcribeVideo(@MultipartForm TranscriptionForm form) {
        // Implementare la logica di salvataggio del video e chiamata a WhisperX
        //return whisperService.transcribeVideo(chatId, videoStream);
        long chatId = form.getChatIdAsLong();
        long videoId = form.getVideoIdAsLong();
        String language = form.getLanguage();

        // Prendi il video relativo all'ID videoId
        Video video = Video.findById(videoId);
        if (video == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Video not found")
                           .build();
        }

        // Prendi la chat relativa all'ID chatId
        Chat chat = Chat.findById(chatId);
        if (chat == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Chat not found")
                           .build();
        }

        // Prendi filename del video e status
        String videoPath = video.videoPath;
        String videoStatus = video.getStatus();

        // Verifica che il video sia stato caricato sul FS remoto
        if (videoStatus.equalsIgnoreCase("missing")) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Video missing")
                           .build();
        }

        // Verifica che il video non sia ancora in fase di upload sul FS remoto
        if (videoStatus.equalsIgnoreCase("uploading")) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Video is still uploading")
                           .build();
        }

        // Crea una nuova entità VideoTranscription e salvala a DB
        VideoTranscription transcription = new VideoTranscription();
        //transcription.chat = chat;
        transcription.video = video;
        transcription.language = language;
        transcription.status = "PROCESSING"; // Imposta lo stato su PROCESSING
        transcription.created_at = LocalDateTime.now();
        transcription.updated_at = LocalDateTime.now();
        transcription.persist(); // Salva a DB

        // Costruisci form per la richiesta di trascrizione
        AudioTranscriptionForm audioTranscriptionForm = new AudioTranscriptionForm(videoPath, Long.toString(videoId));

        System.out.println("Nome del video da trascrivere: " + videoPath);

        // Lancia processo asincrono che richiede l'estrazione dell'audio dal video
        extractAudioAsync(videoId, transcription.id, audioTranscriptionForm);

        // Avvia servizio di Polling a DB. Quando lo stato del video viene impostato a "EXTRACTED" parte con la
        // trascrizione dell'audio estratto. Se l'estrazione impiega più di 60s fallisce
        videoStatusPollingService.pollForVideoExtraction(videoId)
                .thenAccept(extracted -> {
                    if(extracted) {
                        String audioPath= videoPath.replace(FilenameUtils.getExtension(videoPath), "mp3");
                        System.out.println("Audio Extraction Completed Successfully. Filename: " + audioPath);
                        System.out.println("Starting Transcription");
                        processTranscriptionAsync(transcription.id, audioPath, language);
                    } else {
                        System.out.println("Audio Extraction Failed");
                        handleTranscriptionError(transcription.id);
                    }
                });

        // Restituisci l'ID della trascrizione prima che sia stata completata. Verrà usato come token dal client
        return Response.ok(transcription.id).build();
    }


    @GET
    @Path("/{id}")
    public Response getTranscription(@PathParam("id") Long id) {
        VideoTranscription transcription = VideoTranscription.findById(id);
        if (transcription == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Transcription not found")
                    .build();
        }
        return Response.status(Response.Status.OK)
                    .entity(transcription)
                    .build();
    }


    @GET
    @Path("/status/{id}")
    public Response getTranscriptionStatus(@PathParam("id") Long id) {
        VideoTranscription transcription = VideoTranscription.findById(id);

        if (transcription == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Transcription not found")
                    .build();
        }

        return Response.ok(transcription.status).build();
    }


    @GET
    @Path("/video/{video_id}")
    @Transactional
    public Response getTranscriptionsByVideoId(@PathParam("video_id") Long videoId) {
        List<VideoTranscription> transcriptions = VideoTranscription.find("video.id", videoId).list(); // Fetch videos based on chat ID
        if (transcriptions.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("No transcriptions found for video ID: " + videoId)
                           .build();
        }

        return Response.ok(transcriptions).build();
    }


    @GET
    @Path("/vtt/video/{video_id}")
    public Response getVTTByVideoId(@PathParam("video_id") Long videoId) {
        System.out.println("VTT VIDEO ID requested: " + videoId);

        // Step 1: Find the video by its ID
        Video video = Video.findById(videoId);
        if (video == null) {
            System.out.println("No video found for ID: " + videoId);
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("No video found for ID: " + videoId)
                           .build();
        }

        // Step 2: Find transcriptions for the video
        List<VideoTranscription> transcriptions = VideoTranscription.find("video.id", videoId).list();
        if (transcriptions.isEmpty()) {
            System.out.println("No transcriptions found for video ID: " + videoId);
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("No transcriptions found for video ID: " + videoId)
                           .build();
        }

        // Step 3: Construct the path for the VTT file
        String videoPath = video.videoPath;
        String vttFilePath = videoPath.replace(FilenameUtils.getExtension(videoPath), "vtt");

        // Debug: Print the VTT file path
        System.out.println("Requesting VTT file at path: " + vttFilePath);

        // Step 4: Call the filesystem service to download the VTT file
        Response vttResponse = null;
        try {
            vttResponse = fileSystemClient.download(vttFilePath);
        } catch (Exception e) {
            System.out.println("Error calling file system client: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error while communicating with the filesystem service.")
                           .build();
        }

        // Step 5: Check the status of the response from the filesystem service
        System.out.println("Filesystem service response status: " + vttResponse.getStatus());
        if (vttResponse.getStatus() == Response.Status.OK.getStatusCode()) {
            // Step 6: Extract and return the VTT content
            String vttContent = vttResponse.readEntity(String.class);
            System.out.println("Successfully retrieved VTT content.");

            return Response.ok(vttContent, "text/vtt")
                           .header("Content-Disposition", "attachment; filename=\"" + videoId + ".vtt\"")
                           .build();
        } else {
            // Step 7: Handle unsuccessful download attempt
            System.out.println("Failed to download VTT file. Response status: " + vttResponse.getStatus());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Failed to download VTT file for video ID: " + videoId)
                           .build();
        }
    }


    @Transactional
    public void updateTranscriptionContent(Long transcriptionId, String content) {
        VideoTranscription transcription = em.find(VideoTranscription.class, transcriptionId);
        if (transcription != null) {
            transcription.transcription = content;
            transcription.status = "COMPLETED"; // set transcription status to COMPLETED
            transcription.updated_at = LocalDateTime.now();
            em.merge(transcription); // Save changes on the db
        }
    }

    @Transactional
    protected void handleTranscriptionError(Long transcriptionId) {
        VideoTranscription transcription = em.find(VideoTranscription.class, transcriptionId);
        if (transcription != null) {
            transcription.status = "ERROR";
            transcription.updated_at = LocalDateTime.now();
            em.merge(transcription);
        }
    }

    public void processTranscriptionAsync(Long transcriptionId, String audioPath, String language) {
        executor.submit(() -> {
            try {
                String transcriptionContent = transcribeAudio(audioPath, language);
                //System.out.println("Transcription content: " + transcriptionContent);

                // Update the transcription in a new transaction
                updateTranscriptionContent(transcriptionId, transcriptionContent);
            } catch (Exception e) {
                // Error handling
                handleTranscriptionError(transcriptionId);
            }
        });
    }

    public void extractAudioAsync(Long videoId, Long videoTranscriptionId, AudioTranscriptionForm audioTranscriptionForm) {
        executor.submit(() -> {
            try {
                videoResource.updateVideoStatus(videoId, "EXTRACTING");
                fileSystemClient.extractAudio(audioTranscriptionForm);
                videoResource.updateVideoStatus(videoId, "EXTRACTED");
            } catch (Exception e) {
                // Error handling
                videoResource.updateVideoStatus(videoId, "EXTRACTION_FAILED");
                handleTranscriptionError(videoTranscriptionId);
            }
        });
    }

    public String transcribeAudio(String audioPath, String language) {
        String audioFileName = Paths.get(audioPath).getFileName().toString();

        JsonObject transcriptionJson = Json.createObjectBuilder()
            .add("audio_file", audioFileName)
            .add("language", language)
            .add("modelName", model_name)
            .build();

        String transcriptionString = transcriptionJson.toString();
        //logger.info("Sending Transcription String to Whisper server: " + transcriptionString);
        TranscriptionResponse response;
        // Step 1: Send initial transcription request
        try {
            response = whisperClient.transcribe(transcriptionString);
        } catch (Exception e) {
            logger.severe("WhisperClient error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if (response.getStatus().equalsIgnoreCase("fail")) {
            // WhisperX API failed to process the request
            logger.log(Level.SEVERE, "WhisperX Transcription failed for audio file: " + audioFileName);
            throw new RuntimeException("WhisperX Transcription API failed for audio file: " + audioFileName);
        }

        // Step 2: Poll for completion
        String transcriptionResult = pollForTranscriptionResult(response.getTranscriptionToken());

        logger.info("WhisperX Transcription API succeeded for audio file: " + audioFileName);

        return transcriptionResult;
    }

    // Helper method to poll for the transcription result
    private String pollForTranscriptionResult(String transcriptionId) {
        int maxAttempts = 60;
        int attempt = 0;
        int pollIntervalMillis = 3000; // 2 seconds
        while (attempt < maxAttempts) {
            //logger.info("Polling for transcription result: " + transcriptionId);
            attempt++;
            try {
                // Wait before the next poll attempt
                Thread.sleep(pollIntervalMillis);

                // Step 3: Check the transcription status
                TranscriptionResponse statusResponse = whisperClient.checkStatus(transcriptionId);
                if (statusResponse.getStatus().equalsIgnoreCase("success")) {
                    logger.info("Polling status success for: " + transcriptionId);
                    TranscriptionResponse outputResponse = whisperClient.retrieveOutput(transcriptionId);
                    return getOutputAsString(outputResponse);
                } else if (statusResponse.getStatus().equalsIgnoreCase("fail")) {
                    logger.info("Polling status fail for: " + transcriptionId);
                    throw new RuntimeException("Transcription failed on the server side.");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Polling interrupted", e);
            }
        }
        throw new RuntimeException("Polling for transcription result timed out.");
    }

    private String getOutputAsString(TranscriptionResponse response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Convert the 'output' object to a JSON string
            return objectMapper.writeValueAsString(response.getOutput());
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "WhisperX Transcription conversion to string failed");
            return null;  // Handle the exception as appropriate
        }
    }
}