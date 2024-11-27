package com.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences")
public class UserPreference extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_preferences_id_seq")
    @SequenceGenerator(name = "user_preferences_id_seq", sequenceName = "user_preferences_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "preference_key")
    private String preferenceKey;

    @Column(name = "preference_value")
    private String preferenceValue;

    @Column(name = "preference_description")
    private String preferenceDescription;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPreferenceKey() { return preferenceKey; }
    public void setPreferenceKey(String preferenceKey) { this.preferenceKey = preferenceKey; }

    public String getPreferenceValue() { return preferenceValue; }
    public void setPreferenceValue(String preferenceValue) { this.preferenceValue = preferenceValue; }

    public String getPreferenceDescription() { return preferenceDescription; }
    public void setPreferenceDescription(String preferenceDescription) { this.preferenceDescription = preferenceDescription; }
}
