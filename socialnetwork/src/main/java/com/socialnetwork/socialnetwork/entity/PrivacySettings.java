package com.socialnetwork.socialnetwork.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "privacy_settings")
public class PrivacySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "can_see_email")
    private Boolean canSeeEmail = false;

    @Column(name = "can_see_phone")
    private Boolean canSeePhone = false;

    @Column(name = "can_see_birthdate")
    private Boolean canSeeBirthdate = false;

    @Column(name = "can_see_location")
    private Boolean canSeeLocation = false;

    @Column(name = "allow_direct_messages")
    private Boolean allowDirectMessages = true;

    @Column(name = "allow_friend_requests")
    private Boolean allowFriendRequests = true;

    @Column(name = "allow_comments_on_posts")
    private Boolean allowCommentsOnPosts = true;

    @Type(JsonBinaryType.class)
    @Column(name = "block_list", columnDefinition = "jsonb")
    private List<UUID> blockList;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getCanSeeEmail() {
        return canSeeEmail;
    }

    public void setCanSeeEmail(Boolean canSeeEmail) {
        this.canSeeEmail = canSeeEmail;
    }

    public Boolean getCanSeePhone() {
        return canSeePhone;
    }

    public void setCanSeePhone(Boolean canSeePhone) {
        this.canSeePhone = canSeePhone;
    }

    public Boolean getCanSeeBirthdate() {
        return canSeeBirthdate;
    }

    public void setCanSeeBirthdate(Boolean canSeeBirthdate) {
        this.canSeeBirthdate = canSeeBirthdate;
    }

    public Boolean getCanSeeLocation() {
        return canSeeLocation;
    }

    public void setCanSeeLocation(Boolean canSeeLocation) {
        this.canSeeLocation = canSeeLocation;
    }

    public Boolean getAllowDirectMessages() {
        return allowDirectMessages;
    }

    public void setAllowDirectMessages(Boolean allowDirectMessages) {
        this.allowDirectMessages = allowDirectMessages;
    }

    public Boolean getAllowFriendRequests() {
        return allowFriendRequests;
    }

    public void setAllowFriendRequests(Boolean allowFriendRequests) {
        this.allowFriendRequests = allowFriendRequests;
    }

    public Boolean getAllowCommentsOnPosts() {
        return allowCommentsOnPosts;
    }

    public void setAllowCommentsOnPosts(Boolean allowCommentsOnPosts) {
        this.allowCommentsOnPosts = allowCommentsOnPosts;
    }

    public List<UUID> getBlockList() {
        return blockList;
    }

    public void setBlockList(List<UUID> blockList) {
        this.blockList = blockList;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
