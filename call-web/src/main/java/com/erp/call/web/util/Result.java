package com.erp.call.web.util;

import java.io.Serializable;

public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean status = false;

    private String message;

    private T result;

    private String statusCode;

    /**
     * 返回一个错误的结果集
     *
     * @param message 错误信息
     * @return
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(message, null, "401");
    }

    /***
     * 返回一个业务处理正常结果集
     * @param data   数据参数
     * @param <T>    泛型
     * @return
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, "操作成功", data, "SYS000");
    }

    public Result() {
        super();
    }

    public Result(String message, T result, String statusCode) {
        this.message = message;
        this.result = result;
        this.statusCode = statusCode;
    }

    public Result(boolean status, String message, T result, String statusCode) {
        this.status = status;
        this.message = message;
        this.result = result;
        this.statusCode = statusCode;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

}

