package com.socialnetwork.socialnetwork.business.interfaces.service;

import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.ProjectSkill;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface IProjectSkillService {
    
    /**
     * Add skills to a project
     */
    ResponseEntity<List<ProjectSkill>> addSkillsToProject(UUID projectId, List<String> skills);
    
    /**
     * Get all skills for a project
     */
    ResponseEntity<List<ProjectSkill>> getProjectSkills(UUID projectId);
    
    /**
     * Remove all skills from a project
     */
    ResponseEntity<Void> removeAllSkills(UUID projectId);
    
    /**
     * Find projects needing a specific skill
     */
    ResponseEntity<List<Project>> findProjectsBySkill(String skillName);
}
