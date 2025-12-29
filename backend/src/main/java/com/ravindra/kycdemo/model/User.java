package com.ravindra.kycdemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    @JsonProperty("isPanVerified")
    private Boolean isPanVerified = false;
    private String videoPath;
    private String fullName;
    private String role;
    private String panNumber;

    private Integer failedAttempts = 0;
    private java.time.LocalDateTime lockoutTime;

    @JsonProperty("isPanVerified")
    public Boolean getIsPanVerified() {
        return isPanVerified;
    }

    public Boolean getPanVerified() {
        return isPanVerified;
    }

    @JsonProperty("videoUrl")
    public String getVideoUrl() {
        if (videoPath == null || videoPath.isEmpty())
            return null;
        java.io.File f = new java.io.File(videoPath);
        return "/uploads/" + f.getName();
    }

}
