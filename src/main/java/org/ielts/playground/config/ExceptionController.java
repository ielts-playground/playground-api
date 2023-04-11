package org.ielts.playground.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.ielts.playground.common.constant.ValidationConstants;
import org.ielts.playground.common.exception.BadRequestException;
import org.ielts.playground.model.entity.OhMyError;
import org.ielts.playground.repository.OhMyErrorRepository;
import org.joda.time.LocalDateTime;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.ielts.playground.common.exception.BaseWebException;
import org.ielts.playground.model.response.ErrorResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ControllerAdvice
public class ExceptionController implements AsyncUncaughtExceptionHandler {
    private final ExceptionController self;
    private final OhMyErrorRepository repository;

    public ExceptionController(
            @Lazy ExceptionController self,
            OhMyErrorRepository repository) {
        this.self = self;
        this.repository = repository;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationRequest(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error(ex.getMessage());
        return handleErrorRequest(new BadRequestException(ValidationConstants.ARGUMENT_INVALID), request);
    }

    @ExceptionHandler(BaseWebException.class)
    public ResponseEntity<ErrorResponse> handleErrorRequest(BaseWebException ex, HttpServletRequest request) {
        this.self.track(
                false,
                ex,
                request.getServletPath(),
                request.getMethod(),
                ex.getHttpStatus(),
                null);
        return ResponseEntity.status(ex.getHttpStatus())
                .body(from(ex, request));
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleRest(Throwable ex, HttpServletRequest request) {
        this.self.track(
                false,
                ex,
                request.getServletPath(),
                request.getMethod(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(from(ex, request));
    }

    private ErrorResponse from(Throwable ex, HttpServletRequest request) {
        final HttpStatus status = (ex instanceof BaseWebException)
                ? ((BaseWebException) ex).getHttpStatus()
                : HttpStatus.INTERNAL_SERVER_ERROR;
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toDate().getTime())
                .status(status.value())
                .path(request.getServletPath())
                .method(request.getMethod())
                .message(ex.getMessage())
                .build();
    }

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        this.self.track(
                true,
                ex,
                null,
                null,
                HttpStatus.INTERNAL_SERVER_ERROR,
                method,
                params);
    }

    @Async
    @Transactional
    public void track(boolean async, Throwable ex, String path, String httpMethod, HttpStatus httpStatus, Method method, Object... params) {
        log.error(ex.getMessage(), ex);
        final OhMyError error = new OhMyError();
        error.setAsync(async);
        error.setPath(path);
        error.setHttpMethod(httpMethod);
        error.setHttpStatus(httpStatus.value());
        if (Objects.nonNull(method)) {
            error.setAppMethod(method.getName());
        }
        if (params.length > 0) {
            error.setParams(StringUtils.arrayToCommaDelimitedString(params));
        }
        error.setMessage(ex.getMessage());
        try (final StringWriter stackTrace = new StringWriter()) {
            ex.printStackTrace(new PrintWriter(stackTrace));
            error.setStackTrace(stackTrace.toString());
        } catch (IOException ioException) {
            // no need to care!
        }
        this.repository.save(error);
    }
}
