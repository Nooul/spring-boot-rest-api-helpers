package com.nooul.apihelpers.springbootrest.controllerAdvices;

import com.nooul.apihelpers.springbootrest.exceptions.NotFoundException;
import com.nooul.apihelpers.springbootrest.utils.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.TypeMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.MethodNotAllowedException;

import javax.validation.ConstraintViolationException;
import java.util.Date;
//extend them and add @ControllerAdvice

// based on https://www.baeldung.com/global-error-handler-in-a-spring-rest-api
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
        private String user;
        private String uri;
        private String exceptionMessage;
    }

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionAdvice.class);

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorDetails> unhandledExceptionHandler(Exception ex, WebRequest request) throws Exception {
        logger.error("severe", prepareLogMessage(ex, request) + getFirstLinesOfStackTrace(ex, 5));
        throw ex;
    }

    private String getFirstLinesOfStackTrace(Exception ex, int lineLimit) {
        StackTraceElement[] stackTrace = ex.getStackTrace();
        String stackTraceStr = "";
        int linesLogged = 0;
        if(stackTrace != null && stackTrace.length > 0) {
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

    @ExceptionHandler(value = {IllegalArgumentException.class,
            MissingServletRequestPartException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            TypeMismatchException.class,
            ConstraintViolationException.class,
            BindException.class,
            MethodArgumentNotValidException.class})
    public final ResponseEntity<ErrorDetails> BadRequestHandler400(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
                request.getDescription(false));

        logger.warn("warning", prepareLogMessage(ex, request));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {MethodNotAllowedException.class})
    public final ResponseEntity<ErrorDetails> MethodNotAllowedHandler405(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
                request.getDescription(false));

        logger.warn("warning", prepareLogMessage(ex, request));

        return new ResponseEntity<>(errorDetails, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(value = {HttpMediaTypeNotSupportedException.class})
    public final ResponseEntity<ErrorDetails> UnsupportedMediaTypeHandler415(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
                request.getDescription(false));

        logger.warn("warning", prepareLogMessage(ex, request));

        return new ResponseEntity<>(errorDetails, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
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
        JsonLogMessage jsonObj = new JsonLogMessage(user, uri, "[" + ex.getClass().getName() + ": " + ex.getMessage() + "]");
        return JSON.toJsonString(jsonObj);
    }
}