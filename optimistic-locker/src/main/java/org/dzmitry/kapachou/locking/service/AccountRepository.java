package org.dzmitry.kapachou.locking.service;

import jakarta.persistence.LockModeType;
import org.dzmitry.kapachou.locking.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {

    @Override
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Account> findById(String uuid);
}
