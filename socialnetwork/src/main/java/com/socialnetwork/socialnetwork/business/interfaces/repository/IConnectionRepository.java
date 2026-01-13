package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialnetwork.socialnetwork.entity.Connection;
import com.socialnetwork.socialnetwork.enums.ConnectionStatus;

public interface IConnectionRepository extends JpaRepository<Connection, UUID> {

    Optional<Connection> findByRequester_IdAndReceiver_Id(UUID requesterId, UUID receiverId);

    List<Connection> findByReceiver_IdAndStatus(UUID receiverId, ConnectionStatus status);

    List<Connection> findByRequester_IdAndStatus(UUID requesterId, ConnectionStatus status);

    @Query("select c from Connection c where ((c.requester.id = :a and c.receiver.id = :b) or (c.requester.id = :b and c.receiver.id = :a))")
    List<Connection> findAnyBetween(@Param("a") UUID a, @Param("b") UUID b);

    @Query(value = """
            SELECT CASE
                WHEN requester_id = :userID THEN receiver_id
                ELSE requester_id
            END
            FROM connection
            WHERE connection_status = 'Accepted'
            AND (requester_id = :userID OR receiver_id = :userID)
            """, nativeQuery = true)
    List<UUID> findAllFriendsId(@Param("userID") UUID userID);
}
