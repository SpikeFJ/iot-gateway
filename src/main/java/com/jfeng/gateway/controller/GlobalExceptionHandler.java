package com.jfeng.gateway.controller;

import com.jfeng.gateway.util.JsonUtils;
import jakarta.servlet.ServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author ruoyi
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, ServletRequest request) {
        log.warn("系统异常", e);
        return JsonUtils.serialize(Resp.fail("系统异常:" + e.getMessage()));
    }
}
