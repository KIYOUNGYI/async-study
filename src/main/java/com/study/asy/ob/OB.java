package com.study.asy.ob;

import lombok.extern.slf4j.Slf4j;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class OB {

    public static void main(String[] args) {

//        Iterable<Integer> items = () -> {
//            return new Iterator<Integer>() {
//                int i = 0;
//                static final int MAX = 10;
//
//                @Override//오버라이드 생략 가능
//                public boolean hasNext() {
//                    return i < MAX;
//                }
//
//                public Integer next() {
//                    return ++i;
//                }
//            };
//        };
//
//
//        for (Integer item : items) {
//            System.out.println(item);
//        }



        //옵저버 (나중에 리액티브에선 -> 퍼블리셔 ) / 소스를 만드는
        Observer ob = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                log.info(arg.toString());
            }
        };

        IntObservable io = new IntObservable();
        io.addObserver(ob);

//        io.run();

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(io);

        log.info("exit");

        es.shutdown();
    }


    static class IntObservable extends Observable implements Runnable {
        //subject, 리액티브에선 서브스크라이버쪽 , 소스를 받는 쪽
        @Override
        public void run() {

            for (int i = 1; i <= 10; i++) {
                setChanged();
                //push
                notifyObservers(i);//data 넘겨주는 action 이 일어나는 코드

                //pull
                //int i = it.next();

            }

        }
    }
}
