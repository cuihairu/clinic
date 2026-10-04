package com.clinic.exception;

import com.clinic.vo.MessageView;
import com.clinic.vo.ExceptionView;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

@RestControllerAdvice
public class ResponseExceptionAdvice {

    /**
     *
     * */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionView ParamValidatedError(MethodArgumentNotValidException e) {
        return ExceptionView.builder().success(false).errorMessage(e.getFieldErrors().get(0).getDefaultMessage()).build();
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, ConstraintViolationException.class,IllegalArgumentException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionView ParamError(Exception e) {
        return ExceptionView.builder().success(false).errorMessage(e.getLocalizedMessage()).build();
    }

    @ExceptionHandler({UsernameNotFoundException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionView StaffError(ConstraintViolationException e) {
        return ExceptionView.builder().success(false).errorMessage(e.getLocalizedMessage()).build();
    }

    @ExceptionHandler({LockedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionView Forbidden(Exception e){
        return ExceptionView.builder().success(false).errorMessage(e.getLocalizedMessage()).build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionView exception(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return ExceptionView.builder().success(false).errorMessage(e.getLocalizedMessage()).data(errors.toString()).build();
    }
}
