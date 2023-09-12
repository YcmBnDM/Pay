package com.example.pay.service;

import com.example.constants.CS;
import com.example.entity.MchApp;
import com.example.entity.MchInfo;
import com.example.entity.PayInterfaceConfig;
import com.example.model.param.IsvParams;
import com.example.model.param.IsvsubMchParams;
import com.example.model.param.NormalMchParams;
import com.example.model.param.alipay.AlipayIsvParams;
import com.example.model.param.alipay.AlipayNormalMchParams;
import com.example.model.param.pppay.PpPayNormalMchParams;
import com.example.model.param.wxpay.WxpayIsvParams;
import com.example.model.param.wxpay.WxpayNormalMchParams;
import com.example.service.impl.MchAppService;
import com.example.service.impl.MchInfoService;
import com.example.service.impl.PayInterfaceConfigService;
import com.example.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 配置信息查询服务 （兼容 缓存 和 直接查询方式）
 */
@Slf4j
@Service
public class ConfigContextQueryService {

    @Resource
    ConfigContextService configContextService;
    @Resource
    private MchInfoService mchInfoService;
    @Resource
    private MchAppService mchAppService;
    @Resource
    private PayInterfaceConfigService payInterfaceConfigService;

    private boolean isCache() {
        return SysConfigService.IS_USE_CACHE;
    }

    public MchApp queryMchApp(String mchNo, String mchAppId) {

        if (isCache()) {
            return configContextService.getMchAppConfigContext(mchNo, mchAppId).getMchApp();
        }

        return mchAppService.getOneByMch(mchNo, mchAppId);
    }

    public MchAppConfigContext queryMchInfoAndAppInfo(String mchAppId) {
        return queryMchInfoAndAppInfo(mchAppService.getById(mchAppId).getMchNo(), mchAppId);
    }

    public MchAppConfigContext queryMchInfoAndAppInfo(String mchNo, String mchAppId) {

        if (isCache()) {
            return configContextService.getMchAppConfigContext(mchNo, mchAppId);
        }

        MchInfo mchInfo = mchInfoService.getById(mchNo);
        MchApp mchApp = queryMchApp(mchNo, mchAppId);

        if (mchInfo == null || mchApp == null) {
            return null;
        }

        MchAppConfigContext result = new MchAppConfigContext();
        result.setMchInfo(mchInfo);
        result.setMchNo(mchNo);
        result.setMchType(mchInfo.getType());

        result.setMchApp(mchApp);
        result.setAppId(mchAppId);

        return result;
    }


    public NormalMchParams queryNormalMchParams(String mchNo, String mchAppId, String ifCode) {

        if (isCache()) {
            return configContextService.getMchAppConfigContext(mchNo, mchAppId).getNormalMchParamsByIfCode(ifCode);
        }

        // 查询商户的所有支持的参数配置
        PayInterfaceConfig payInterfaceConfig = payInterfaceConfigService.getOne(PayInterfaceConfig.gw()
                .select(PayInterfaceConfig::getIfCode, PayInterfaceConfig::getIfParams)
                .eq(PayInterfaceConfig::getState, CS.YES)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
                .eq(PayInterfaceConfig::getInfoId, mchAppId)
                .eq(PayInterfaceConfig::getIfCode, ifCode)
        );

        if (payInterfaceConfig == null) {
            return null;
        }

        return NormalMchParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams());
    }


    public IsvsubMchParams queryIsvsubMchParams(String mchNo, String mchAppId, String ifCode) {

        if (isCache()) {
            return configContextService.getMchAppConfigContext(mchNo, mchAppId).getIsvsubMchParamsByIfCode(ifCode);
        }

        // 查询商户的所有支持的参数配置
        PayInterfaceConfig payInterfaceConfig = payInterfaceConfigService.getOne(PayInterfaceConfig.gw()
                .select(PayInterfaceConfig::getIfCode, PayInterfaceConfig::getIfParams)
                .eq(PayInterfaceConfig::getState, CS.YES)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
                .eq(PayInterfaceConfig::getInfoId, mchAppId)
                .eq(PayInterfaceConfig::getIfCode, ifCode)
        );

        if (payInterfaceConfig == null) {
            return null;
        }

        return IsvsubMchParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams());
    }


    public IsvParams queryIsvParams(String isvNo, String ifCode) {

        if (isCache()) {
            IsvConfigContext isvConfigContext = configContextService.getIsvConfigContext(isvNo);
            return isvConfigContext == null ? null : isvConfigContext.getIsvParamsByIfCode(ifCode);
        }

        // 查询商户的所有支持的参数配置
        PayInterfaceConfig payInterfaceConfig = payInterfaceConfigService.getOne(PayInterfaceConfig.gw()
                .select(PayInterfaceConfig::getIfCode, PayInterfaceConfig::getIfParams)
                .eq(PayInterfaceConfig::getState, CS.YES)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_ISV)
                .eq(PayInterfaceConfig::getInfoId, isvNo)
                .eq(PayInterfaceConfig::getIfCode, ifCode)
        );

        if (payInterfaceConfig == null) {
            return null;
        }

        return IsvParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams());

    }

    public AlipayClientWrapper getAlipayClientWrapper(MchAppConfigContext mchAppConfigContext) {

        if (isCache()) {
            return
                    configContextService.getMchAppConfigContext(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId()).getAlipayClientWrapper();
        }

        if (mchAppConfigContext.isIsvsubMch()) {

            AlipayIsvParams alipayParams = (AlipayIsvParams) queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), CS.IF_CODE.ALIPAY);
            return AlipayClientWrapper.buildAlipayClientWrapper(alipayParams);
        } else {

            AlipayNormalMchParams alipayParams = (AlipayNormalMchParams) queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.ALIPAY);
            return AlipayClientWrapper.buildAlipayClientWrapper(alipayParams);
        }

    }

    public WxServiceWrapper getWxServiceWrapper(MchAppConfigContext mchAppConfigContext) {

        if (isCache()) {
            return
                    configContextService.getMchAppConfigContext(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId()).getWxServiceWrapper();
        }

        if (mchAppConfigContext.isIsvsubMch()) {

            WxpayIsvParams wxParams = (WxpayIsvParams) queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), CS.IF_CODE.WXPAY);
            return WxServiceWrapper.buildWxServiceWrapper(wxParams);
        } else {

            WxpayNormalMchParams wxParams = (WxpayNormalMchParams) queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.WXPAY);
            return WxServiceWrapper.buildWxServiceWrapper(wxParams);
        }

    }

    public PaypalWrapper getPaypalWrapper(MchAppConfigContext mchAppConfigContext) {
        if (isCache()) {
            return
                    configContextService.getMchAppConfigContext(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId()).getPaypalWrapper();
        }
        PpPayNormalMchParams ppPayNormalMchParams = (PpPayNormalMchParams) queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.PPPAY);
        ;
        return PaypalWrapper.buildPaypalWrapper(ppPayNormalMchParams);

    }

}
