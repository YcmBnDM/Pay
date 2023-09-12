package com.example.model.param.ysf;

import com.example.model.param.IsvsubMchParams;
import lombok.Data;

/*
 * 云闪付 配置信息
 *
 */
@Data
public class YsfpayIsvsubMchParams extends IsvsubMchParams {

    private String merId;   // 商户编号

}
