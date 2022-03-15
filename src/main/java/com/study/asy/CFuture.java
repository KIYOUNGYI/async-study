package com.study.asy;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class CFuture {

    /**
     * Future, Promise, Deferred -> 섞여서 혼용되어 사용되는듯
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        CompletableFuture
                .supplyAsync(() -> {
                    log.info("supplyAsync");
//                    if (1 == 1) throw new RuntimeException();
                    return 1;
                }, executorService)
                .thenCompose(s -> {
                    log.info("thenRun");
                    return CompletableFuture.completedFuture(s + 1);
                }).
                thenApplyAsync(s2 -> {
                    log.info("thenRun2 {}", s2);
                    return s2 * 3;
                },executorService).
                exceptionally(e -> -10)
                .thenAcceptAsync(s3 -> log.info("then Accept {}", s3),executorService);


        log.info("exit");

        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
        executorService.awaitTermination(10,TimeUnit.SECONDS);
        executorService.shutdown();
    }
}
