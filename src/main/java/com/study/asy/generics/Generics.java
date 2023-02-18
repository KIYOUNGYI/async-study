package com.study.asy.generics;

import java.util.Arrays;

public class Generics {

    static <T extends Comparable<T>> long greaterThan(T[] arr, T elem) {

        return Arrays.stream(arr).filter(s -> s.compareTo(elem) > 0).count();
    }

    public static void main(String[] args) {
        String[] strings = {"a", "b", "c", "d", "e"};
        System.out.println("greaterThan(strings,\"b\") = " + greaterThan(strings, "b"));

        Integer i = 10;
        Number n = i;

    }
}
