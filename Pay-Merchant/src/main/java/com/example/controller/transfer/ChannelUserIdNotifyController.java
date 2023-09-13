package com.example.controller.transfer;

import com.alibaba.fastjson.JSONObject;
import com.example.controller.CommonCtrl;
import com.example.websocket.server.WsChannelUserIdServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 获取用户ID - 回调函数
 */
@Api(tags = "商户转账")
@Controller
@RequestMapping("/api/anon/channelUserIdCallback")
public class ChannelUserIdNotifyController extends CommonCtrl {

    @ApiOperation("（转账）获取用户ID - 回调函数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "extParam", value = "扩展参数"),
            @ApiImplicitParam(name = "channelUserId", value = "用户userId"),
            @ApiImplicitParam(name = "appId", value = "应用ID")
    })
    @RequestMapping("")
    public String channelUserIdCallback() {

        try {
            //请求参数
            JSONObject params = getReqParamJSON();

            String extParam = params.getString("extParam");
            String channelUserId = params.getString("channelUserId");
            String appId = params.getString("appId");

            //推送到前端
            WsChannelUserIdServer.sendMsgByAppAndCid(appId, extParam, channelUserId);

        } catch (Exception e) {
            request.setAttribute("errMsg", e.getMessage());
        }

        return "channelUser/getChannelUserIdPage";
    }
}
