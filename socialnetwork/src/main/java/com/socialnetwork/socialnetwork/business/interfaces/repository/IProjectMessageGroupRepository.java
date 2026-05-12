package com.socialnetwork.socialnetwork.business.interfaces.repository;

import com.socialnetwork.socialnetwork.entity.ProjectMessageGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface IProjectMessageGroupRepository extends JpaRepository<ProjectMessageGroup, UUID> {

    @Modifying
    @Query("DELETE FROM ProjectMessageGroup g WHERE g.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);

    List<ProjectMessageGroup> findByProjectId(UUID projectId);
    
    @Query("SELECT pmg FROM ProjectMessageGroup pmg WHERE pmg.project.id = :projectId ORDER BY pmg.createdAt ASC")
    List<ProjectMessageGroup> findByProjectIdOrderedByCreation(@Param("projectId") UUID projectId);
}
