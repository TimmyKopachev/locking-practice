package org.dzmitry.kapachou.locking.service;


import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
@AllArgsConstructor
public class MoneyTransferService {

    private static final String BLACK_BG_COLOR = "\u001B[40m";
    private static final String WHITE_COLOR = "\u001B[37m";
    public static final String COLOR_RESET = "\u001B[0m";

    final AccountRepository accountRepository;
    final ReentrantLock retryerLock = new ReentrantLock();

    @Transactional(propagation = Propagation.REQUIRED,
            rollbackFor = ObjectOptimisticLockingFailureException.class)
    public void transfer(String fromUUID, String toUUID, BigDecimal money) {
        var from = accountRepository.findById(fromUUID).orElseThrow();
        var to = accountRepository.findById(toUUID).orElseThrow();

        from.withdraw(money);
        to.transfer(money);

        List.of(from, to).forEach(accountRepository::save);
    }


    public void optimisticSafeTransfer(String from, String to, BigDecimal money) {
        try {
            log.info("{}{} transaction:<{}> started at:<{}>{}",
                    WHITE_COLOR, BLACK_BG_COLOR,
                    Thread.currentThread().getId(), Instant.now().getEpochSecond(),
                    COLOR_RESET);
            transfer(from, to, money);
            log.info("{}{} transaction:<{}> started at:<{}> {}",
                    WHITE_COLOR, BLACK_BG_COLOR,
                    Thread.currentThread().getId(), Instant.now().getEpochSecond(),
                    COLOR_RESET);
        } catch (ObjectOptimisticLockingFailureException ex) {
            log.info("{}{}Account transfer cannot be executed due to version inconsistency. <{}>{}",
                    WHITE_COLOR, BLACK_BG_COLOR,
                    ex.getLocalizedMessage(),
                    COLOR_RESET);
            log.info("{}{} Catching Exception after save attempt we have following accounts:{}",
                    BLACK_BG_COLOR, WHITE_COLOR, COLOR_RESET);
            logAccounts();
            // multiple threads might take over these two accounts again then we follow the same situation
            // as a workaround we must set a thread.sleep() with a random delta to have a competitive holding the lock
            transferRetryer(from, to, money);
        }
    }

    private void transferRetryer(String from, String to, BigDecimal money) {
        retryerLock.lock();
        log.info("{}{} Retry transaction:<{}> started at:<{}>{}",
                WHITE_COLOR, BLACK_BG_COLOR,
                Thread.currentThread().getId(), Instant.now().getEpochSecond(),
                COLOR_RESET);
        transfer(from, to, money);
        log.info("{}{} Retry transaction:<{}> ended up at:<{}>{}",
                WHITE_COLOR, BLACK_BG_COLOR,
                Thread.currentThread().getId(), Instant.now().getEpochSecond(),
                COLOR_RESET);
        retryerLock.unlock();
    }

    public void logAccounts() {
        accountRepository.findAll().forEach(acc -> log.info("{}{} account:{}.{}",
                WHITE_COLOR, BLACK_BG_COLOR, acc, COLOR_RESET));
    }

    @Data
    @AllArgsConstructor
    private static class TransferRequest {
        private String from, to;
        private BigDecimal money;
    }


}
