package ink.andromeda.dataflow.web.controller;

import ink.andromeda.dataflow.entity.HttpResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static ink.andromeda.dataflow.entity.HttpResult.FAILED;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(Exception.class)
    public HttpResult<?> exception(Exception ex){
        ex.printStackTrace();
        log.error("{}", ex.getMessage(), ex);
        return HttpResult.FAILED(ex.toString());
    }



}
