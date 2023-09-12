package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.constants.ApiCodeEnum;
import com.example.constants.CS;
import com.example.entity.MchApp;
import com.example.entity.MchPayPassage;
import com.example.entity.PayInterfaceConfig;
import com.example.entity.PayOrder;
import com.example.exception.BizException;
import com.example.service.mapper.MchAppMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 商户应用表 服务实现类
 *
 */
@Service
public class MchAppService extends ServiceImpl<MchAppMapper, MchApp> {

    @Resource
    private PayOrderService payOrderService;
    @Resource private MchPayPassageService mchPayPassageService;
    @Resource private PayInterfaceConfigService payInterfaceConfigService;

    @Transactional(rollbackFor = Exception.class)
    public void removeByAppId(String appId) {

        // 1.查看当前应用是否存在交易数据
        int payCount = payOrderService.count(PayOrder.gw().eq(PayOrder::getAppId, appId));
        if (payCount > 0) {
            throw new BizException("该应用已存在交易数据，不可删除");
        }

        // 2.删除应用关联的支付通道
        mchPayPassageService.remove(MchPayPassage.gw().eq(MchPayPassage::getAppId, appId));

        // 3.删除应用配置的支付参数
        payInterfaceConfigService.remove(PayInterfaceConfig.gw()
                .eq(PayInterfaceConfig::getInfoId, appId)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
        );

        // 4.删除当前应用
        if (!removeById(appId)) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
        }
    }

    public MchApp selectById(String appId) {
        MchApp mchApp = this.getById(appId);
        if (mchApp == null) {
            return null;
        }
        mchApp.setAppSecret(StringKit.str2Star(mchApp.getAppSecret(), 6, 6, 6));

        return mchApp;
    }

    public IPage<MchApp> selectPage(IPage iPage, MchApp mchApp) {

        LambdaQueryWrapper<MchApp> wrapper = MchApp.gw();
        if (StringUtils.isNotBlank(mchApp.getMchNo())) {
            wrapper.eq(MchApp::getMchNo, mchApp.getMchNo());
        }
        if (StringUtils.isNotEmpty(mchApp.getAppId())) {
            wrapper.eq(MchApp::getAppId, mchApp.getAppId());
        }
        if (StringUtils.isNotEmpty(mchApp.getAppName())) {
            wrapper.eq(MchApp::getAppName, mchApp.getAppName());
        }
        if (mchApp.getState() != null) {
            wrapper.eq(MchApp::getState, mchApp.getState());
        }
        wrapper.orderByDesc(MchApp::getCreatedAt);

        IPage<MchApp> pages = this.page(iPage, wrapper);

        pages.getRecords().stream().forEach(item -> item.setAppSecret(StringKit.str2Star(item.getAppSecret(), 6, 6, 6)));

        return pages;
    }

    public MchApp getOneByMch(String mchNo, String appId){
        return getOne(MchApp.gw().eq(MchApp::getMchNo, mchNo).eq(MchApp::getAppId, appId));
    }

}
