package com.socialnetwork.socialnetwork.dto;

import com.socialnetwork.socialnetwork.enums.VisibilityType;

public class ProjectDto {
    private String name;
    private String description;
    private VisibilityType visibility;

    public ProjectDto() {
    }

    public ProjectDto(String name, String description, VisibilityType visibility) {
        this.name = name;
        this.description = description;
        this.visibility = visibility;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public VisibilityType getVisibility() {
        return visibility;
    }

    public void setVisibility(VisibilityType visibility) {
        this.visibility = visibility;
    }
}
