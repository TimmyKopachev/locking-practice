package org.dzmitry.kapachou.locking.scenario;

import org.apache.commons.lang3.tuple.Pair;
import org.dzmitry.kapachou.locking.service.MoneyTransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;


@SpringBootTest
public class OptimisticLockerScenarioTest {

    @Autowired
    private MoneyTransferService moneyTransferService;

    @Test
    // deadlock is resolved by Optimistic Locking
    // we catch the case and retry the process checking if lock for the same row has been released
    public void verifyAccountBalanceConsistentByTransactionUsingLock() throws InterruptedException {
        var executor = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(2);

        executor.submit(() -> {
            moneyTransferService.optimisticSafeTransfer(
                    "534692af-a182-437d-9fc3-237b544bc5b1",
                    "534692af-a182-437d-9fc3-237b544bc5b2",
                    new BigDecimal(5_000));
            countDownLatch.countDown();
        });

        executor.submit(() -> {
            moneyTransferService.optimisticSafeTransfer(
                    "534692af-a182-437d-9fc3-237b544bc5b2",
                    "534692af-a182-437d-9fc3-237b544bc5b1",
                    new BigDecimal(15_000));
            countDownLatch.countDown();
        });
        countDownLatch.await();
        executor.shutdown();
    }


}
