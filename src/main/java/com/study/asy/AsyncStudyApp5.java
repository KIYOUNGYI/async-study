package com.study.asy;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

@Slf4j
@SpringBootApplication
public class AsyncStudyApp5 {

    public static void main(String[] args) {
//        System.setProperty("server.port", "8084");
//        System.setProperty("server.tomcat.threads.max", "1");
//        System.setProperty("spring.task.execution.pool.core-size", "100");
//        SpringApplication.run(AsyncStudyApp5.class, args);
    }

}
