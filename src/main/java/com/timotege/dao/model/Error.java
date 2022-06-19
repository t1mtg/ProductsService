package com.timotege.dao.model;

public class Error {
    private String message;
    private int code;

    public Error(int code, String message) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "Error{" +
                ", code=" + code +
                "message='" + message + '\'' +
                '}';
    }
}
