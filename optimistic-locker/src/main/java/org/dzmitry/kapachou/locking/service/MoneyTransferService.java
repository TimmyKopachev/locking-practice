package org.dzmitry.kapachou.locking.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class MoneyTransferService {

    final AccountRepository accountRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void transfer(String fromUUID, String toUUID, BigDecimal money) {
        var from = accountRepository.findById(fromUUID).orElseThrow();
        var to = accountRepository.findById(toUUID).orElseThrow();

        from.withdraw(money);
        to.transfer(money);

        List.of(from, to).forEach(accountRepository::save);
    }


    public void optimisticSafeTransfer(String from, String to, BigDecimal money) {
        try {
            transfer(from, to, money);
        } catch (ObjectOptimisticLockingFailureException ex) {
            log.info("Account transfer cannot be executed due to version inconsistency. <{}>", ex.getMessage());
            // multiple threads might take over these two accounts again then we follow the same situation
            // as a workaround we must set a thread.sleep() with a random delta to have a competitive holding the lock
            optimisticSafeTransfer(from, to, money);
        }
    }

}
