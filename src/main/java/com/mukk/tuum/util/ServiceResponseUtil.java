package com.mukk.tuum.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ServiceResponseUtil {

    private ServiceResponseUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> ResponseEntity<T> ok(T data) {
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    public static <T> ResponseEntity<T> created(T data) {
        return new ResponseEntity<>(data, HttpStatus.CREATED);
    }

    public static <T> ResponseEntity<T> nok(T data, HttpStatus httpStatus) {
        return new ResponseEntity<>(data, httpStatus);
    }
}
