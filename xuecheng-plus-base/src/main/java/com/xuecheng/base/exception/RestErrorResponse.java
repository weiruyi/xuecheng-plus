package com.xuecheng.base.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 错误响应参数包装
 */
@Data
@AllArgsConstructor
public class RestErrorResponse implements Serializable {

    private String errMessage;

//    public RestErrorResponse(String errMessage){
//        this.errMessage= errMessage;
//    }
//
//    public String getErrMessage() {
//        return errMessage;
//    }
//
//    public void setErrMessage(String errMessage) {
//        this.errMessage = errMessage;
//    }
}