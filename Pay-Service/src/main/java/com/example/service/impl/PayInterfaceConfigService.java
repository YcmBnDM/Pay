package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.constants.ApiCodeEnum;
import com.example.constants.CS;
import com.example.entity.MchApp;
import com.example.entity.MchInfo;
import com.example.entity.PayInterfaceConfig;
import com.example.entity.PayInterfaceDefine;
import com.example.exception.BizException;
import com.example.service.mapper.PayInterfaceConfigMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 支付接口配置参数表 服务实现类
 */
@Service
public class PayInterfaceConfigService extends ServiceImpl<PayInterfaceConfigMapper, PayInterfaceConfig> {

    @Resource
    private PayInterfaceDefineService payInterfaceDefineService;

    @Resource
    private MchInfoService mchInfoService;

    @Resource
    private MchAppService mchAppService;

    /**
     *  根据 账户类型、账户号、接口类型 获取支付参数配置
     */
    public PayInterfaceConfig getByInfoIdAndIfCode(Byte infoType, String infoId, String ifCode) {
        return getOne(PayInterfaceConfig.gw()
                .eq(PayInterfaceConfig::getInfoType, infoType)
                .eq(PayInterfaceConfig::getInfoId, infoId)
                .eq(PayInterfaceConfig::getIfCode, ifCode));
    }

    /**
     *  根据 账户类型、账户号 获取支付参数配置列表
     */
    public List<PayInterfaceDefine> selectAllPayIfConfigListByIsvNo(Byte infoType, String infoId) {

        // 支付定义列表
        LambdaQueryWrapper<PayInterfaceDefine> queryWrapper = PayInterfaceDefine.gw();
        queryWrapper.eq(PayInterfaceDefine::getState, CS.YES);
        queryWrapper.eq(PayInterfaceDefine::getIsIsvMode, CS.YES); // 支持服务商模式

        List<PayInterfaceDefine> defineList = payInterfaceDefineService.list(queryWrapper);

        // 支付参数列表
        LambdaQueryWrapper<PayInterfaceConfig> wrapper = PayInterfaceConfig.gw();
        wrapper.eq(PayInterfaceConfig::getInfoId, infoId);
        wrapper.eq(PayInterfaceConfig::getInfoType, infoType);
        List<PayInterfaceConfig> configList = this.list(wrapper);

        for (PayInterfaceDefine define : defineList) {
            for (PayInterfaceConfig config : configList) {
                if (define.getIfCode().equals(config.getIfCode())) {
                    define.addExt("ifConfigState", config.getState()); // 配置状态
                }
            }
        }
        return defineList;
    }


    public List<PayInterfaceDefine> selectAllPayIfConfigListByAppId(String appId) {

        MchApp mchApp = mchAppService.getById(appId);
        if (mchApp == null || mchApp.getState() != CS.YES) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        MchInfo mchInfo = mchInfoService.getById(mchApp.getMchNo());
        if (mchInfo == null || mchInfo.getState() != CS.YES) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        // 支付定义列表
        LambdaQueryWrapper<PayInterfaceDefine> queryWrapper = PayInterfaceDefine.gw();
        queryWrapper.eq(PayInterfaceDefine::getState, CS.YES);

        Map<String, PayInterfaceConfig> isvPayConfigMap = new HashMap<>(); // 服务商支付参数配置集合

        // 根据商户类型，添加接口是否支持该商户类型条件
        if (mchInfo.getType() == CS.MCH_TYPE_NORMAL) {
            queryWrapper.eq(PayInterfaceDefine::getIsMchMode, CS.YES); // 支持普通商户模式
        }
        if (mchInfo.getType() == CS.MCH_TYPE_ISVSUB) {
            queryWrapper.eq(PayInterfaceDefine::getIsIsvMode, CS.YES); // 支持服务商模式
            // 商户类型为特约商户，服务商应已经配置支付参数
            List<PayInterfaceConfig> isvConfigList = this.list(PayInterfaceConfig.gw().eq(PayInterfaceConfig::getInfoId, mchInfo.getIsvNo()).eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_ISV).eq(PayInterfaceConfig::getState, CS.YES).ne(PayInterfaceConfig::getIfParams, "").isNotNull(PayInterfaceConfig::getIfParams));

            for (PayInterfaceConfig config : isvConfigList) {
                config.addExt("mchType", mchInfo.getType());
                isvPayConfigMap.put(config.getIfCode(), config);
            }
        }

        List<PayInterfaceDefine> defineList = payInterfaceDefineService.list(queryWrapper);

        // 支付参数列表
        LambdaQueryWrapper<PayInterfaceConfig> wrapper = PayInterfaceConfig.gw();
        wrapper.eq(PayInterfaceConfig::getInfoId, appId);
        wrapper.eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP);
        List<PayInterfaceConfig> configList = this.list(wrapper);

        for (PayInterfaceDefine define : defineList) {
            define.addExt("mchType", mchInfo.getType()); // 所属商户类型

            for (PayInterfaceConfig config : configList) {
                if (define.getIfCode().equals(config.getIfCode())) {
                    define.addExt("ifConfigState", config.getState()); // 配置状态
                }
            }

            if (mchInfo.getType() == CS.MCH_TYPE_ISVSUB && isvPayConfigMap.get(define.getIfCode()) == null) {
                define.addExt("subMchIsvConfig", CS.NO); // 特约商户，服务商支付参数的配置状态，0表示未配置
            }
        }
        return defineList;
    }


    /**
     * 查询商户app使用已正确配置了通道信息
     */
    public boolean mchAppHasAvailableIfCode(String appId, String ifCode) {

        return this.count(PayInterfaceConfig.gw().eq(PayInterfaceConfig::getIfCode, ifCode).eq(PayInterfaceConfig::getState, CS.PUB_USABLE).eq(PayInterfaceConfig::getInfoId, appId).eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)) > 0;

    }


}
