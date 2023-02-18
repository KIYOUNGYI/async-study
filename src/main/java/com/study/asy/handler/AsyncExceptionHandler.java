package com.study.asy.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

@Component
@Slf4j
public class AsyncExceptionHandler {


    public void exceptionHandle(Exception ex) {

        if (ex instanceof HttpServerErrorException) {
            HttpServerErrorException httpException = (HttpServerErrorException) ex;

            log.error("httpException.getMessage() : " + httpException.getMessage());
            log.error("httpException.getResponseBodyAsString() : " + httpException.getResponseBodyAsString());
            log.error("", httpException);

        } else {
            log.error("", ex);
        }

        // slack send
        StringBuilder sb = new StringBuilder();
        sb.append("비동기 호출 오류가 발생하였습니다.");
        sb.append("\n");

        if (ex instanceof HttpServerErrorException) {
            HttpServerErrorException httpException = (HttpServerErrorException) ex;
            sb.append(httpException.getMessage());
            sb.append(" : ");
            sb.append(httpException.getResponseBodyAsString());
            sb.append("\n");
        }

    }

}