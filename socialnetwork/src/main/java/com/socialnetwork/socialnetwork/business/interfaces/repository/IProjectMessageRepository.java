package com.socialnetwork.socialnetwork.business.interfaces.repository;

import com.socialnetwork.socialnetwork.entity.ProjectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface IProjectMessageRepository extends JpaRepository<ProjectMessage, UUID> {
    List<ProjectMessage> findByMessageGroupIdOrderByCreatedAtAsc(UUID messageGroupId);
    
    @Query("SELECT COUNT(pm) FROM ProjectMessage pm WHERE pm.messageGroup.id = :messageGroupId AND pm.isRead = false")
    long countUnreadByMessageGroupId(@Param("messageGroupId") UUID messageGroupId);
}
