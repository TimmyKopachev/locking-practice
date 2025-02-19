package org.dzmitry.kapachou.locking.service;


import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dzmitry.kapachou.locking.model.Account;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class MoneyTransferService {

    final AccountRepository accountRepository;

    @Transactional(timeout = 1_000)
    public void safeTransfer(String fromUUID, String toUUID, BigDecimal money, boolean enableLongTimeProcessing) {
        Account from;
        Account to;
        if (fromUUID.compareTo(toUUID) > 0) {
            from = accountRepository.findById(fromUUID).orElseThrow();
            to = accountRepository.findById(toUUID).orElseThrow();
        } else {
            to = accountRepository.findById(toUUID).orElseThrow();
            from = accountRepository.findById(fromUUID).orElseThrow();
        }

        if (enableLongTimeProcessing) {
            // simulate a long time processing to trigger a timeout exception
            // for the second waiting transaction
            threadSleep(5000);
        }

        synchronized (from) {
            synchronized (to) {
                from.withdraw(money);
                to.transfer(money);
            }
        }
        accountRepository.saveAll(List.of(from, to))
                .forEach(account ->
                            log.info("Client:<{}> account:<{}> with new balance:<{}> has been updated successfully.",
                                account.getClient().getName(),
                                account.getUuid(),
                                account.getBalance()));
    }

    @Transactional
    public void noLockTransfer(String fromUUID, String toUUID, BigDecimal money) {
        var from = accountRepository.findById(fromUUID).orElseThrow();
        // sleeping thread to allow another thread getting a lock for the second account
        threadSleep(1000);

        var to = accountRepository.findById(toUUID).orElseThrow();

        from.withdraw(money);
        to.transfer(money);

        accountRepository.saveAll(List.of(from, to));
    }

    public static void threadSleep(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
