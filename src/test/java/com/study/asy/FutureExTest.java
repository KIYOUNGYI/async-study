package com.study.asy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
class FutureExTest {

    @Test
    public void case_1() throws InterruptedException {
        Thread.sleep(2000);
        System.out.println("Hello");
        System.out.println("Exit");
        //hello -> Exit
    }


    @Test
    public void case_2() {

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(()->{
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            log.info("Hello");
        });
        log.info("Exit");
    }

}