package com.study.asy;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
public class FutureEx {

    interface SuccessCallback {
        void onSuccess(String result);
    }

    interface ExceptionCallback {
        void onError(Throwable t);
    }

    public static class CallbackFutureTask extends FutureTask<String> {

        SuccessCallback sc;
        ExceptionCallback ec;

        /**
         * Callable<String> callable <- 수행해야 할 비동기 작업
         * SuccessCallback sc <- 성공했을 때 던저야 하는 녀석
         */
        public CallbackFutureTask(Callable<String> callable, SuccessCallback sc, ExceptionCallback ec) {
            super(callable);
//            if (sc == null) throw null;
            this.sc = Objects.requireNonNull(sc);//null 차단하는 간단한 코드
            this.ec = Objects.requireNonNull(ec);
        }

        @Override
        protected void done() {
            log.info("done()");
            try {
                String s = get();
                log.info("s->", s);
                sc.onSuccess(get());
            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                //인터럽트 났다는 시그널을 던지는게 중요, 위 코드면 충분함

            } catch (ExecutionException e) {
                //비동기 작업을 수행하다 그 안에서 예외가 발생하면
                ec.onError(e.getCause());
            }
        }
    }

    // Future   - 자바 8 이전 / 비동기 작업, 블로킹 (경악) 말이 되냐,
    // Callback - 미래의 실행 작업을 가져다가 이런저런 조건이 충족되면 수행해주세요. (일종의 커맨드 패턴, 전략 패턴을 아우르는건데)


    public static void main(String[] args) throws InterruptedException, ExecutionException {
        //스레드 풀
        //스레드를 만들고 폐기하는 비용 큼
        //새로 만드는 비용 cpu, memory 절약하자 (재사용해서)
        ExecutorService es = Executors.newCachedThreadPool();

//        case2(es);

//        case3(es);

//        case4(es);

//        case5(es);

//        case6(es);

        //퓨처라는 것은 스레드 풀에 submit 을 이용해서 작업을 던지고 그 결과를 받아오는 건데,
        //아이에 퓨처 자체를 오브젝트로 만들 수 있는 방법
        //Future Task 이용하기

//        case7(es);

//        case8(es);

        case9(es);

    }

    private static void case9(ExecutorService es) {

        CallbackFutureTask f = new CallbackFutureTask(() -> {

            Thread.sleep(2000);
            if (1 == 1) {
                throw new RuntimeException("Async Error!!!");
            }
            log.info("Async");
            return "Hello";

        },
                s -> log.info("Result : {}", s),
                e -> log.info("Error: {}", e.getMessage()));

        es.execute(f);
        es.shutdown();
        ;
    }


    private static void case8(ExecutorService es) throws InterruptedException, ExecutionException {

        FutureTask<String> f = new FutureTask<String>(
                () -> {
                    Thread.sleep(2000);
                    log.info("Async");
                    return "Hello";
                }
        ) {
            //hook
            @Override
            protected void done() {
                try {
                    log.info(get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };

        es.execute(f);
        es.shutdown();
    }

    private static void case7(ExecutorService es) throws InterruptedException, ExecutionException {

        FutureTask<String> f = new FutureTask<String>(
                () -> {
                    Thread.sleep(2000);
                    log.info("Async");
                    return "Hello";
                }
        );

        es.execute(f);

        log.info(String.valueOf(f.isDone()));
        Thread.sleep(2100);

        log.info("Exit");//이걸 종료라 생각하지 말고 동시에 실행해야 할 작업이라 생각해보자
        //Exit -> Aync, Hello 이런 식으로 흘러가겠지

        log.info(String.valueOf(f.isDone()));//작업 끝나면 그 때 가져와~

        log.info(f.get());

        es.shutdown();
    }

    /**
     * isDone
     */
    private static void case6(ExecutorService es) throws InterruptedException, ExecutionException {
        //main thread 에서 가져오고 싶어 -> 이때 사용하는게 Future

        Future<String> f = es.submit(() -> {
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";//future 의 get 을 사용하면 비동기 작업이 완료될 때까지 블로킹 됨, 사실 이러면 스레드 풀 만들어서 실행할 이유가 없음
        });

        log.info(String.valueOf(f.isDone()));
        Thread.sleep(2100);

        log.info("Exit");//이걸 종료라 생각하지 말고 동시에 실행해야 할 작업이라 생각해보자
        //Exit -> Aync, Hello 이런 식으로 흘러가겠지

        log.info(String.valueOf(f.isDone()));//작업 끝나면 그 때 가져와~

        log.info(f.get());
    }

    /**
     * 뭔가 다른 비동기 작업이랑 쓰기 나쁘진 않은 방법일 수 있음
     */
    private static void case5(ExecutorService es) throws InterruptedException, ExecutionException {
        //main thread 에서 가져오고 싶어 -> 이때 사용하는게 Future

        Future<String> f = es.submit(() -> {
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";//future 의 get 을 사용하면 비동기 작업이 완료될 때까지 블로킹 됨, 사실 이러면 스레드 풀 만들어서 실행할 이유가 없음
        });

        log.info("Exit");//이걸 종료라 생각하지 말고 동시에 실행해야 할 작업이라 생각해보자
        //Exit -> Aync, Hello 이런 식으로 흘러가겠지

        log.info(f.get());
    }

    /**
     * Async 먼저 찍히고
     * Future 를 통해서 get 을 호출하는 시점에 비동기 작업의 결과를 리턴한 값이 get 을 통해서 넘어옴
     * 그러고 exit 이 넘어옴
     */
    private static void case4(ExecutorService es) throws InterruptedException, ExecutionException {
        //main thread 에서 가져오고 싶어 -> 이때 사용하는게 Future

        Future<String> f = es.submit(() -> {
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";//future 의 get 을 사용하면 비동기 작업이 완료될 때까지 블로킹 됨, 사실 이러면 스레드 풀 만들어서 실행할 이유가 없음
        });

        log.info(f.get());
        log.info("Exit");
    }

    private static void case3(ExecutorService es) {

        es.submit(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            log.info("Async");
            return "Hello";
        });

        log.info("Exit");
    }

    private static void case2(ExecutorService es) {
        es.execute(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            log.info("Async");
        });

        log.info("Exit");
    }
}
