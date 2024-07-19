package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 自定义异常
     * @param e
     * @return
     */
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(XueChengPlusException e) {
        //记录异常
        log.error("自定义异常：{}", e.getErrMessage());
        //解析出异常信息
        String errMessage = e.getErrMessage();
        RestErrorResponse restErrorResponse = new RestErrorResponse(errMessage);
        return restErrorResponse;
    }


    /**
     * 系统异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception e) {
        //记录异常
        log.error("系统异常：{}", e.getMessage());
        //解析出异常信息
        RestErrorResponse restErrorResponse = new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
        return restErrorResponse;
    }



}
