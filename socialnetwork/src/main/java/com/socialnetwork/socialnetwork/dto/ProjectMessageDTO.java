package com.socialnetwork.socialnetwork.dto;

import java.util.UUID;

public class ProjectMessageDTO {
    private UUID id;
    private UUID messageGroupId;
    private UUID senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private Boolean isRead;
    private long timestamp;

    // Constructors
    public ProjectMessageDTO() {}

    public ProjectMessageDTO(UUID id, UUID messageGroupId, UUID senderId, String senderName, String content) {
        this.id = id;
        this.messageGroupId = messageGroupId;
        this.senderId = senderId;
        this.senderName = senderName;
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

    public UUID getMessageGroupId() {
        return messageGroupId;
    }

    public void setMessageGroupId(UUID messageGroupId) {
        this.messageGroupId = messageGroupId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
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
