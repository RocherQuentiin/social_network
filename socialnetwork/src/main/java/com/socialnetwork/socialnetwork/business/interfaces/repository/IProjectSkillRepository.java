package com.socialnetwork.socialnetwork.business.interfaces.repository;

import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.ProjectSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IProjectSkillRepository extends JpaRepository<ProjectSkill, UUID> {

    /**
     * Find all skills for a project
     */
    Optional<List<ProjectSkill>> findByProject(Project project);

    /**
     * Find all projects needing a specific skill
     */
    Optional<List<ProjectSkill>> findBySkillNameContainingIgnoreCase(String skillName);

    /**
     * Delete all skills for a project
     */
    void deleteByProject(Project project);
}
