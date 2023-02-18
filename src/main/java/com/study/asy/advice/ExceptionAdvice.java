package com.study.asy.advice;

import com.study.asy.exception.BackOfficeCustomException;
import com.study.asy.service.AsyncService;
import com.study.asy.service.SlackService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(annotations = {RestGlobalExceptionTarget.class})
public class ExceptionAdvice {

    private final AsyncService asyncService;

    private final SlackService slackService;

    @Value("${app.debug-mode:false}")
    private boolean isDebug;

    public ExceptionAdvice(SlackService slackService, AsyncService asyncService) {
        this.slackService = slackService;
        this.asyncService = asyncService;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected Map<String, Object> defaultException(HttpServletRequest req, HttpServletResponse res, Exception ex) {

//    if (ex instanceof org.apache.catalina.connector.ClientAbortException && StringUtils.isNotBlank(ex.getMessage()) && ex.getMessage().indexOf("Broken pipe") > -1) {
//
//      if (isDebug) {
//        print(req, res, ex);
//      }
//    } else {
//      if (isDebug) {
//        print(req, res, ex);
//      } else {
        slackMsg(req, res, ex);
//      }
//    }

        return getResponseModel(HttpStatus.BAD_REQUEST.value(), ex.getMessage());

    }

    @ExceptionHandler(BackOfficeCustomException.class)
    public Map<String, Object> handleError(HttpServletRequest req, HttpServletResponse res,
                                           BackOfficeCustomException ex) {

        log.error("Exception: " + ex.getClass().getName() + " Request : " + req.getRequestURL() + " raised " + ex.getMessage());

        res.setStatus(ex.getStatusCd());

        slackMsg(req, res, ex);

        return getResponseModel(ex.getStatusCd(), ex.getMessage());
    }


    private void print(HttpServletRequest req, HttpServletResponse res, Exception ex) {
        log.error("Exception Request : " + req.getRequestURL() + "res status : " + res.getStatus() + " raised ", ex);
    }


    private void slackMsg(HttpServletRequest req, HttpServletResponse res, Exception ex) {

        String requestMethod = req.getMethod();
        String requestUri = req.getRequestURI();
        String requestQueryString = req.getQueryString();
        //requestBody 는 추후에

        StringBuilder sb = new StringBuilder();
        sb.append("오류가 발생하였습니다.");

        // 요청 http method / request uri + query string
        sb.append("\n\t");
        sb.append(requestMethod);
        sb.append(" : ");
        sb.append(requestUri);

        if (requestQueryString != null && requestQueryString.length() != 0) {
            sb.append("?");
            sb.append(requestQueryString);
        }

        // request info logging
        log.error(sb.toString());

        // common logging print
        print(req, res, ex);

        asyncService.doAsync(() -> {
//            slackService.sendExceptionMessage(sb.toString(), ExceptionUtils.getStackTrace(ex));
            slackService.asyncSendExceptionMessage(sb.toString(), ExceptionUtils.getStackTrace(ex));
        });


    }


    private Map<String, Object> getResponseModel(int statusCd, String message) {

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("code", statusCd);
        errorResult.put("message", message);

        result.put("error", errorResult);

        return result;

    }
}
