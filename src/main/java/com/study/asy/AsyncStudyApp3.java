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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

@Slf4j
@SpringBootApplication
public class AsyncStudyApp3 {

    public static void main(String[] args) {
        System.setProperty("server.port", "8084");
        System.setProperty("server.tomcat.threads.max", "1");
        System.setProperty("spring.task.execution.pool.core-size", "100");
        SpringApplication.run(AsyncStudyApp3.class, args);
    }

    @RestController
    @Slf4j
    public static class MainController {


        RestTemplate rt = new RestTemplate();


        /**
         * 스프링 4 부터 제공하는 AsyncRestTemplate을 사용하면 이 문제를 쉽게 해결할 수 있습니다.
         * AsyncRestTemplate은 비동기 클라이언트를 제공하는 클래스이며 ListenableFuture를 반환합니다.
         * 스프링은 컨트롤러에서 ListenableFuture를 리턴하면 해당 스레드는 즉시 반납하고,
         * 스프링 MVC가 자동으로 등록해준 콜백에 의해 결과가 처리됩니다.
         */
        AsyncRestTemplate art = new AsyncRestTemplate();

        // asynchronous + netty non-blocking
//        AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));



        /**
         * RemoteController.service 에 Thread.sleep(2000L) 했을 경우
         * <p>
         * 이런 결과가 나오게 된 이유는 클라이언트로부터의 요청을 받아 처리하는 Main Application의 tomcat thread가 1개이고,
         * <p>
         * 1개의 서블릿 스레드를 이용해 클라이언트의 요청을 처리하는 과정에서
         * <p>
         * Remote Application에 대한 요청(Network I/O)에서
         * 응답을 받기까지 약 2초간 스레드가 block되기 때문입니다.
         * <p>
         * <p>
         * 위의 문제는 MainApplication의 tomcat 스레드는 클라이언트의 요청을 처리하며
         * 외부 서비스(RemoteApplication)로 요청(Network I/O)을 보낸 후,
         * 응답이 올 때까지 대기하고 있는 상태라는 점입니다.
         * <p>
         * 해당 시간동안 CPU는 아무 일을 처리하지 않기때문에 자원이 소모되고 있습니다.
         * <p>
         * 이 문제를 해결하기 위해서는 API를 호출하는 작업을 비동기적으로 바꿔야합니다.
         * <p>
         * tomcat 스레드는 요청에 대한 작업을 다 끝내기 전에 반환을 해서 바로 다음 요청을 처리하도록 사용합니다.
         * 그리고 외부 서비스로부터 실제 결과를 받고 클라이언트의 요청에 응답을 보내기 위해서는 새로운 스레드를 할당 받아 사용합니다.
         * <p>
         * (외부 서비스로부터 실제 결과를 받고 클라이언트에 응답을 보내기 위해서는 새로운 스레드를 할당 받아야 하지만, 외부 API를 호출하는 동안은 스레드(tomcat) 자원을 낭비하고 싶지 않다는 것이 목적이다.)
         * <p>
         * 핵심은 응답을 기다리는 동안 쓰레드는 놀고 있다는 뜻 // 지금 테스트 1개인거 200개로 늘리는게 핵심이 아니라
         */
        @GetMapping("/app3/sync_rest")
        public String syncRest(int idx) {
            String res = rt.getForObject("http://localhost:8085/remote3/service?req={req}",
                    String.class, "hello" + idx);
            log.info("res : {}", res);
            return res;
//            return null;
        }

        @GetMapping("/app3/async_rest")
        public ListenableFuture<ResponseEntity<String>> asyncRest(int idx) {
//            String res = rt.getForObject("http://localhost:8085/remote3/service?req={req}",
//                    String.class, "hello" + idx);
//            log.info("res : {}", res);
//            return res;
            log.info("app3/async_rest idx:{}", idx);
            return art.getForEntity("http://localhost:8085/remote3/service?req={req}",
                    String.class, "hello" + idx);
        }


        /**
         * 지금까지 Tomcat의 스레드가 1개이지만 요청을 비동기적으로 처리함으로써 Tomcat의 스레드는 바로 반환이되어 다시 그 후의 요청에 Tomcat의 스레드를 이용해 요청을 받을 수 있었습니다.
         * 그러나 결과적으로는 실제 비동기 요청을 처리하는 스레드는 요청의 수 만큼 계속 생성되는 것을 확인할 수 있었습니다.
         *
         * 이번에는 이렇게 비동기 요청을 처리하는 스레드의 수도 Netty의 non blocking I/O를 이용함으로써 비동기 요청을 처리하는 스레드도 줄여보고자 합니다.
         * 그러면 결과적으로 tomcat의 스레드 1개, netty의 non blocking I/O를 이용하기위한 필요한 스레드의 수만큼만 생성되어 클라이언트의 요청을 모두 처리할 수 있을 것 입니다.
         */
        @GetMapping("/app3/async_non_block_rest")
        public ListenableFuture<ResponseEntity<String>> asyncNonBlockRest(int idx) {
//            String res = rt.getForObject("http://localhost:8085/remote3/service?req={req}",
//                    String.class, "hello" + idx);
//            log.info("res : {}", res);
//            return res;
            log.info("app3/async_rest idx:{}", idx);
            return art.getForEntity("http://localhost:8085/remote3/service?req={req}",
                    String.class, "hello" + idx);
        }

        @GetMapping("/app3/deferred_rest")
        public DeferredResult<String> deferredRest1(int idx) {
            // 오브젝트를 만들어서 컨트롤러에서 리턴하면 언제가 될지 모르지만 언제인가 DeferredResult에 값을 써주면
            // 그 값을 응답으로 사용
            DeferredResult<String> dr = new DeferredResult<>();

            ListenableFuture<ResponseEntity<String>> f1 = art.getForEntity("http://localhost:8085/service?req={req}",
                    String.class, "hello" + idx);
            f1.addCallback(s -> {
                dr.setResult(s.getBody() + "/work~~");
            }, e -> {
                dr.setErrorResult(e.getMessage());
            });

            return dr;
        }


        @GetMapping("/app3/deferred_rest/hell")
        public DeferredResult<String> rest(int idx) {
            // 오브젝트를 만들어서 컨트롤러에서 리턴하면 언제가 될지 모르지만 언제인가 DeferredResult에 값을 써주면
            // 그 값을 응답으로 사용
            DeferredResult<String> dr = new DeferredResult<>();

            ListenableFuture<ResponseEntity<String>> f1 = art.getForEntity("http://localhost:8085/service?req={req}", String.class, "hello" + idx);
            f1.addCallback(s -> {
                ListenableFuture<ResponseEntity<String>> f2 = art.getForEntity("http://localhost:8085/service2?req={req}", String.class, s.getBody());
                f2.addCallback(s2 -> {
                    dr.setResult(s2.getBody());
                }, e -> {
                    dr.setErrorResult(e.getMessage());
                });

            }, e -> {
                dr.setErrorResult(e.getMessage());
            });

            return dr;
        }


    }

}
