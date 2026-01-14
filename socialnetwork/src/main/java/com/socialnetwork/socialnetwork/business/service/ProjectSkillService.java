package com.socialnetwork.socialnetwork.business.service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IProjectRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IProjectSkillRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectSkillService;
import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.ProjectSkill;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectSkillService implements IProjectSkillService {

    private final IProjectSkillRepository projectSkillRepository;
    private final IProjectRepository projectRepository;

    public ProjectSkillService(IProjectSkillRepository projectSkillRepository,
                              IProjectRepository projectRepository) {
        this.projectSkillRepository = projectSkillRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional
    public ResponseEntity<List<ProjectSkill>> addSkillsToProject(UUID projectId, List<String> skills) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (skills == null || skills.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }

        List<ProjectSkill> projectSkills = new ArrayList<>();
        for (String skillName : skills) {
            if (skillName != null && !skillName.trim().isEmpty()) {
                ProjectSkill skill = new ProjectSkill();
                skill.setProject(project.get());
                skill.setSkillName(skillName.trim());
                projectSkills.add(projectSkillRepository.save(skill));
            }
        }

        return new ResponseEntity<>(projectSkills, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<ProjectSkill>> getProjectSkills(UUID projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<List<ProjectSkill>> skills = projectSkillRepository.findByProject(project.get());
        return new ResponseEntity<>(skills.orElse(new ArrayList<>()), HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<Void> removeAllSkills(UUID projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        projectSkillRepository.deleteByProject(project.get());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<Project>> findProjectsBySkill(String skillName) {
        Optional<List<ProjectSkill>> skills = projectSkillRepository.findBySkillNameContainingIgnoreCase(skillName);
        
        if (!skills.isPresent() || skills.get().isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }

        List<Project> projects = skills.get().stream()
            .map(ProjectSkill::getProject)
            .distinct()
            .toList();

        return new ResponseEntity<>(projects, HttpStatus.OK);
    }
}
