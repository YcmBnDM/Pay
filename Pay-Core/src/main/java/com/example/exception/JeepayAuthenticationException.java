
package com.example.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

/*
 * Spring Security 框架自定义异常类
 *
 */
@Getter
@Setter
public class JeepayAuthenticationException extends InternalAuthenticationServiceException {

    private BizException bizException;

    public JeepayAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public JeepayAuthenticationException(String msg) {
        super(msg);
    }

    public static JeepayAuthenticationException build(String msg){
        return build(new BizException(msg));
    }

    public static JeepayAuthenticationException build(BizException ex){

        JeepayAuthenticationException result = new JeepayAuthenticationException(ex.getMessage());
        result.setBizException(ex);
        return result;
    }

}
