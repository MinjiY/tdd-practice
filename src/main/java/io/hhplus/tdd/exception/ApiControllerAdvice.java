package io.hhplus.tdd.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
    }

    @ExceptionHandler(TddServerException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(TddServerException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(ex.getCode(), String.valueOf(request));

        return ResponseEntity
                .status(response.getCode())
                .body(response);
    }
}
