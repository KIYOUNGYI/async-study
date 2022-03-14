package com.study.asy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@SpringBootApplication
public class RemoteService {

    public static void main(String[] args) {
        System.setProperty("server.port", "8082");
        System.setProperty("server.tomcat.threads.max", "1000");
        System.setProperty("spring.task.execution.pool.core-size", "100");
        SpringApplication.run(RemoteService.class, args);
    }


    @RestController
    public static class MyController {

        @GetMapping("/service")
        public String service(String req) throws InterruptedException {
            Thread.sleep(2000);
            return req + "/service";
//            throw new RuntimeException();
        }

        @GetMapping("/service2")
        public String service2(String req) throws InterruptedException {
            System.out.println("MyController.service2");
            System.out.println("req = " + req);
            System.out.println("=====================");
            Thread.sleep(2000);
            return req + "/service2_response";
//            throw new RuntimeException();
        }

    }

}
