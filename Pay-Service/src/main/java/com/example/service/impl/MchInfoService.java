package com.example.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.constants.ApiCodeEnum;
import com.example.constants.CS;
import com.example.entity.*;
import com.example.exception.BizException;
import com.example.service.mapper.MchInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * 商户信息表 服务实现类
 */
@Service
public class MchInfoService extends ServiceImpl<MchInfoMapper, MchInfo> {

    @Resource
    private SysUserService sysUserService;

    @Resource
    private PayOrderService payOrderService;

    @Resource
    private MchPayPassageService mchPayPassageService;

    @Resource
    private PayInterfaceConfigService payInterfaceConfigService;

    @Resource
    private SysUserAuthService sysUserAuthService;

    @Resource
    private IsvInfoService isvInfoService;

    @Resource
    private MchAppService mchAppService;

    @Transactional(rollbackFor = Exception.class)
    public void addMch(MchInfo mchInfo, String loginUserName) {

        // 校验特邀商户信息
        if (mchInfo.getType() == CS.MCH_TYPE_ISVSUB && StringUtils.isNotEmpty(mchInfo.getIsvNo())) {
            // 当前服务商状态是否正确
            IsvInfo isvInfo = isvInfoService.getById(mchInfo.getIsvNo());
            if (isvInfo == null || isvInfo.getState() == CS.NO) {
                throw new BizException("当前服务商不可用");
            }
        }

        // 插入商户基本信息
        boolean saveResult = save(mchInfo);
        if (!saveResult) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }

        // 插入用户信息
        SysUser sysUser = new SysUser();
        sysUser.setLoginUsername(loginUserName);
        sysUser.setRealname(mchInfo.getContactName());
        sysUser.setTelphone(mchInfo.getContactTel());
        sysUser.setUserNo(mchInfo.getMchNo());
        sysUser.setBelongInfoId(mchInfo.getMchNo());
        sysUser.setSex(CS.SEX_MALE);
        sysUser.setIsAdmin(CS.YES);
        sysUser.setState(mchInfo.getState());
        sysUserService.addSysUser(sysUser, CS.SYS_TYPE.MCH);

        // 插入商户默认应用
        MchApp mchApp = new MchApp();
        mchApp.setAppId(IdUtil.objectId());
        mchApp.setMchNo(mchInfo.getMchNo());
        mchApp.setAppName("默认应用");
        mchApp.setAppSecret(RandomUtil.randomString(128));
        mchApp.setState(CS.YES);
        mchApp.setCreatedBy(sysUser.getRealname());
        mchApp.setCreatedUid(sysUser.getSysUserId());
        saveResult = mchAppService.save(mchApp);
        if (!saveResult) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }

        // 存入商户默认用户ID
        MchInfo updateRecord = new MchInfo();
        updateRecord.setMchNo(mchInfo.getMchNo());
        updateRecord.setInitUserId(sysUser.getSysUserId());
        saveResult = updateById(updateRecord);
        if (!saveResult) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }

    }

    /**
     * 删除商户
     **/
    @Transactional(rollbackFor = Exception.class)
    public List<Long> removeByMchNo(String mchNo) {
        try {
            // 0.当前商户是否存在
            MchInfo mchInfo = getById(mchNo);
            if (mchInfo == null) {
                throw new BizException("该商户不存在");
            }

            // 1.查看当前商户是否存在交易数据
            int payCount = (int) payOrderService.count(PayOrder.gw()
                    .eq(PayOrder::getMchNo, mchNo));
            if (payCount > 0) {
                throw new BizException("该商户已存在交易数据，不可删除");
            }

            // 2.删除当前商户配置的支付通道
            mchPayPassageService.remove(MchPayPassage.gw().eq(MchPayPassage::getMchNo, mchNo));

            // 3.删除当前商户支付接口配置参数
            List<String> appIdList = new LinkedList<>();
            mchAppService.list(MchApp.gw().eq(MchApp::getMchNo, mchNo)).forEach(item -> appIdList.add(item.getAppId()));
            if (CollectionUtils.isNotEmpty(appIdList)) {
                payInterfaceConfigService.remove(PayInterfaceConfig.gw()
                        .in(PayInterfaceConfig::getInfoId, appIdList)
                        .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
                );
            }

            List<SysUser> userList = sysUserService.list(SysUser.gw()
                    .eq(SysUser::getBelongInfoId, mchNo)
                    .eq(SysUser::getSysType, CS.SYS_TYPE.MCH)
            );

            // 4.删除当前商户应用信息
            if (CollectionUtils.isNotEmpty(appIdList)) {
                mchAppService.removeByIds(appIdList);
            }

            // 返回的用户id
            List<Long> userIdList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(userList)) {
                for (SysUser user : userList) {
                    userIdList.add(user.getSysUserId());
                }
                // 5.删除当前商户用户子用户信息
                sysUserAuthService.remove(SysUserAuth.gw().in(SysUserAuth::getUserId, userIdList));
            }

            // 6.删除当前商户的登录用户
            sysUserService.remove(SysUser.gw()
                    .eq(SysUser::getBelongInfoId, mchNo)
                    .eq(SysUser::getSysType, CS.SYS_TYPE.MCH)
            );

            // 7.删除当前商户
            boolean removeMchInfo = removeById(mchNo);
            if (!removeMchInfo) {
                throw new BizException("删除当前商户失败");
            }
            return userIdList;
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }
}
