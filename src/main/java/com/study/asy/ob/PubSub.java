package com.study.asy.ob;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PubSub {

    public static void main(String[] args) throws InterruptedException {

        //publisher<-observable
        //Subscriber<-Observer

        //메소드가 1개면 람다, 여러개면 익명클래스로 구현
        ExecutorService es = Executors.newSingleThreadExecutor();

        Iterable<Integer> iter = Arrays.asList(1, 2, 3, 4, 5);//디비에서 가져온 데이터라 생각해도 됨

        //data 주는 쪽
        Publisher p = new Publisher() {

            @Override
            public void subscribe(Subscriber subscriber) {


                Iterator<Integer> it = iter.iterator();

                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {

                        es.execute(() -> {
                            int i = 0;
                            while (i++ < n) {
                                if (it.hasNext()) {
                                    subscriber.onNext(it.next());
                                } else {
                                    subscriber.onComplete();
                                    break;
                                }
                            }
                        });


                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        //데이터를 받는 쪽
        Subscriber<Integer> s = new Subscriber<Integer>() {


            Subscription subscription;

            //필수
            @Override
            public void onSubscribe(Subscription subscription) {
                log.info("onSubscribe");
                this.subscription = subscription;
                subscription.request(1);
            }

            //옵저버 패턴의 업데이트와 같음 (퍼블리셔가 데이터를 주면 받는)
            //옵셔널 (무제한 가능)
            @Override
            public void onNext(Integer item) {
                log.info("onNext {}", item);
                this.subscription.request(1);

            }

            //애 때문에 try,catch 로 감쌀 필요가 없음
            //onComplete 와 onError 둘 중 하나 (쓸거면) / 옵셔널임
            @Override
            public void onError(Throwable t) {
                log.info("onError");
            }

            @Override
            public void onComplete() {
                log.info("onComplete");
            }
        };

        //서브스크라이버가 퍼블리셔한테 서브스크라이빙 해야 되쥬

        p.subscribe(s);

        es.awaitTermination(10l, TimeUnit.SECONDS);
        es.shutdown();
    }
}
