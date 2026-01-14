package com.socialnetwork.socialnetwork.dto;

import java.util.UUID;

public class NotificationDTO {
    private UUID id;
    private UUID recipientId;
    private UUID actorId;
    private String actorName;
    private String actorAvatar;
    private String type;
    private String content;
    private Boolean isRead;
    private long timestamp;

    // Constructors
    public NotificationDTO() {}

    public NotificationDTO(UUID id, UUID recipientId, UUID actorId, String actorName, String type, String content) {
        this.id = id;
        this.recipientId = recipientId;
        this.actorId = actorId;
        this.actorName = actorName;
        this.type = type;
        this.content = content;
        this.isRead = false;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(UUID recipientId) {
        this.recipientId = recipientId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorAvatar() {
        return actorAvatar;
    }

    public void setActorAvatar(String actorAvatar) {
        this.actorAvatar = actorAvatar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
