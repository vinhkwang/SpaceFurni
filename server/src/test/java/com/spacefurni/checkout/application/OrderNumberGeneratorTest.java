package com.spacefurni.checkout.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.support.AbstractIntegrationTest;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.datasource.hikari.maximum-pool-size=60")
class OrderNumberGeneratorTest extends AbstractIntegrationTest {

    @Autowired
    private OrderNumberGenerator orderNumberGenerator;

    @Test
    void generatesOrderNumberInTheExpectedFormat() {
        String orderNumber = orderNumberGenerator.generate();

        assertThat(orderNumber).matches("SF-\\d+");
    }

    @Test
    void generatesOneHundredDistinctOrderNumbersUnderConcurrency() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<AtomicReference<String>> results = IntStream.range(0, threadCount)
                .mapToObj(i -> new AtomicReference<String>()).collect(Collectors.toList());

        for (AtomicReference<String> result : results) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    result.set(orderNumberGenerator.generate());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        Set<String> distinctNumbers = results.stream().map(AtomicReference::get).collect(Collectors.toSet());
        assertThat(distinctNumbers).hasSize(threadCount);
    }
}
