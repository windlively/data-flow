package ink.windlively.dataflow.server.web.controller;

import ink.windlively.dataflow.server.entity.HttpResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static ink.windlively.dataflow.server.entity.HttpResult.FAILED;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(Exception.class)
    public HttpResult<?> exception(Exception ex){
        log.error("{}", ex.getMessage(), ex);
        return HttpResult.FAILED(ex.toString());
    }



}
