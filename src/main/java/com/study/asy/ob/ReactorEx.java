package com.study.asy.ob;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class ReactorEx {

    public static void main(String[] args) {

        //publisher
        Flux.<Integer>create(e -> {
            e.next(1);
            e.next(2);
            e.next(3);
            e.complete();
        }).log()
        .map(s -> s * 10)
        .log()
        .subscribe(s -> System.out.println(s));
    }
}
