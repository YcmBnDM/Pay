package com.example.exception;

import com.example.constants.ApiCodeEnum;
import com.example.model.ApiRes;
import lombok.Getter;

import java.io.Serializable;


/**
 * 自定义的业务异常
 */
@Getter
public class BizException extends RuntimeException implements Serializable  {

    public static final long serialVersionUID = 1L;

    private ApiRes apiRes;

    public BizException(String msg) {
        super(msg);
        this.apiRes = ApiRes.customFail(msg);
    }

    public BizException(ApiCodeEnum apiCodeEnum, String... params) {
        super();
        apiRes = ApiRes.fail(apiCodeEnum, params);
    }

    public BizException(ApiRes apiRes) {
        super(apiRes.getMsg());
        this.apiRes = apiRes;
    }

}
