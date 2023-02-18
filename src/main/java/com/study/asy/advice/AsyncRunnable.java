package com.study.asy.advice;

@FunctionalInterface
public interface AsyncRunnable {

    abstract void run() throws InterruptedException;
}

