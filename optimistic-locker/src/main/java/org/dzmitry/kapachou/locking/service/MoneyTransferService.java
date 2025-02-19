package org.dzmitry.kapachou.locking.service;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

@Service
@Slf4j
@AllArgsConstructor
public class MoneyTransferService {

    final AccountRepository accountRepository;
    final ReentrantLock retryerLock = new ReentrantLock();

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
            log.info("transaction:<{}> started at:<{}>", Thread.currentThread().getId(), Instant.now().getEpochSecond());
            transfer(from, to, money);
            log.info("transaction:<{}> started at:<{}>", Thread.currentThread().getId(), Instant.now().getEpochSecond());
        } catch (ObjectOptimisticLockingFailureException ex) {
            log.info("Account transfer cannot be executed due to version inconsistency. <{}>", ex.getMessage());
            // multiple threads might take over these two accounts again then we follow the same situation
            // as a workaround we must set a thread.sleep() with a random delta to have a competitive holding the lock
            transferRetryer(from, to, money);
        }
    }

    private void transferRetryer(String from, String to, BigDecimal money) {
        retryerLock.lock();
        log.info("Retry transaction:<{}> started at:<{}>", Thread.currentThread().getId(), Instant.now().getEpochSecond());
        transfer(from, to, money);
        log.info("Retry transaction:<{}> ended up at:<{}>", Thread.currentThread().getId(), Instant.now().getEpochSecond());
        retryerLock.unlock();
    }

    @Data
    @AllArgsConstructor
    private static class TransferRequest {
        private String from, to;
        private BigDecimal money;
    }


}
