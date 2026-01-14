package com.socialnetwork.socialnetwork.dto;

public class ProjectRequestDto {
    private String skillName;
    private String message;

    public ProjectRequestDto() {
    }

    public ProjectRequestDto(String skillName, String message) {
        this.skillName = skillName;
        this.message = message;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
