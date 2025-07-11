package io.hhplus.tdd.exception;

import lombok.Getter;

@Getter
public class TddServerException extends RuntimeException {

    private final String code;

    public TddServerException(String code, String message) {
        super(message);
        this.code = code;
    }

    public TddServerException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
