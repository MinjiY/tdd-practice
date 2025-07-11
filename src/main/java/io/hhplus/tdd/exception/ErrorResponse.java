package io.hhplus.tdd.exception;

public record ErrorResponse(
        String code,
        String message
) {

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message);
    }

    public ErrorResponse(TddServerException e) {
        this(e.getCode(), e.getMessage());
    }

    public int getCode() {
        return Integer.parseInt(this.code);
    }
}
