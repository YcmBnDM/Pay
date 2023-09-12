package com.example.pay.service;

import com.example.constants.CS;
import com.example.entity.IsvInfo;
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
import com.example.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 商户/服务商 配置信息上下文服务
 */
@Slf4j
@Service
public class ConfigContextService {

    /**
     * <商户ID, 商户配置项>
     **/
    private static final Map<String, MchInfoConfigContext> mchInfoConfigContextMap = new ConcurrentHashMap<>();

    /**
     * <应用ID, 商户配置上下文>
     **/
    private static final Map<String, MchAppConfigContext> mchAppConfigContextMap = new ConcurrentHashMap<>();

    /**
     * <服务商号, 服务商配置上下文>
     **/
    private static final Map<String, IsvConfigContext> isvConfigContextMap = new ConcurrentHashMap<>();

    @Resource
    private MchInfoService mchInfoService;
    @Resource
    private MchAppService mchAppService;
    @Resource
    private IsvInfoService isvInfoService;
    @Resource
    private PayInterfaceConfigService payInterfaceConfigService;


    /**
     * 获取 [商户配置信息]
     **/
    public MchInfoConfigContext getMchInfoConfigContext(String mchNo) {

        MchInfoConfigContext mchInfoConfigContext = mchInfoConfigContextMap.get(mchNo);

        //无此数据， 需要初始化
        if (mchInfoConfigContext == null) {
            initMchInfoConfigContext(mchNo);
        }

        return mchInfoConfigContextMap.get(mchNo);
    }

    /**
     * 获取 [商户应用支付参数配置信息]
     **/
    public MchAppConfigContext getMchAppConfigContext(String mchNo, String appId) {

        MchAppConfigContext mchAppConfigContext = mchAppConfigContextMap.get(appId);

        //无此数据， 需要初始化
        if (mchAppConfigContext == null) {
            initMchAppConfigContext(mchNo, appId);
        }

        return mchAppConfigContextMap.get(appId);
    }

    /**
     * 获取 [ISV支付参数配置信息]
     **/
    public IsvConfigContext getIsvConfigContext(String isvNo) {

        IsvConfigContext isvConfigContext = isvConfigContextMap.get(isvNo);

        //无此数据， 需要初始化
        if (isvConfigContext == null) {
            initIsvConfigContext(isvNo);
        }

        return isvConfigContextMap.get(isvNo);
    }


    /**
     * 初始化 [商户配置信息]
     **/
    public synchronized void initMchInfoConfigContext(String mchNo) {

        if (!isCache()) { // 当前系统不进行缓存
            return;
        }

        //商户主体信息
        MchInfo mchInfo = mchInfoService.getById(mchNo);
        if (mchInfo == null) { // 查询不到商户主体， 可能已经删除

            MchInfoConfigContext mchInfoConfigContext = mchInfoConfigContextMap.get(mchNo);

            // 删除所有的商户应用
            if (mchInfoConfigContext != null) {
                mchInfoConfigContext.getAppMap().forEach((k, v) -> mchAppConfigContextMap.remove(k));
            }

            mchInfoConfigContextMap.remove(mchNo);
            return;
        }

        MchInfoConfigContext mchInfoConfigContext = new MchInfoConfigContext();

        // 设置商户信息
        mchInfoConfigContext.setMchNo(mchInfo.getMchNo());
        mchInfoConfigContext.setMchType(mchInfo.getType());
        mchInfoConfigContext.setMchInfo(mchInfo);
        mchAppService.list(MchApp.gw().eq(MchApp::getMchNo, mchNo)).stream().forEach(mchApp -> {

            //1. 更新商户内appId集合
            mchInfoConfigContext.putMchApp(mchApp);

            MchAppConfigContext mchAppConfigContext = mchAppConfigContextMap.get(mchApp.getAppId());
            if (mchAppConfigContext != null) {
                mchAppConfigContext.setMchApp(mchApp);
                mchAppConfigContext.setMchNo(mchInfo.getMchNo());
                mchAppConfigContext.setMchType(mchInfo.getType());
                mchAppConfigContext.setMchInfo(mchInfo);
            }
        });

        mchInfoConfigContextMap.put(mchNo, mchInfoConfigContext);
    }

    /**
     * 初始化 [商户应用支付参数配置信息]
     **/
    public synchronized void initMchAppConfigContext(String mchNo, String appId) {

        if (!isCache()) { // 当前系统不进行缓存
            return;
        }

        // 获取商户的配置信息
        MchInfoConfigContext mchInfoConfigContext = getMchInfoConfigContext(mchNo);
        if (mchInfoConfigContext == null) { // 商户信息不存在
            return;
        }

        // 查询商户应用信息主体
        MchApp dbMchApp = mchAppService.getById(appId);

        //DB已经删除
        if (dbMchApp == null) {
            mchAppConfigContextMap.remove(appId);  //清除缓存信息
            mchInfoConfigContext.getAppMap().remove(appId); //清除主体信息中的appId
            return;
        }


        // 商户应用mchNo 与参数不匹配
        if (!dbMchApp.getMchNo().equals(mchNo)) {
            return;
        }

        //更新商户信息主体中的商户应用
        mchInfoConfigContext.putMchApp(dbMchApp);

        //商户主体信息
        MchInfo mchInfo = mchInfoConfigContext.getMchInfo();
        MchAppConfigContext mchAppConfigContext = new MchAppConfigContext();

        // 设置商户信息
        mchAppConfigContext.setAppId(appId);
        mchAppConfigContext.setMchNo(mchInfo.getMchNo());
        mchAppConfigContext.setMchType(mchInfo.getType());
        mchAppConfigContext.setMchInfo(mchInfo);
        mchAppConfigContext.setMchApp(dbMchApp);

        // 查询商户的所有支持的参数配置
        List<PayInterfaceConfig> allConfigList = payInterfaceConfigService.list(PayInterfaceConfig.gw()
                .select(PayInterfaceConfig::getIfCode, PayInterfaceConfig::getIfParams)
                .eq(PayInterfaceConfig::getState, CS.YES)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
                .eq(PayInterfaceConfig::getInfoId, appId)
        );

        // 普通商户
        if (mchInfo.getType() == CS.MCH_TYPE_NORMAL) {

            for (PayInterfaceConfig payInterfaceConfig : allConfigList) {
                mchAppConfigContext.getNormalMchParamsMap().put(
                        payInterfaceConfig.getIfCode(),
                        NormalMchParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams())
                );
            }

            //放置alipay client

            AlipayNormalMchParams alipayParams = mchAppConfigContext.getNormalMchParamsByIfCode(CS.IF_CODE.ALIPAY, AlipayNormalMchParams.class);
            if (alipayParams != null) {
                mchAppConfigContext.setAlipayClientWrapper(AlipayClientWrapper.buildAlipayClientWrapper(alipayParams));
            }

            //放置 wxJavaService
            WxpayNormalMchParams wxpayParams = mchAppConfigContext.getNormalMchParamsByIfCode(CS.IF_CODE.WXPAY, WxpayNormalMchParams.class);
            if (wxpayParams != null) {
                mchAppConfigContext.setWxServiceWrapper(WxServiceWrapper.buildWxServiceWrapper(wxpayParams));
            }

            //放置 paypal client
            PpPayNormalMchParams ppPayMchParams = mchAppConfigContext.getNormalMchParamsByIfCode(CS.IF_CODE.PPPAY, PpPayNormalMchParams.class);
            if (ppPayMchParams != null) {
                mchAppConfigContext.setPaypalWrapper(PaypalWrapper.buildPaypalWrapper(ppPayMchParams));
            }


        } else { //服务商模式商户
            for (PayInterfaceConfig payInterfaceConfig : allConfigList) {
                mchAppConfigContext.getIsvsubMchParamsMap().put(
                        payInterfaceConfig.getIfCode(),
                        IsvsubMchParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams())
                );
            }

            //放置 当前商户的 服务商信息
            mchAppConfigContext.setIsvConfigContext(getIsvConfigContext(mchInfo.getIsvNo()));

        }

        mchAppConfigContextMap.put(appId, mchAppConfigContext);
    }


    /**
     * 初始化 [ISV支付参数配置信息]
     **/
    public synchronized void initIsvConfigContext(String isvNo) {

        if (!isCache()) { // 当前系统不进行缓存
            return;
        }

        //查询出所有商户的配置信息并更新
        List<String> mchNoList = new ArrayList<>();
        mchInfoService.list(MchInfo.gw().select(MchInfo::getMchNo).eq(MchInfo::getIsvNo, isvNo)).forEach(r -> mchNoList.add(r.getMchNo()));

        // 查询出所有 所属当前服务商的所有应用集合
        List<String> mchAppIdList = new ArrayList<>();
        if (!mchNoList.isEmpty()) {
            mchAppService.list(MchApp.gw().select(MchApp::getAppId).in(MchApp::getMchNo, mchNoList)).forEach(r -> mchAppIdList.add(r.getAppId()));
        }

        IsvConfigContext isvConfigContext = new IsvConfigContext();
        IsvInfo isvInfo = isvInfoService.getById(isvNo);
        if (isvInfo == null) {

            for (String appId : mchAppIdList) {
                //将更新已存在缓存的商户配置信息 （每个商户下存储的为同一个 服务商配置的对象指针）
                MchAppConfigContext mchAppConfigContext = mchAppConfigContextMap.get(appId);
                if (mchAppConfigContext != null) {
                    mchAppConfigContext.setIsvConfigContext(null);
                }
            }

            isvConfigContextMap.remove(isvNo); // 服务商有商户不可删除， 此处不再更新商户下的配置信息
            return;
        }

        // 设置商户信息
        isvConfigContext.setIsvNo(isvInfo.getIsvNo());
        isvConfigContext.setIsvInfo(isvInfo);

        // 查询商户的所有支持的参数配置
        List<PayInterfaceConfig> allConfigList = payInterfaceConfigService.list(PayInterfaceConfig.gw()
                .select(PayInterfaceConfig::getIfCode, PayInterfaceConfig::getIfParams)
                .eq(PayInterfaceConfig::getState, CS.YES)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_ISV)
                .eq(PayInterfaceConfig::getInfoId, isvNo)
        );

        for (PayInterfaceConfig payInterfaceConfig : allConfigList) {
            isvConfigContext.getIsvParamsMap().put(
                    payInterfaceConfig.getIfCode(),
                    IsvParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams())
            );
        }

        //放置alipay client
        AlipayIsvParams alipayParams = isvConfigContext.getIsvParamsByIfCode(CS.IF_CODE.ALIPAY, AlipayIsvParams.class);
        if (alipayParams != null) {
            isvConfigContext.setAlipayClientWrapper(AlipayClientWrapper.buildAlipayClientWrapper(alipayParams));
        }

        //放置 wxJavaService
        WxpayIsvParams wxpayParams = isvConfigContext.getIsvParamsByIfCode(CS.IF_CODE.WXPAY, WxpayIsvParams.class);
        if (wxpayParams != null) {
            isvConfigContext.setWxServiceWrapper(WxServiceWrapper.buildWxServiceWrapper(wxpayParams));
        }

        isvConfigContextMap.put(isvNo, isvConfigContext);

        //查询出所有商户的配置信息并更新
        for (String appId : mchAppIdList) {
            //将更新已存在缓存的商户配置信息 （每个商户下存储的为同一个 服务商配置的对象指针）
            MchAppConfigContext mchAppConfigContext = mchAppConfigContextMap.get(appId);
            if (mchAppConfigContext != null) {
                mchAppConfigContext.setIsvConfigContext(isvConfigContext);
            }
        }
    }

    private boolean isCache() {
        return SysConfigService.IS_USE_CACHE;
    }

}
