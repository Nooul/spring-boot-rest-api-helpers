package com.nooul.apihelpers.springbootrest.controllerAdvices;

import com.nooul.apihelpers.springbootrest.exceptions.NotFoundException;
import com.nooul.apihelpers.springbootrest.utils.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
//extend them and add @ControllerAdvice

@ControllerAdvice
public class GlobalExceptionAdvice {

    @Data
    public class ErrorDetails {
        private Date timestamp;
        private String message;
        private String details;

        public ErrorDetails(Date timestamp, String message, String details) {
            super();
            this.timestamp = timestamp;
            this.message = message;
            this.details = details;
        }
    }

    @Data
    @AllArgsConstructor
    public class JsonLogMessage {
        private String kioskId;
        private String user;
        private String uri;
        private String exceptionMessage;
    }

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionAdvice.class);

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorDetails> unhandledExceptionHandler(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
                request.getDescription(false));

        logger.error("severe", prepareLogMessage(ex, request) + getFirstLinesOfStackTrace(ex, 5));

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getFirstLinesOfStackTrace(Exception ex, int lineLimit) {
        StackTraceElement[] stackTrace = ex.getStackTrace();
        String stackTraceStr = "";
        int linesLogged = 0;
        if (stackTrace != null && stackTrace.length > 0) {
            for (StackTraceElement e : stackTrace) {
                if (linesLogged > lineLimit) {
                    break;
                }
                stackTraceStr += "\n" + e.toString();
                linesLogged++;
            }
        }
        return stackTraceStr;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public final ResponseEntity<ErrorDetails> UnauthorizedHandler401(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
                request.getDescription(false));

        logger.warn("warning", prepareLogMessage(ex, request));

        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<ErrorDetails> ForbiddenHandler403(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
                request.getDescription(false));

        logger.warn("warning", prepareLogMessage(ex, request));

        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public final ResponseEntity<ErrorDetails> BadRequestHandler400(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
                request.getDescription(false));

        logger.warn("warning", prepareLogMessage(ex, request));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<ErrorDetails> notFoundHandler(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
                request.getDescription(false));

        logger.warn("warning", prepareLogMessage(ex, request));

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    private String prepareLogMessage(Exception ex, WebRequest request) {
        String uri = "";
        String user = "";
        if (request instanceof ServletWebRequest) {
            ServletWebRequest servletWebRequest = (ServletWebRequest) request;
            uri = servletWebRequest.getRequest().getRequestURI();
            user = servletWebRequest.getRemoteUser();
        }
        String kioskId = request.getHeader("Kiosk-Id");
        JsonLogMessage jsonObj = new JsonLogMessage(
                kioskId, user, uri, "[" + ex.getClass().getName() + ": " + ex.getMessage() + "]");
        return JSON.toJsonString(jsonObj);
    }
}