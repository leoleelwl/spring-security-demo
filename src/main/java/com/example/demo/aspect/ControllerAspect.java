package com.example.demo.aspect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@ResponseBody
@ControllerAdvice(
        annotations = {Controller.class,RestController.class}
)
@Slf4j
public class ControllerAspect {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        BindingResult result = e.getBindingResult();
        String errMsg =extractErrorMessage(result);
        return ResponseEntity.of(Optional.of(errMsg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e){
        Set<ConstraintViolation<?>> set = e.getConstraintViolations();
        String errMsg = set.stream().map(el -> el.getRootBeanClass() + " :" + el.getPropertyPath() + " :" + el.getMessage())
                .reduce((s1, s2) -> s1 + " ; " + s2).orElse("校验失败");
        return ResponseEntity.of(Optional.of(errMsg));
    }

    public String extractErrorMessage(BindingResult result){
        List<FieldError> fieldErrors = result.getFieldErrors();
        return fieldErrors.stream().map(e->e.getField() + ": "+e.getDefaultMessage())
                .reduce((s1,s2)-> s1 + " ; "+s2).orElse("参数非法");

    }

/*    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDeniedException(AccessDeniedException e){
        System.out.println("ExceptionHandler "+e.getMessage());
    }*/
}
