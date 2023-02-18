package com.study.asy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import javax.inject.Inject;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackService {

    @Value("${slackWebHookUrl:#{null}}")
    private String webHookUrl;

    @Inject
    private final RestTemplate restTemplate;

    @Inject
    private final AsyncRestTemplate myAsyncRestTemplate;

    private String profileName;

    @Inject
    public void setProfileName(Environment environment) {
        this.profileName = Arrays.stream(environment.getActiveProfiles()).collect(Collectors.joining(","));
    }

    public void sendExceptionMessage(String title, String description) throws InterruptedException {
        log.info("SlackService.sendExceptionMessage===");
        Thread.sleep(500l);

        if (webHookUrl != null && webHookUrl.length() > 0) {
            String exceptionTitle = "Exception occured";

            exceptionTitle += " : " + this.profileName;

            List<Map<String, Object>> fields = new ArrayList<>();

            Map<String, Object> fieldMap = new HashMap<>();
            fieldMap.put("title", title);
            fieldMap.put("value", description);
            fieldMap.put("short", true);
            fields.add(fieldMap);

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("pretext", exceptionTitle);
            payloadMap.put("fallback", exceptionTitle);
            payloadMap.put("color", "#FF0000");
            payloadMap.put("fields", fields);


            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> entity = new HttpEntity<>(payloadMap, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    webHookUrl,
                    HttpMethod.PUT,
                    entity,
                    String.class);
//            log.error("=>" + responseEntity.toString());

        }
    }

//    @Async
    public void asyncSendExceptionMessage(String title, String description) throws InterruptedException {
        log.info("SlackService.asyncSendExceptionMessage===");

        Thread.sleep(500l);

        if (webHookUrl != null && webHookUrl.length() > 0) {
            String exceptionTitle = "Exception occured";

            exceptionTitle += " : " + this.profileName;

            List<Map<String, Object>> fields = new ArrayList<>();

            Map<String, Object> fieldMap = new HashMap<>();
            fieldMap.put("title", title);
            fieldMap.put("value", description);
            fieldMap.put("short", true);
            fields.add(fieldMap);

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("pretext", exceptionTitle);
            payloadMap.put("fallback", exceptionTitle);
            payloadMap.put("color", "#FF0000");
            payloadMap.put("fields", fields);


            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> entity = new HttpEntity<>(payloadMap, headers);

            ListenableFuture<?> put = myAsyncRestTemplate.put(
                    webHookUrl,
                    entity);

//            log.error("=>" + responseEntity.toString());
        }
    }
}
