package com.study.asy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 쓰레드의 동기화 기법
 * CyclicBarrier -> 어느 순간에 경계를 만들어둠, 거기에 숫자를 만들어둠
 * 어느 시점에 가서 await() 을 적어두고 만나는 순간 블로킹됨
 * 숫자가 도달할 때까지
 * 그 순간 코드 블록 싹 다 풀림
 * <p>
 * execute -> runnable 애는 기본적으로 exception 을 던지게 되어 있지 않음
 * <p>
 * 그래서 callable 인터페이스 사용
 * <p>
 * Callable 은 리턴 값이 있음 / exception 던지는 코드 있음
 * Runnable
 */
@Slf4j
public class LoadTest2 {

    static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {


        ExecutorService es = Executors.newFixedThreadPool(100);
        RestTemplate rt = new RestTemplate();
        String url = "http://localhost:8080/rest11?idx={idx}";

        CyclicBarrier barrier = new CyclicBarrier(101);

//        for (int i = 0; i < 100; i++) {
//            es.execute(() -> {
//                int idx = counter.addAndGet(1);
//                log.info("Thread {}" , idx);
//
//                try {
//                    barrier.await();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (BrokenBarrierException e) {
//                    e.printStackTrace();
//                }
//
//                StopWatch sw = new StopWatch();
//                sw.start();
//
//                rt.getForObject(url, String.class);
//
//                sw.stop();
//                log.info("Elapsed: " + idx + " -> " + sw.getTotalTimeSeconds());
//            });
//        }
        for (int i = 0; i < 100; i++) {
            es.submit(() -> {
                int idx = counter.addAndGet(1);
//                log.info("Thread {}" , idx);

                barrier.await();

                StopWatch sw = new StopWatch();
                sw.start();

                String res = rt.getForObject(url, String.class, idx);
                //외부 서비스 호출하는 동안에 오래 거림
                //API 를 호출하고 나서 결과가 돌아올 때까지는 스레드가 대기 상태에 있음

                sw.stop();
                log.info("Elapsed: {} {} / {}", idx, sw.getTotalTimeSeconds(), res);
                return null;//쓰레드 종료
            });
        }

        barrier.await();
        StopWatch main = new StopWatch();
        main.start();

        es.shutdown();
        es.awaitTermination(100, TimeUnit.SECONDS);

        main.stop();
        log.info("Total: {}", main.getTotalTimeSeconds());

    }
}
