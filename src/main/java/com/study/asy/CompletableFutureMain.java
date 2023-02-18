package com.study.asy;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CompletableFutureMain {

    /**
     * 이번에는 자바8에 나온 CompletableFuture 라는 새로운 비동기 자바 프로그래밍 기술에 대해서 알아보고,
     * 지난 3회 정도 동안 다루어 왔던 자바 서블릿,
     * 스프링의 비동기 기술 발전의 내용을 자바 8을 기준으로 다시 재작성합니다.
     */
    public static void main(String[] args) throws InterruptedException {

        // Async 작업이 끝나고 해당 스레드에서 계속해서 작업을 수행한다.

        CompletableFuture.runAsync(()->log.info("runAsync"))
                .thenRun(()->log.info("thenRun"))
                .thenRun(()->log.info("thenRun2"));
        log.info("exit");

        // 별도의 pool을 설정하지 않으면 자바7 부터는 ForkJoinPool이 자동으로 사용된다.
        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
    }
}
