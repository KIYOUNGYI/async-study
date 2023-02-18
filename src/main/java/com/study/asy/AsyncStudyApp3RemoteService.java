package com.study.asy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@SpringBootApplication
public class AsyncStudyApp3RemoteService {

    public static void main(String[] args) {
        System.setProperty("server.port", "8085");
        System.setProperty("server.tomcat.threads.max", "1000");
        System.setProperty("spring.task.execution.pool.core-size", "100");
        SpringApplication.run(AsyncStudyApp3RemoteService.class, args);
    }


    @RestController
    public static class MyController {

        @RestController
        public static class RemoteController {

            @GetMapping("/remote3/service")
            public String service(String req) throws InterruptedException {
                log.info("/remote3/service req={}", req);
                Thread.sleep(2000L);
                return req + "/service";
            }

            @GetMapping("/remote3/service2")
            public String service2(String req) throws InterruptedException {
                log.info("/remote3/service2 req={}", req);
                Thread.sleep(2000L);
                return req + "/second_service";
            }
        }
    }

}
