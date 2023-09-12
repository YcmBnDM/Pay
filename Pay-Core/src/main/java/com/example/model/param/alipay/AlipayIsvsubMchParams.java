
package com.example.model.param.alipay;

import com.example.model.param.IsvsubMchParams;
import lombok.Data;

/*
 * 支付宝 特约商户参数定义
 *
 */
@Data
public class AlipayIsvsubMchParams  extends IsvsubMchParams {

    private String appAuthToken;


}
