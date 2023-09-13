package com.example.controller.transfer;

import com.alibaba.fastjson.JSONObject;
import com.example.constants.CS;
import com.example.controller.CommonCtrl;
import com.example.entity.MchApp;
import com.example.entity.PayInterfaceConfig;
import com.example.entity.PayInterfaceDefine;
import com.example.exception.BizException;
import com.example.model.ApiRes;
import com.example.model.DBApplicationConfig;
import com.example.service.impl.MchAppService;
import com.example.service.impl.PayInterfaceConfigService;
import com.example.service.impl.PayInterfaceDefineService;
import com.example.service.impl.SysConfigService;
import com.example.util.StringUtil;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.TransferOrderCreateReqModel;
import com.jeequan.jeepay.request.TransferOrderCreateRequest;
import com.jeequan.jeepay.response.TransferOrderCreateResponse;
import com.jeequan.jeepay.util.JeepayKit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

;

/**
 * 转账api
 */
@Api(tags = "商户转账")
@RestController
@RequestMapping("/api/mchTransfers")
public class MchTransferController extends CommonCtrl {

    @Resource
    private MchAppService mchAppService;
    @Resource
    private PayInterfaceConfigService payInterfaceConfigService;
    @Resource
    private PayInterfaceDefineService payInterfaceDefineService;
    @Resource
    private SysConfigService sysConfigService;

    /**
     * 查询商户对应应用下支持的支付通道
     **/
    @ApiOperation("查询商户对应应用下支持的支付通道")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "appId", value = "应用ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_TRANSFER_IF_CODE_LIST')")
    @GetMapping("/ifCodes/{appId}")
    public ApiRes<List> ifCodeList(@PathVariable("appId") String appId) {

        List<String> ifCodeList = new ArrayList<>();
        payInterfaceConfigService.list(
                PayInterfaceConfig.gw().select(PayInterfaceConfig::getIfCode)
                        .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
                        .eq(PayInterfaceConfig::getInfoId, appId)
                        .eq(PayInterfaceConfig::getState, CS.PUB_USABLE)
        ).stream().forEach(r -> ifCodeList.add(r.getIfCode()));

        if (ifCodeList.isEmpty()) {
            return ApiRes.ok(ifCodeList);
        }

        List<PayInterfaceDefine> result = payInterfaceDefineService.list(PayInterfaceDefine.gw().in(PayInterfaceDefine::getIfCode, ifCodeList));
        return ApiRes.ok(result);
    }


    /**
     * 获取渠道侧用户ID
     **/
    @ApiOperation("获取渠道侧用户ID")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "appId", value = "应用ID", required = true),
            @ApiImplicitParam(name = "ifCode", value = "接口类型代码", required = true),
            @ApiImplicitParam(name = "extParam", value = "扩展参数", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_TRANSFER_CHANNEL_USER')")
    @GetMapping("/channelUserId")
    public ApiRes channelUserId() {

        String appId = getValStringRequired("appId");
        MchApp mchApp = mchAppService.getById(appId);
        if (mchApp == null || mchApp.getState() != CS.PUB_USABLE || !mchApp.getMchNo().equals(getCurrentMchNo())) {
            throw new BizException("商户应用不存在或不可用");
        }

        JSONObject param = getReqParamJSON();
        param.put("mchNo", getCurrentMchNo());
        param.put("appId", appId);
        param.put("ifCode", getValStringRequired("ifCode"));
        param.put("extParam", getValStringRequired("extParam"));
        param.put("reqTime", System.currentTimeMillis() + "");
        param.put("version", "1.0");
        param.put("signType", "MD5");

        DBApplicationConfig dbApplicationConfig = sysConfigService.getDBApplicationConfig();

        param.put("redirectUrl", dbApplicationConfig.getMchSiteUrl() + "/api/anon/channelUserIdCallback");

        param.put("sign", JeepayKit.getSign(param, mchApp.getAppSecret()));
        String url = StringUtil.appendUrlQuery(dbApplicationConfig.getPaySiteUrl() + "/api/channelUserId/jump", param);

        return ApiRes.ok(url);
    }


    /**
     * 调起下单接口
     **/
    @ApiOperation("调起转账接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "mchOrderNo", value = "商户订单号", required = true),
            @ApiImplicitParam(name = "entryType", value = "入账方式： WX_CASH-微信零钱; ALIPAY_CASH-支付宝转账; BANK_CARD-银行卡", required = true),
            @ApiImplicitParam(name = "ifCode", value = "接口类型代码", required = true),
            @ApiImplicitParam(name = "amount", value = "转账金额,单位元", required = true),
            @ApiImplicitParam(name = "accountNo", value = "收款账号", required = true),
            @ApiImplicitParam(name = "accountName", value = "收款人姓名"),
            @ApiImplicitParam(name = "bankName", value = "收款人开户行名称"),
            @ApiImplicitParam(name = "clientIp", value = "客户端IP"),
            @ApiImplicitParam(name = "transferDesc", value = "转账备注信息"),
            @ApiImplicitParam(name = "notifyUrl", value = "通知地址"),
            @ApiImplicitParam(name = "channelExtra", value = "特定渠道发起时额外参数"),
            @ApiImplicitParam(name = "extParam", value = "扩展参数")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_TEST_DO')")
    @PostMapping("/doTransfer")
    public ApiRes doTransfer() {

        handleParamAmount("amount");
        TransferOrderCreateReqModel model = getObject(TransferOrderCreateReqModel.class);

        MchApp mchApp = mchAppService.getById(model.getAppId());
        if (mchApp == null || mchApp.getState() != CS.PUB_USABLE || !mchApp.getMchNo().equals(getCurrentMchNo())) {
            throw new BizException("商户应用不存在或不可用");
        }

        TransferOrderCreateRequest request = new TransferOrderCreateRequest();
        model.setMchNo(this.getCurrentMchNo());
        model.setAppId(mchApp.getAppId());
        model.setCurrency("CNY");
        request.setBizModel(model);

        JeepayClient jeepayClient = new JeepayClient(sysConfigService.getDBApplicationConfig().getPaySiteUrl(), mchApp.getAppSecret());

        try {
            TransferOrderCreateResponse response = jeepayClient.execute(request);
            if (response.getCode() != 0) {
                throw new BizException(response.getMsg());
            }
            return ApiRes.ok(response.get());
        } catch (JeepayException e) {
            throw new BizException(e.getMessage());
        }
    }

}
