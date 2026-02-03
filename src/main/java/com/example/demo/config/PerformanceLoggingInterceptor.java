package com.example.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class PerformanceLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String fullUri = queryString != null ? uri + "?" + queryString : uri;
            int status = response.getStatus();
            
            // 느린 요청만 로깅 (예: 1초 이상)
            if (duration > 1000) {
                log.warn("⚠️ 느린 요청 감지: {} {} - {}ms (status: {})", method, fullUri, duration, status);
            } else if (duration > 500) {
                log.info("⏱️ 요청 처리: {} {} - {}ms (status: {})", method, fullUri, duration, status);
            } else {
                log.debug("요청 처리: {} {} - {}ms (status: {})", method, fullUri, duration, status);
            }
        }
    }
}



























