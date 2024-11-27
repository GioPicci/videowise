package com.resource;

import com.model.User;
import com.model.UserPreference;
import com.pojo.UserPreferencesRequest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;


@Path("/api/preferences")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class UserPreferenceResource {

    @POST
    public Response addUserPreferences(UserPreferencesRequest userPreferencesRequest) {
        Long userId = userPreferencesRequest.getUserId();
        User user = User.findById(userId);
        if(user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("User not found")
                .build();
        }
        List<UserPreferencesRequest.Preference> userPreferences = userPreferencesRequest.getPreferences();
        for (UserPreferencesRequest.Preference preference : userPreferences) {
            UserPreference userPreference = new UserPreference();
            userPreference.setUserId(userId);
            userPreference.setPreferenceKey(preference.getKey());
            userPreference.setPreferenceValue(preference.getValue());
            userPreference.setPreferenceDescription(preference.getDescription());
            userPreference.persist();
        }
        return Response.status(Response.Status.OK)
            .entity("Properties Added")
            .build();
    }

    @GET
    @Path("/{user_id}/{preference_key}")
    public Response getUserPreference(@PathParam("user_id") Long userId, @PathParam("preference_key") String preferenceKey) {
        UserPreference userPreference = UserPreference.find("userId = ?1 and preferenceKey = ?2", userId, preferenceKey).firstResult();
        if (userPreference == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("User preference not found").build();
        }
        // Find the UserPreference based on userId and preferenceKey
        return Response.status(Response.Status.OK).entity(userPreference).build();
    }

    @PATCH
    @Transactional
    public Response updateUserPreferences(UserPreferencesRequest userPreferencesRequest) {
        Long userId = userPreferencesRequest.getUserId();
        User user = User.findById(userId);
        if(user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("User not found")
                .build();
        }
        List<UserPreferencesRequest.Preference> userPreferences = userPreferencesRequest.getPreferences();
        for (UserPreferencesRequest.Preference updatedPreference : userPreferences) {
            UserPreference userPreference = UserPreference.find("userId = ?1 and preferenceKey = ?2",
                    userId, updatedPreference.getKey()).firstResult();
            if (updatedPreference.getValue() != null) {
                userPreference.setPreferenceValue(updatedPreference.getValue());
            }
            if (updatedPreference.getDescription() != null) {
                userPreference.setPreferenceDescription(updatedPreference.getDescription());
            }
            userPreference.persist();
        }
        return Response.status(Response.Status.OK)
            .entity("Properties Updated")
            .build();
    }
}
