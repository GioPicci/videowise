package com.resource;

import com.client.FileSystemClient;
import com.client.WhisperClient;

import com.model.ChatInfoDTO;
import com.model.Video;
import com.model.VideoTranscription;
import com.model.Chat;

import com.pojo.HtmlConversionRequest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Path("/api/chats")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ChatResource {

    @RestClient
    FileSystemClient fileSystemClient;
    @RestClient
    WhisperClient whisperClient;

    @POST
    @Transactional
    public Chat createChat(Chat chat) {
        // Set the creation and update timestamp
        chat.created_at = LocalDateTime.now();
        chat.updated_at = LocalDateTime.now();
        chat.persist();
        return chat;
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    public Chat updateChat(@PathParam("id") Long id, Chat updatedChat) {
        Chat chat = Chat.findById(id);
        if (chat == null) {
            throw new NotFoundException("Chat not found");
        }

        // Conditionally update fields if they are provided
        if (updatedChat.getTitle() != null) {
            chat.setTitle(updatedChat.getTitle());
        }
        if (updatedChat.getChat_msg() != null) {
            chat.setChat_msg(updatedChat.getChat_msg());
        }
        // Update the updated_at timestamp
        chat.setUpdated_at(LocalDateTime.now());

        return chat;
    }

    @GET
    public List<Chat> getUserChats(@QueryParam("userId") Long userId) {
        List<Chat> chats = Chat.list("user.id", userId);
        chats.sort(Comparator.comparing(Chat::getId).reversed());
        return chats;
    }

    @GET
    @Path("/info")
    public List<ChatInfoDTO> getUserChatsInfo(@QueryParam("userId") Long userId) {
        return Chat.find("user.id", userId)
               .project(ChatInfoDTO.class)  // Projects to the DTO
               .list();
    }

    @GET
    @Path("/{id}")
    public Chat getChat(@PathParam("id") Long chatId) {
        Chat chat = Chat.findById(chatId);
        if (chat == null) {
            throw new NotFoundException("Chat not found");
        }
        return chat;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void deleteChat(@PathParam("id") Long id) {
        List<Video> videos = Video.list("chat.id", id);

        List<VideoTranscription> allTranscriptions = new ArrayList<>();
        for (Video video : videos) {
            List<VideoTranscription> transcriptions = VideoTranscription.list("video.id", video.getId());
            allTranscriptions.addAll(transcriptions);
        }

        for (VideoTranscription transcription : allTranscriptions) {
            //System.out.println("Cancello transcrizione con id: " + transcription.id);
            VideoTranscription.deleteById(transcription.id);
        }
        for (Video video : videos) {
            //System.out.println("Cancello video con id: " + video.id +" e filename: " + video.getVideoPath());
            fileSystemClient.delete(video.videoPath);
            if(Objects.equals(video.status, "EXTRACTED"))
                fileSystemClient.delete(replaceFileExtension(video.videoPath, ".mp3"));
            Video.deleteById(video.id);
        }
        //System.out.println("Cancello chat con id: "+id);
        Chat.deleteById(id);
    }

    @POST
    @Path("/generate/pdf")
    @Transactional
    public byte[] generatePDF(HtmlConversionRequest htmlConversionRequest) {
        Response response = whisperClient.convertHTMLtoPDF(htmlConversionRequest);
        if (response.getStatus() == 200) {
            return response.readEntity(byte[].class);
        } else {
            throw new RuntimeException("Failed to convert HTML to PDF. Status: " + response.getStatus());
        }
    }

    @POST
    @Path("/generate/word")
    @Transactional
    public byte[] generateWORD(HtmlConversionRequest htmlConversionRequest) {
        Response response = whisperClient.convertHTMLtoWORD(htmlConversionRequest);
        if (response.getStatus() == 200) {
            return response.readEntity(byte[].class);
        } else {
            throw new RuntimeException("Failed to convert HTML to Word. Status: " + response.getStatus());
        }
    }

    public String replaceFileExtension(String fileName, String newExtension) {
    int lastDotIndex = fileName.lastIndexOf(".");
    if (lastDotIndex == -1) {
        // If there's no extension, just add the new extension
        return fileName + newExtension;
    }
    // Replace the existing extension with the new one
    return fileName.substring(0, lastDotIndex) + newExtension;
}
}
