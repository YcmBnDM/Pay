
package com.example.pay.channel;


import com.example.pay.service.ConfigContextQueryService;
import com.example.pay.util.ChannelCertConfigKitBean;
import com.example.service.impl.SysConfigService;

import javax.annotation.Resource;



/*
* 退款接口抽象类
*
*/
public abstract class AbstractRefundService implements IRefundService{

    @Resource
    protected SysConfigService sysConfigService;
    @Resource protected ChannelCertConfigKitBean channelCertConfigKitBean;
    @Resource protected ConfigContextQueryService configContextQueryService;

    protected String getNotifyUrl(){
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/refund/notify/" + getIfCode();
    }

    protected String getNotifyUrl(String refundOrderId){
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/refund/notify/" + getIfCode() + "/" + refundOrderId;
    }

}
