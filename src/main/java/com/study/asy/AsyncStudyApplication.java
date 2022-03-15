package com.study.asy;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * https://www.youtube.com/watch?v=aSTuQiPB4Ns&list=PLOLeoJ50I1kkqC4FuEztT__3xKSfR2fpw&index=4
 * 2가지가 핵심
 * Callable, DeferredResult
 */
@Slf4j
@SpringBootApplication
@EnableAsync
public class AsyncStudyApplication {

    @RestController
    public static class MyController2 {

        @Autowired
        MyService2 myService2;

        RestTemplate rt = new RestTemplate();

        //spring 4 (뒷단 스레드 만듬)
//        AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
        //
        AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));


        @GetMapping("/rest")
        public String rest(int idx) {
            return "rest " + idx;
        }

        @GetMapping("/rest2")
        public String rest2(int idx) {

            String res = rt.getForObject("http://localhost:8082/service?req={req}", String.class, "hello" + idx);
            //스레드 개수 1개로 제한했는데, 첫번째 요청이 들어와서 2초가 딱 걸리는 이 작업을 수행하는 동안
            //계속 대기 상태에 빠짐, 두번째 요청은 아이에 들어오지도 못함
            //cpu 상당히 놀고 있을거임, 서버는 요청 처리 못하고, (응답 기다리니까)
            //해결 방법은 API 호출을 비동기적으로 바꾸는 것
            //스프링 3 때는 이걸 완전히 해결 못함 (callable 을 써서 뒷단 워킹쓰레드를 사용한다든가)
            //디퍼드 result 는 외부에서 결과가 만들어질 때 이루어지는 거고
            //노드개발자 : 스프링 블로킹 한다더라 (노노 스레드 2개여도 충분)

            return res;
        }

        @GetMapping("/rest3")
        public ListenableFuture<ResponseEntity<String>> rest3(int idx) {
            ListenableFuture<ResponseEntity<String>> forEntity = asyncRestTemplate.getForEntity("http://localhost:8082/service?req={req}", String.class, "hello" + idx);
            /**
             * 이렇게 리턴해도 무방함
             * 2초동안 대기하지 않음
             * 콜백은 springmvc 가 알아서 해줌
             * 톰캣의 쓰레드는 1개뿐이지만
             * getForEntity 를 해도 결과가 즉시 리턴됨
             * 백그라운드에서 비동기적으로 이 서버 호출을 하고 결과가 오면 spring mvc 한테 리턴을 해서 그 리턴값을 받아서 처리
             * 외부 API 호출할 때 비동기로 호출하는 것은 맞음 (즉시 리턴하니까)
             * 비동기 작업 처리를 위해 백그라운드 스레드 만듬 (100 번 요청 오면 100번 실행)
             * 기본적인 자바의 네트워크 API 를 호출하는 코드가 실행되는거임
             * 사실 서버 자원 100개를 더 사용하는 거임
             * 일시적이지만
             * 매번 스레드를 만드는 것은 큰 비용임
             * 서블릿 스레드 100개 만들고말지 뒷단에 뭐하러 만듬
             * 바람직한 상황 아님
             * 최소한의 API 호출을 비동기적으로 처리하기 위해  스레드 자원만 사용하고 100개의 API 를 동시에 날리고,
             *  동시에 응답받아서 스프링 mvc 에 비동기 mvc 쪽으로 리턴해주는 그런 걸 만들어보는걸 지금부터 할 것임
             */

            return forEntity;
        }

        /**
         * nonblocking io 방식
         * 아파치의 async Http Client
         * netty
         * nonblocking API call 라이브러리는 많음
         * netty 를 사용해보겠음
         */


        @GetMapping("/rest4")
        public DeferredResult<String> rest4(int idx) {

            DeferredResult<String> dr = new DeferredResult<>();

            ListenableFuture<ResponseEntity<String>> f1 = asyncRestTemplate.getForEntity("http://localhost:8082/service?req={req}", String.class, "hello" + idx);

            f1.addCallback(s -> {
                dr.setResult(s.getBody() + "/work");
            }, e -> {
                dr.setErrorResult(e.getMessage());
            });


            return dr;
        }


        static final String URL1 = "http://localhost:8082/service?req={req}";
        static final String URL2 = "http://localhost:8082/service2?req={req}";

        @GetMapping("/rest5")
        public DeferredResult<String> rest5(int idx) {
            DeferredResult<String> dr = new DeferredResult<>();

            ListenableFuture<ResponseEntity<String>> f1 = asyncRestTemplate.getForEntity(URL1, String.class, "hi" + idx);

            f1.addCallback(

                    s -> {
                        ListenableFuture<ResponseEntity<String>> f2 = asyncRestTemplate.getForEntity(URL2, String.class, "hi" + idx);
                        f2.addCallback(s2 -> {
//                            dr.setResult(s2.getBody());
                            ListenableFuture<String> f3 = myService2.work(s2.getBody());

                            f3.addCallback(s3 -> {
                                dr.setResult(s3);

                            }, e -> {
                                dr.setErrorResult(e.getMessage());
                            });

                        }, e -> {
                            dr.setErrorResult(e.getMessage());
                        });
                    },
                    e -> {
                        dr.setErrorResult(e.getMessage());
                    }
            );

            return dr;
        }


        @GetMapping("/rest6")
        public DeferredResult<String> rest6(int idx) {
            DeferredResult<String> dr = new DeferredResult<>();

            System.out.println("MyController2.rest6");

            Completion
                    .from(asyncRestTemplate.getForEntity(URL1, String.class, "hi" + idx))//completion 오브젝트 만들어지고(비동기 작업에 대한)
                    .andAccept(s -> dr.setResult(s.getBody()));


            return dr;
        }

        @GetMapping("/rest7")
        public DeferredResult<String> rest7(int idx) {

            DeferredResult<String> dr = new DeferredResult<>();

            //Continuation Passing Style
            Completion
                    .from(asyncRestTemplate.getForEntity(URL1, String.class, "hi" + idx))//completion 오브젝트 만들어지고(비동기 작업에 대한)
                    .andApply(s -> asyncRestTemplate.getForEntity(URL2, String.class, s.getBody()))
                    .andAccept(s -> dr.setResult(s.getBody()));//수행하고 끝나면 됨 (컨슈머 인터페이스 이용)


            return dr;
        }

        @GetMapping("/rest8")
        public DeferredResult<String> rest8(int idx) {

            DeferredResult<String> dr = new DeferredResult<>();

            //Continuation Passing Style
            Completion
                    .from(asyncRestTemplate.getForEntity(URL1, String.class, "hi" + idx))//completion 오브젝트 만들어지고(비동기 작업에 대한)
                    .andApply(s -> asyncRestTemplate.getForEntity(URL2, String.class, s.getBody()))
                    .andError(e -> dr.setErrorResult(e.toString()))
                    .andAccept(s -> dr.setResult(s.getBody()));//수행하고 끝나면 됨 (컨슈머 인터페이스 이용)


            return dr;
        }

        @GetMapping("/rest9")
        public DeferredResult<String> rest9(int idx) {

            DeferredResult<String> dr = new DeferredResult<>();

            //Continuation Passing Style
            Completion
                    .from(asyncRestTemplate.getForEntity(URL1, String.class, "hi" + idx))//completion 오브젝트 만들어지고(비동기 작업에 대한)
                    .andApply(s -> asyncRestTemplate.getForEntity(URL2, String.class, s.getBody()))
//                    .andApply(s -> myService2.work(s.getBody()))
                    .andError(e -> dr.setErrorResult(e.toString()))
                    .andAccept(s -> dr.setResult(s.getBody()));//수행하고 끝나면 됨 (컨슈머 인터페이스 이용)


            return dr;
        }

        @GetMapping("/rest10")
        public DeferredResult<String> rest10(int idx) {

            DeferredResult<String> dr = new DeferredResult<>();

            toCF(asyncRestTemplate.getForEntity(URL1, String.class, "hi" + idx))
                    .thenCompose(s -> toCF(asyncRestTemplate.getForEntity(URL2, String.class, s.getBody())))
                    .thenCompose(s2 -> toCF(myService2.work(s2.getBody())))
                    .thenAccept(s3 -> dr.setResult(s3))
                    .exceptionally(e -> {
                        dr.setErrorResult(e.getMessage());
                        return (Void) null;
                    });


            return dr;
        }

        @GetMapping("/rest11")
        public DeferredResult<String> rest11(int idx) {

            DeferredResult<String> dr = new DeferredResult<>();

            toCF(asyncRestTemplate.getForEntity(URL1, String.class, "hi" + idx))
                    .thenCompose(s -> toCF(asyncRestTemplate.getForEntity(URL2, String.class, s.getBody())))
                    .thenApplyAsync(s2 -> myService2.work2(s2.getBody()))
                    .thenAccept(s3 -> dr.setResult(s3))
                    .exceptionally(e -> {
                        dr.setErrorResult(e.getMessage());
                        return (Void) null;
                    });


            return dr;
        }


        <T> CompletableFuture<T> toCF(ListenableFuture<T> lf) {
            CompletableFuture<T> cf = new CompletableFuture<T>();
            lf.addCallback(s -> cf.complete(s), e -> cf.completeExceptionally(e));
            return cf;
        }


    }

    public static class ErrorCompletion extends Completion {

        public Consumer<Throwable> econ;

        public ErrorCompletion(Consumer<Throwable> con) {
            this.econ = con;
        }

        @Override
        void run(ResponseEntity<String> value) {
            if (next != null) {
                next.run(value);
            }
        }

        @Override
        public void error(Throwable e) {
            econ.accept(e);
        }
    }

    public static class AcceptCompletion extends Completion {

        Consumer<ResponseEntity<String>> con;

        public AcceptCompletion(Consumer<ResponseEntity<String>> con) {
            this.con = con;
        }

        @Override
        void run(ResponseEntity<String> value) {
            con.accept(value);
        }
    }

    public static class ApplyCompletion extends Completion {

        public Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn;

        public ApplyCompletion(Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn) {
            this.fn = fn;
        }

        @Override
        void run(ResponseEntity<String> value) {
            ListenableFuture<ResponseEntity<String>> lf = fn.apply(value);
            lf.addCallback(s -> complete(s), e -> error(e));

        }
    }


    //    public static class Completion {
//
//        Completion next;
//        Consumer<ResponseEntity<String>> con;
//
//
//        public Completion() {
//        }
//
//        public Completion(Consumer<ResponseEntity<String>> con) {
//            this.con = con;
//        }
//
//        Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn;
//
//        public Completion(Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn) {
//            this.fn = fn;
//        }
//
//        //인스턴스 메소드
//        public void andAccept(Consumer<ResponseEntity<String>> con) {
//            Completion c = new Completion(con);
//            this.next = c;
//        }
//
//        /**
//         * Funcution 첫번째는 인풋, 결과값은 (getForEntity)를 실행하는거니까
//         *
//         * @param function
//         * @return
//         */
//        public Completion andApply(Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> function) {
//            Completion c = new Completion(function);
//            this.next = c;
//            return c;
//        }
//
//
//        public static Completion from(ListenableFuture<ResponseEntity<String>> lf) {
//
//            Completion completion = new Completion();
//
//            lf.addCallback(s -> {
//                completion.complete(s);
//            }, e -> {
//                completion.error(e);
//            });
//            return completion;
//        }
//
//        private void error(Throwable e) {
//
//        }
//
//        private void complete(ResponseEntity<String> value) {
//
//            if (next != null) {
//
//                next.run(value);
//
//            }
//        }
//
//        private void run(ResponseEntity<String> value) {
//
//            if (con != null) {
//
//                con.accept(value);
//
//            } else if (fn != null) {
//
//                ListenableFuture<ResponseEntity<String>> lf = fn.apply(value);
//                lf.addCallback(s -> complete(s), e -> error(e));
//
//            }
//        }

//    public static class Completion {
//
//        Completion next;
//
//        public Completion() {
//        }
//
//        //인스턴스 메소드
//        public void andAccept(Consumer<ResponseEntity<String>> con) {
//            Completion c = new AcceptCompletion(con);
//            this.next = c;
//        }
//
//        /**
//         * Funcution 첫번째는 인풋, 결과값은 (getForEntity)를 실행하는거니까
//         *
//         * @param function
//         * @return
//         */
//        public Completion andApply(Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> function) {
//            Completion c = new ApplyCompletion(function);
//            this.next = c;
//            return c;
//        }
//
//
//        public static Completion from(ListenableFuture<ResponseEntity<String>> lf) {
//
//            Completion completion = new Completion();
//
//            lf.addCallback(s -> {
//                completion.complete(s);
//            }, e -> {
//                completion.error(e);
//            });
//            return completion;
//        }
//
//        public Completion andError(Consumer<Throwable> con) {
//            Completion c = new ErrorCompletion(con);
//            this.next = c;
//            return c;
//        }
//
//        public void error(Throwable e) {
//
//            if (next != null) {
//                next.error(e);
//            }
//
//        }
//
//        public void complete(ResponseEntity<String> value) {
//
//            if (next != null) {
//
//                next.run(value);
//
//            }
//        }
//
//        void run(ResponseEntity<String> value) {
//        }
//    }

    public static class Completion {

        Completion next;

        public Completion() {
        }

        //인스턴스 메소드
        public void andAccept(Consumer<ResponseEntity<String>> con) {
            Completion c = new AcceptCompletion(con);
            this.next = c;
        }

        /**
         * Funcution 첫번째는 인풋, 결과값은 (getForEntity)를 실행하는거니까
         *
         * @param function
         * @return
         */
        public Completion andApply(Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> function) {
            Completion c = new ApplyCompletion(function);
            this.next = c;
            return c;
        }


        public static Completion from(ListenableFuture<ResponseEntity<String>> lf) {

            Completion completion = new Completion();

            lf.addCallback(s -> {
                completion.complete(s);
            }, e -> {
                completion.error(e);
            });
            return completion;
        }

        public Completion andError(Consumer<Throwable> con) {
            Completion c = new ErrorCompletion(con);
            this.next = c;
            return c;
        }

        public void error(Throwable e) {

            if (next != null) {
                next.error(e);
            }

        }

        public void complete(ResponseEntity<String> value) {

            if (next != null) {

                next.run(value);

            }
        }

        void run(ResponseEntity<String> value) {
        }
    }


    @Service
    public static class MyService2 {

        @Async
        public ListenableFuture<String> work(String req) {
            return new AsyncResult<>(req + "/asyncwork");
        }

        public String work2(String req2) {
            return req2 + "/asnyc_work2";
        }

    }

    @Bean
    public ThreadPoolTaskExecutor myThreadPool() {

        //자바의 기본적인 스레드 풀 동작 원리 - queue 채우고 그 다음에 큐까지 다 찼을 때
        //maxpoolsize 까지 스레드를 더 늘렸다가 그것까지 다 차면 에러가 남

        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        te.setCorePoolSize(1);
        te.setMaxPoolSize(1);
        te.initialize();
        ;
        return te;

    }


    @RestController
    public static class MyController {

        @GetMapping("/async")
        public String async() throws InterruptedException {
            log.info("async");
            Thread.sleep(2000);
            return "hello";
        }

        @GetMapping("/callable")
        public Callable<String> callable() throws InterruptedException {
//            log.info("MyController.callable");
            return () -> {
                log.info("MyController.callable a sync");
                Thread.sleep(2000);
                return "hello";
            };
        }

        Queue<DeferredResult<String>> results = new ConcurrentLinkedQueue<>();

        @GetMapping("/dr")
        public DeferredResult<String> dr() throws InterruptedException {
            log.info("dr");
            DeferredResult<String> dr = new DeferredResult<>(60000L);
            results.add(dr);
            return dr;
        }

        @GetMapping("/dr/count")
        public String drCount() {
            return String.valueOf(results.size());
        }

        @GetMapping("/dr/event")
        public String drEvent(String msg) {

            for (DeferredResult<String> dr : results) {
                dr.setResult("Hello " + msg);
                results.remove(dr);
            }
            return "OK";
        }

        @GetMapping("/emitter")
        public ResponseBodyEmitter emitter() throws InterruptedException {

            ResponseBodyEmitter emitter = new ResponseBodyEmitter();

            Executors.newSingleThreadExecutor().submit(() -> {

                for (int i = 1; i < 50; i++) {
                    try {
                        emitter.send("<p>Stream " + i + "</p>");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });
            return emitter;
        }
    }


    /**
     * 서블릿은 기본적으로 블로킹 IO 방식임
     */
    @Component
    public static class MyService {


//        @Async
//        public ListenableFuture<String> hello() throws InterruptedException {
//            log.info("hello()");
//            Thread.sleep(1000);
//            return new AsyncResult<>("Hello");
//        }

//        @Bean
//        ThreadPoolTaskExecutor tp() {
//            ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
//            threadPoolTaskExecutor.setCorePoolSize(10);
//            threadPoolTaskExecutor.setMaxPoolSize(100);
//            threadPoolTaskExecutor.setQueueCapacity(200);
//            threadPoolTaskExecutor.setThreadNamePrefix("myThread kk");
//            threadPoolTaskExecutor.initialize();
//            return threadPoolTaskExecutor;
//        }

        //나중에
//        @Async
//        public CompletableFuture<String> hello2() throws InterruptedException {
//            log.info("hello()");
//            Thread.sleep(1000);
//            return new AsyncResult<>("Hello");
//        }
    }


    public static void main(String[] args) {

//        try (ConfigurableApplicationContext c = SpringApplication.run(BraintuningApplication.class, args)) {
//
//        }

        SpringApplication.run(AsyncStudyApplication.class, args);
    }

    @Autowired
    MyService myService;


    //일종의 컨트롤러
//    @Bean
//    ApplicationRunner run() {
//        return args -> {
//            log.info("run()");
//            ListenableFuture<String> hello = myService.hello();
//            hello.addCallback(s -> System.out.println(s), e -> System.out.println(e.getMessage()));
//            hello.cancel(true);
//            log.info("exit");
//        };
//    }
}
