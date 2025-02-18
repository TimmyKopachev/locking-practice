package org.dzmitry.kapachou.locking.scenario;

import org.dzmitry.kapachou.locking.service.MoneyTransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;


@SpringBootTest
public class PessimisticLockerScenarioTest {

    @Autowired
    private MoneyTransferService moneyTransferService;

    @Test
    // expect to catch a deadlock by having LockAcquisitionException.class
    public void verifyAccountBalanceInconsistentByTransaction() throws InterruptedException {
        var executor = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(2);

        executor.submit(() -> {
            moneyTransferService.noLockTransfer(
                    "534692af-a182-437d-9fc3-237b544bc5b1",
                    "534692af-a182-437d-9fc3-237b544bc5b2",
                    new BigDecimal(20_000));
            countDownLatch.countDown();
        });

        executor.submit(() -> {
            moneyTransferService.noLockTransfer(
                    "534692af-a182-437d-9fc3-237b544bc5b2",
                    "534692af-a182-437d-9fc3-237b544bc5b1",
                    new BigDecimal(20_000));
            countDownLatch.countDown();
        });

        countDownLatch.await();
        executor.shutdown();
    }

    @Test
    public void verifyAccountBalanceConsistentByTransactionUsingLock() throws InterruptedException {
        var executor = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(2);

        executor.submit(() -> {
            moneyTransferService.safeTransfer(
                    "534692af-a182-437d-9fc3-237b544bc5b1",
                    "534692af-a182-437d-9fc3-237b544bc5b2",
                    new BigDecimal(20_000), false);
            countDownLatch.countDown();
        });

        executor.submit(() -> {
            moneyTransferService.safeTransfer(
                    "534692af-a182-437d-9fc3-237b544bc5b2",
                    "534692af-a182-437d-9fc3-237b544bc5b1",
                    new BigDecimal(20_000), false);
            countDownLatch.countDown();
        });

        countDownLatch.await();
        executor.shutdown();
    }

    @Test
    // expect to catch a timeout
    public void verifyAccountBalanceThrowTimeoutWhenFirstLockBeyondTimeout() throws InterruptedException {
        var executor = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(2);

        executor.submit(() -> {
            moneyTransferService.safeTransfer(
                    "534692af-a182-437d-9fc3-237b544bc5b1",
                    "534692af-a182-437d-9fc3-237b544bc5b2",
                    new BigDecimal(20_000), true);
            countDownLatch.countDown();
        });

        executor.submit(() -> {
            moneyTransferService.safeTransfer(
                    "534692af-a182-437d-9fc3-237b544bc5b2",
                    "534692af-a182-437d-9fc3-237b544bc5b1",
                    new BigDecimal(20_000), true);
            countDownLatch.countDown();
        });

        countDownLatch.await();
        executor.shutdown();
    }


}
