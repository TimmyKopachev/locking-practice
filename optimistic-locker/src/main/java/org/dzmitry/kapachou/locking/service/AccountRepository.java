package org.dzmitry.kapachou.locking.service;

import jakarta.persistence.LockModeType;
import org.dzmitry.kapachou.locking.model.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    @Override
    @Lock(LockModeType.OPTIMISTIC)
    @EntityGraph(value = "graph.client", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Account> findById(String uuid);
}
