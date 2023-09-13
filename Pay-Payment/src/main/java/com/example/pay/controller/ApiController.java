
package com.example.pay.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.constants.CS;
import com.example.ctrl.AbstractCtrl;
import com.example.entity.MchApp;
import com.example.exception.BizException;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.AbstractMchAppRQ;
import com.example.pay.rqrs.AbstractRQ;
import com.example.pay.service.ConfigContextQueryService;
import com.example.pay.service.ValidateService;
import com.jeequan.jeepay.util.JeepayKit;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;

;

/*
* api 抽象接口， 公共函数
*
*/
public abstract class ApiController extends AbstractCtrl {

    @Resource
    private ValidateService validateService;
    @Resource private ConfigContextQueryService configContextQueryService;


    /** 获取请求参数并转换为对象，通用验证  **/
    protected <T extends AbstractRQ> T getRQ(Class<T> cls){

        T bizRQ = getObject(cls);

        // [1]. 验证通用字段规则
        validateService.validate(bizRQ);

        return bizRQ;
    }


    /** 获取请求参数并转换为对象，商户通用验证  **/
    protected <T extends AbstractRQ> T getRQByWithMchSign(Class<T> cls){

        //获取请求RQ, and 通用验证
        T bizRQ = getRQ(cls);

        AbstractMchAppRQ abstractMchAppRQ = (AbstractMchAppRQ)bizRQ;

        //业务校验， 包括： 验签， 商户状态是否可用， 是否支持该支付方式下单等。
        String mchNo = abstractMchAppRQ.getMchNo();
        String appId = abstractMchAppRQ.getAppId();
        String sign = bizRQ.getSign();

        if(StringUtils.isAnyBlank(mchNo, appId, sign)){
            throw new BizException("参数有误！");
        }

        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(mchNo, appId);

        if(mchAppConfigContext == null){
            throw new BizException("商户或商户应用不存在");
        }

        if(mchAppConfigContext.getMchInfo() == null || mchAppConfigContext.getMchInfo().getState() != CS.YES){
            throw new BizException("商户信息不存在或商户状态不可用");
        }

        MchApp mchApp = mchAppConfigContext.getMchApp();
        if(mchApp == null || mchApp.getState() != CS.YES){
            throw new BizException("商户应用不存在或应用状态不可用");
        }

        if(!mchApp.getMchNo().equals(mchNo)){
            throw new BizException("参数appId与商户号不匹配");
        }

        // 验签
        String appSecret = mchApp.getAppSecret();

        // 转换为 JSON
        JSONObject bizReqJSON = (JSONObject)JSONObject.toJSON(bizRQ);
        bizReqJSON.remove("sign");
        if(!sign.equalsIgnoreCase(JeepayKit.getSign(bizReqJSON, appSecret))){
             throw new BizException("验签失败");
        }

        return bizRQ;
    }
}
