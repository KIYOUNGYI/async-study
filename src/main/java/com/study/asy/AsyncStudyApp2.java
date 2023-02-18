package com.study.asy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

@EnableAsync
@Slf4j
@SpringBootApplication
public class AsyncStudyApp2 {

    @Bean
    WebMvcConfigurer configure() {
        return new WebMvcConfigurer() {
            // 워커 스레드 풀 설정
            @Override
            public void configureAsyncSupport(AsyncSupportConfigurer configure) {
                ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
                te.setCorePoolSize(100);
                te.setQueueCapacity(50);
                te.setMaxPoolSize(200);
                te.setThreadNamePrefix("workThread");
                te.initialize();
                configure.setTaskExecutor(te);
            }
        };
    }

    public static void main(String[] args) {
        System.setProperty("server.port", "8083");
        System.setProperty("server.tomcat.threads.max", "1");
        System.setProperty("spring.task.execution.pool.core-size", "100");
        SpringApplication.run(AsyncStudyApp2.class, args);
    }

    @Service
    public static class MyService {
        /*
         내부적으로 AOP를 이용해 복잡한 로직이 실행된다.
         비동기 작업은 return값으로 바로 결과를 줄 수 없다. (Future 혹은 Callback을 이용해야 한다.)
         */

    }

    @RestController
    @EnableAsync//-> ListenableFuture 비동기 실행하려면 필요한 어노테이션인가?
    public static class MyController {

        Queue<DeferredResult<String>> results = new ConcurrentLinkedQueue<>();

        @GetMapping("/test_dummy1")
        public String hello() throws InterruptedException {
            log.info("MyController.hello");
            Thread.sleep(1000L);
            return "hello";
        }

        @Async
        @GetMapping("/test_dummy2")
        public Future<String> test2() throws InterruptedException {
            log.info("hello()");
            Thread.sleep(1000);
            return new AsyncResult<String>("Hello");
        }

//        @Async
//        @GetMapping("/test_dummy3")
//        public ListenableFuture<String> test3() throws InterruptedException {
//            log.info("hello()");
//            Thread.sleep(1000);
//            return new AsyncResult<String>("Hello");
//        }


        /**
         * nio-8083-exec-1, nio-8083-exec-1
         * MvcAsync200, MvcAsync195, MvcAsync198
         * <p>
         * An Executor is required to handle java.util.concurrent.Callable return values.
         * Please, configure a TaskExecutor in the MVC config under “async support”.
         * The SimpleAsyncTaskExecutor currently in use is not suitable under load.
         * =================================
         * configure 빈으로 등록후 / configureAsyncSupport 오버라이드
         * <p>
         * [nio-8083-exec-1] (매 요청)
         * workThread [1~100] (실행문 안에 로그 찍는거 수행하는 명령어)
         * hello 리턴받음
         *
         * @return
         * @throws InterruptedException
         */
        @GetMapping("/test_dummy4")
        public Callable<String> test4() throws InterruptedException {
            log.info("callable");
            int a = 2 + 3;
            log.info("a: {}", a);
            return () -> {
                log.info("async");
                int y = 2 + 3;
                log.info("y : {}", y);
                Thread.sleep(2000);
                return "hello";
            };
        }

        /**
         * 내가 직접 등록한 myThreadPool 빈을 활용함
         * myThreadPool-1 (callable 로그 찍는것)
         * <p>
         * async 로그 안찍힘
         * hello 리턴 안받음
         */
        @Async
        @GetMapping("/test_dummy4_1")
        public Callable<String> test4_1() throws InterruptedException {
            log.info("callable");
            return () -> {
                log.info("async");
//                System.out.println("async");
                Thread.sleep(2000);
                return "hello";
            };
        }


        /**
         * nio-8083-exec-1
         */
        @GetMapping("/test_dummy5")
        public String callable() throws InterruptedException {
            log.info("sync");
            Thread.sleep(2000);
            return "hello";
        }


        /**
         * workThread1~100
         */
        @GetMapping("/test_dummy6")
        public Callable<String> callable6() {
            return () -> {
                log.info("async");
                Thread.sleep(2000);
                return "hello";
            };
        }

        @GetMapping("/test/dr")
        public DeferredResult<String> dr() {
            log.info("dr");
            DeferredResult<String> dr = new DeferredResult<>();
            results.add(dr);
            return dr;
        }

        @GetMapping("/test/dr/count")
        public String drCount() {
            return String.valueOf(results.size());
        }

        @GetMapping("/test/dr/event")
        public String drEvent(String msg) {
            for (DeferredResult<String> dr : results) {
                dr.setResult("Hello " + msg);
                results.remove(dr);
            }
            return "OK";
        }


    }

}
