package io.hhplus.tdd.exception;

public class IllegalArgumentException extends TddServerException {

    public IllegalArgumentException(String code, String message) {
        super(code, message);
    }

    public IllegalArgumentException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
