package com.example.common;

import lombok.Data;

/**
 * 统一返回结果类型
 * @param <T>
 */

@Data
public class Result<T> {
    private String code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T object) {
        Result<T> r = new Result<>();
        r.data = object;
        r.code = "200";
        return r;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.msg = msg;
        r.code = "500";
        return r;
    }
}
