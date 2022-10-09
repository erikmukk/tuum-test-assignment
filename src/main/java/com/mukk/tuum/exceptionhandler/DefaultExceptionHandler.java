package com.mukk.tuum.exceptionhandler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.mukk.tuum.exception.AccountMissingException;
import com.mukk.tuum.exception.ExceptionTexts;
import com.mukk.tuum.exception.TransactionException;
import com.mukk.tuum.util.ApiErrorResponse;
import com.mukk.tuum.util.ServiceResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
public class DefaultExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<ApiErrorResponse> handle(HttpMessageNotReadableException ex, WebRequest request) {
        final var errorResponse = ApiErrorResponse.builder()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ifx = (InvalidFormatException) ex.getCause();
            if (ifx.getTargetType() != null && ifx.getTargetType().isEnum()) {
                String message = String.format(ExceptionTexts.INVALID_ENUM_FIELD_VALUE, ifx.getPath().get(0).getFieldName());
                String details = String.format(ExceptionTexts.INVALID_ENUM_EXPLANATION, ifx.getValue(), Arrays.toString(ifx.getTargetType().getEnumConstants()));
                errorResponse.setMessage(message);
                errorResponse.setDetail(details);
            }
        }
        return ServiceResponseUtil.nok(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ApiErrorResponse> handle(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> messages = ex.getBindingResult().getAllErrors().stream()
                .map(e -> ((FieldError) e).getField() + " field - " + e.getDefaultMessage())
                .collect(Collectors.toList());
        final var errorResponse = ApiErrorResponse.builder()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .message(messages)
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
        return ServiceResponseUtil.nok(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(AccountMissingException.class)
    @ResponseBody
    public ResponseEntity<ApiErrorResponse> handle(AccountMissingException ex, WebRequest request) {
        final var errorResponse = ApiErrorResponse.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
        return ServiceResponseUtil.nok(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TransactionException.class)
    @ResponseBody
    public ResponseEntity<ApiErrorResponse> handle(TransactionException ex, WebRequest request) {
        final var errorResponse = ApiErrorResponse.builder()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .message(ex.getMessage())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();
        return ServiceResponseUtil.nok(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
