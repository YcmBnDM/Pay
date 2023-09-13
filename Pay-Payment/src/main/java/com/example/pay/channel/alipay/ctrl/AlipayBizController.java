package com.example.pay.channel.alipay.ctrl;

import cn.hutool.core.util.URLUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.domain.AlipayOpenAuthTokenAppModel;
import com.alipay.api.request.AlipayOpenAuthTokenAppRequest;
import com.alipay.api.response.AlipayOpenAuthTokenAppResponse;
import com.example.components.mq.model.extender.ResetIsvMchAppInfoConfigMQ;
import com.example.components.mq.vender.IMQSender;
import com.example.constants.CS;
import com.example.ctrl.AbstractCtrl;
import com.example.entity.MchApp;
import com.example.entity.PayInterfaceConfig;
import com.example.exception.BizException;
import com.example.model.param.alipay.AlipayConfig;
import com.example.model.param.alipay.AlipayIsvParams;
import com.example.pay.channel.alipay.AlipayKit;
import com.example.pay.model.AlipayClientWrapper;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.service.ConfigContextQueryService;
import com.example.service.impl.MchAppService;
import com.example.service.impl.PayInterfaceConfigService;
import com.example.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;


/**
* 渠道侧自定义业务ctrl
*
*/
@Slf4j
@Controller
@RequestMapping("/api/channelbiz/alipay")
public class AlipayBizController extends AbstractCtrl {

    @Resource
    private ConfigContextQueryService configContextQueryService;
    @Resource private SysConfigService sysConfigService;
    @Resource private PayInterfaceConfigService payInterfaceConfigService;
    @Resource private MchAppService mchAppService;
    @Resource private IMQSender mqSender;


    /** 跳转到支付宝的授权页面 （统一从pay项目获取到isv配置信息）
     * isvAndMchNo 格式:  ISVNO_MCHAPPID
     * example: https://pay.jeepay.cn/api/channelbiz/alipay/redirectAppToAppAuth/V1623998765_60cc41694ee0e6685f57eb1f
     * **/
    @RequestMapping("/redirectAppToAppAuth/{isvAndMchAppId}")
    public void redirectAppToAppAuth(@PathVariable("isvAndMchAppId") String isvAndMchAppId) throws IOException {

        String isvNo = isvAndMchAppId.split("_")[0];

        AlipayIsvParams alipayIsvParams = (AlipayIsvParams) configContextQueryService.queryIsvParams(isvNo, CS.IF_CODE.ALIPAY);
        alipayIsvParams.getSandbox();

        String oauthUrl = AlipayConfig.PROD_APP_TO_APP_AUTH_URL;
        if(alipayIsvParams.getSandbox() != null && alipayIsvParams.getSandbox() == CS.YES){
            oauthUrl = AlipayConfig.SANDBOX_APP_TO_APP_AUTH_URL;
        }

        String redirectUrl = sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/channelbiz/alipay/appToAppAuthCallback";
        response.sendRedirect(String.format(oauthUrl, alipayIsvParams.getAppId(), URLUtil.encodeAll(redirectUrl), isvAndMchAppId));
    }

    /** 支付宝授权回调地址 **/
    @RequestMapping("/appToAppAuthCallback")
    public String appToAppAuthCallback() {

        String errMsg = null;
        boolean isAlipaySysAuth = true; //是否 服务商登录支付宝后台系统发起的商户授权， 此时无法获取authCode和商户的信息。

        try {
            // isvAndMchAppId 格式:  ISVNO_MCHAPPID,  如果isvAndMchNo为空说明是： 支付宝后台的二维码授权之后的跳转链接。
            String isvAndMchAppId = getValString("state");
            String appAuthCode = getValString("app_auth_code"); // 支付宝授权code

            if(StringUtils.isNotEmpty(isvAndMchAppId) && StringUtils.isNotEmpty(appAuthCode)){
                isAlipaySysAuth = false;
                String isvNo = isvAndMchAppId.split("_")[0];
                String mchAppId = isvAndMchAppId.split("_")[1];

                MchApp mchApp = mchAppService.getById(mchAppId);

                MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(mchApp.getMchNo(), mchAppId);
                AlipayClientWrapper alipayClientWrapper = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext);

                AlipayOpenAuthTokenAppRequest request = new AlipayOpenAuthTokenAppRequest();
                AlipayOpenAuthTokenAppModel model = new AlipayOpenAuthTokenAppModel();
                model.setGrantType("authorization_code");
                model.setCode(appAuthCode);
                request.setBizModel(model);

                // expiresIn: 该字段已作废，应用令牌长期有效，接入方不需要消费该字段
                // reExpiresIn: 刷新令牌的有效时间（从接口调用时间作为起始时间），单位到秒
                // DateUtil.offsetSecond(new Date(), Integer.parseInt(resp.getExpiresIn()));
                AlipayOpenAuthTokenAppResponse resp = alipayClientWrapper.execute(request);
                if(!resp.isSuccess()){
                    throw new BizException(AlipayKit.appendErrMsg(resp.getMsg(), resp.getSubMsg()));
                }
                String appAuthToken = resp.getAppAuthToken();
                JSONObject ifParams = new JSONObject();
                ifParams.put("appAuthToken", appAuthToken); ifParams.put("refreshToken", resp.getAppRefreshToken()); ifParams.put("expireTimestamp", resp.getExpiresIn());

                PayInterfaceConfig dbRecord = payInterfaceConfigService.getByInfoIdAndIfCode(CS.INFO_TYPE_MCH_APP, mchAppId, CS.IF_CODE.ALIPAY);

                if(dbRecord != null){
                    PayInterfaceConfig updateRecord = new PayInterfaceConfig();
                    updateRecord.setId(dbRecord.getId()); updateRecord.setIfParams(ifParams.toJSONString());
                    payInterfaceConfigService.updateById(updateRecord);
                }else{

                    dbRecord = new PayInterfaceConfig();
                    dbRecord.setInfoType(CS.INFO_TYPE_MCH_APP);
                    dbRecord.setInfoId(mchAppId);
                    dbRecord.setIfCode(CS.IF_CODE.ALIPAY);
                    dbRecord.setIfParams(ifParams.toJSONString());
                    dbRecord.setIfRate(new BigDecimal("0.006")); //默认费率
                    dbRecord.setState(CS.YES);
                    dbRecord.setCreatedBy("SYS");
                    dbRecord.setCreatedUid(0L);
                    payInterfaceConfigService.save(dbRecord);
                }

                // 更新应用配置信息
                mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_APP, null, mchApp.getMchNo(), mchApp.getAppId()));

            }
        } catch (Exception e) {
            log.error("error", e);
            errMsg = StringUtils.defaultIfBlank(e.getMessage(), "系统异常！");
        }

        request.setAttribute("errMsg", errMsg);
        request.setAttribute("isAlipaySysAuth", isAlipaySysAuth);
        return "channel/alipay/isvsubMchAuth";
    }

    /**
     * 接收  支付宝 应用 配置中： 【应用网关地址 ： 用于接收支付宝异步通知消息（例如 From蚂蚁消息等），需要传入http(s)公网可访问的网页地址。选填，若不设置，则无法接收相应的异步通知消息。】
     *
     * **/
    @RequestMapping("/appGatewayMsgReceive")
    public ModelAndView alipayAppGatewayMsgReceive() {

        JSONObject reqJSON = getReqParamJSON();

        // 获取到报文信息， 然后 转发到对应的ctrl
        log.error("支付宝应用网关接收消息参数：{}", reqJSON);

        // 分账交易通知
        if("alipay.trade.order.settle.notify".equals(reqJSON.getString("msg_method"))){

            // 直接转发到 分账通知的 URL去。
            ModelAndView mv = new ModelAndView();
            mv.setViewName("forward:/api/divisionRecordChannelNotify/" + CS.IF_CODE.ALIPAY);
            return mv;
        }

        throw new BizException("无此事件["+ reqJSON.getString("msg_method") +"]处理器");

    }

}
