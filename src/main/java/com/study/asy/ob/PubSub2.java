package com.study.asy.ob;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Operators
 * <p>
 * publisher -> Data -> Subscriber
 * <p>
 * Publisher -> [Data1] -> Operator -> [Data2] -> Op2 -> [Data3] -> Subscriber
 * <p>
 * 1] map (d1 -> f -> d2)
 * <p>
 * pub -> [Data1] -> mapPub -> [Data2] -> logSub
 * <- subscribe(logSub)
 * -> onSubscribe(s)
 * -> onNext
 * -> onNext
 * -> onNext
 * -> onComplete
 */
@Slf4j
public class PubSub2 {


    public static void main(String[] args) {

        //iterPub 을 호출하면 퍼블리셔를 하나 만들어줌
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10).collect(Collectors.toList()));
//        Publisher<Integer> mapPub = mapPub(pub, (Function<Integer, Integer>) s -> s * 10);
//        Publisher<Integer> map2Pub = mapPub(mapPub, (Function<Integer, Integer>) s -> -s);
//        Publisher<Integer> sumPub = sumPub(pub);
        Publisher<Integer> reducePub = reducePub(pub, 0, (BiFunction<Integer, Integer, Integer>) (a, b) -> a + b);
        reducePub.subscribe(logSub());
    }

    private static Publisher<Integer> reducePub(Publisher<Integer> pub, int init, BiFunction<Integer, Integer, Integer> bf) {

        return new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> sub) {


                pub.subscribe(new DelegateSub(sub) {

                    int result = init;

                    @Override
                    public void onNext(Integer i) {
                        result = bf.apply(result, i);
                    }

                    @Override
                    public void onComplete() {
                        sub.onNext(result);
                        sub.onComplete();
                    }
                });
            }
        };
    }

    private static Publisher<Integer> sumPub(Publisher<Integer> pub) {

        return new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> sub) {

                pub.subscribe(new DelegateSub(sub) {
                    int sum = 0;

                    @Override
                    public void onNext(Integer i) {
                        sum += i;
                    }

                    @Override
                    public void onComplete() {
                        sub.onNext(sum);
                        sub.onComplete();
                    }
                });


            }
        };
    }

    private static Publisher<Integer> mapPub(Publisher<Integer> pub, Function<Integer, Integer> f) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new DelegateSub(sub) {
                    @Override
                    public void onNext(Integer i) {
                        super.onNext(f.apply(i));
                    }
                });
            }
        };
    }


    private static Publisher<Integer> iterPub(List<Integer> iter) {

        return new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> sub) {


                sub.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {

                        try {
                            iter.forEach(s -> sub.onNext(s));
                            sub.onComplete();

                        } catch (Throwable t) {
                            sub.onError(t);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };
    }


    private static Subscriber<Integer> logSub() {
        return new Subscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                log.debug("onSubscribe start");
                s.request(Long.MAX_VALUE);//그냥 다 줘 (무제한)
            }

            //정상
            @Override
            public void onNext(Integer i) {
                log.debug("onNext:{}", i);
            }

            //익셉션 오브젝트를
            @Override
            public void onError(Throwable t) {
                log.debug("onError:{}", t);
            }

            //전통적 옵저버 패턴과 달리 완료되었다 알려주는 것
            @Override
            public void onComplete() {
                log.debug("onComplete");
            }
        };
    }

}