package com.example.controller.paytest;

import com.alibaba.fastjson.JSONObject;
import com.example.controller.CommonCtrl;
import com.example.entity.MchApp;
import com.example.service.impl.MchAppService;
import com.example.websocket.server.WsPayOrderServer;
import com.jeequan.jeepay.util.JeepayKit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

;

/**
 * 支付测试 - 回调函数
 */
@Api(tags = "支付测试")
@RestController
@RequestMapping("/api/anon/paytestNotify")
public class PaytestNotifyController extends CommonCtrl {

    @Resource
    private MchAppService mchAppService;

    @ApiOperation("支付回调信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "应用ID", required = true),
            @ApiImplicitParam(name = "mchNo", value = "商户号", required = true),
            @ApiImplicitParam(name = "sign", value = "签名值", required = true)
    })
    @RequestMapping("/payOrder")
    public void payOrderNotify() throws IOException {

        //请求参数
        JSONObject params = getReqParamJSON();

        String mchNo = params.getString("mchNo");
        String appId = params.getString("appId");
        String sign = params.getString("sign");
        MchApp mchApp = mchAppService.getById(appId);
        if (mchApp == null || !mchApp.getMchNo().equals(mchNo)) {
            response.getWriter().print("app is not exists");
            return;
        }

        params.remove("sign");
        if (!JeepayKit.getSign(params, mchApp.getAppSecret()).equalsIgnoreCase(sign)) {
            response.getWriter().print("sign fail");
            return;
        }

        JSONObject msg = new JSONObject();
        msg.put("state", params.getIntValue("state"));
        msg.put("errCode", params.getString("errCode"));
        msg.put("errMsg", params.getString("errMsg"));

        //推送到前端
        WsPayOrderServer.sendMsgByOrderId(params.getString("payOrderId"), msg.toJSONString());

        response.getWriter().print("SUCCESS");
    }

}
