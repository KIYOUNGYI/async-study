package com.study.asy.service;

import com.study.asy.advice.AsyncRunnable;
import com.study.asy.handler.AsyncExceptionHandler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class AsyncService {


    @Inject
    AsyncExceptionHandler asyncExceptionHandler;

    @Async
    public void doAsync(AsyncRunnable asyncRunnable) {

        try {
            asyncRunnable.run();
        } catch (Exception e) {
            exceptionHandle(e);
        }
    }


    private void exceptionHandle(Exception e) {
        asyncExceptionHandler.exceptionHandle(e);
    }


}
