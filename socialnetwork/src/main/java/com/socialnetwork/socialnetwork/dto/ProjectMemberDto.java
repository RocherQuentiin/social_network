package com.socialnetwork.socialnetwork.dto;

import com.socialnetwork.socialnetwork.enums.ProjectMemberRole;

import java.util.UUID;

public class ProjectMemberDto {
    private UUID userId;
    private ProjectMemberRole role;

    public ProjectMemberDto() {
    }

    public ProjectMemberDto(UUID userId, ProjectMemberRole role) {
        this.userId = userId;
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public ProjectMemberRole getRole() {
        return role;
    }

    public void setRole(ProjectMemberRole role) {
        this.role = role;
    }
}
