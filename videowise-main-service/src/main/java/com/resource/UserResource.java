package com.resource;

import com.model.UserPreference;
import com.model.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.mindrot.jbcrypt.BCrypt;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class UserResource {

    @POST
    @Path("/register")
    @Transactional
    public Response register(User user) {
        // Hashing password
        user.password = BCrypt.hashpw(user.password, BCrypt.gensalt());
        User existingUsername = User.find("username", user.username).firstResult();
        User existingMail = User.find("email", user.email).firstResult();
        if (existingUsername != null) {
            return Response.status(400).entity("Username is already in use.").build();
        }
        if (existingMail != null) {
            return Response.status(400).entity("Email already in use.").build();
        }
        user.persist();

        UserPreference userPreference = new UserPreference();
        userPreference.setUserId(user.id);
        userPreference.setPreferenceKey("transcription-language");
        userPreference.setPreferenceValue("en");
        userPreference.setPreferenceDescription("Language used when transcribing the video with WhisperX");
        userPreference.persist();

        UserPreference userPreference2 = new UserPreference();
        userPreference2.setUserId(user.id);
        userPreference2.setPreferenceKey("assistant-message-pins");
        userPreference2.setPreferenceValue("true");
        userPreference2.setPreferenceDescription("Show/hide the assistant's message pins on the chat scrollbar");
        userPreference2.persist();

        //UserPreference userPreference3 = new UserPreference();
        //userPreference.setUserId(user.id);
        //userPreference.setPreferenceKey("chat-model");
        //userPreference.setPreferenceValue("llama3.1");
        //userPreference.setPreferenceDescription("Model used when chatting with the assistant");
        //userPreference.persist();

        return Response.status(200).entity(user).build();
    }

    @POST
    @Path("/login")
    public Response login(User loginRequest) {
        // Find user by mail
        User user = User.find("email", loginRequest.email).firstResult();

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not found").build();
        }

        boolean passwordMatch = BCrypt.checkpw(loginRequest.password, user.password);

        if (!passwordMatch) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid password").build();
        }

        return Response.ok(user).build();
    }

}