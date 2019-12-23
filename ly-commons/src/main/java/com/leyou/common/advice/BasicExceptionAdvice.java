package com.leyou.common.advice;


import com.leyou.common.exceptions.ExceptionResult;
import com.leyou.common.exceptions.LyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice //作用域在每个controller上
public class BasicExceptionAdvice {

    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> handleException(LyException e){

        //从LyException中获取信息
        return ResponseEntity.ok(new ExceptionResult(e));

    }
}
