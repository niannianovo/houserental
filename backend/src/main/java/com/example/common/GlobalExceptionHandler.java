package com.example.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理所有运行时异常
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handlerRuntimeException(RuntimeException e){
        log.error("运行时异常:{}",e.getMessage(),e);
        return Result.error(e.getMessage());
    }

    // 处理所有异常
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常：{}", e.getMessage(), e);
        return Result.error("系统繁忙，请稍后重试");
    }
}
