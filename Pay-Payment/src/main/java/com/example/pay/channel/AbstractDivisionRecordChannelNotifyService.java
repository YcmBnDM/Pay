
package com.example.pay.channel;

import com.alibaba.fastjson.JSONObject;
import com.example.bean.RequestKitBean;
import com.example.entity.PayOrderDivisionRecord;
import com.example.pay.model.MchAppConfigContext;
import com.example.pay.rqrs.msg.DivisionChannelNotifyModel;
import com.example.pay.service.ConfigContextQueryService;
import com.example.pay.util.ChannelCertConfigKitBean;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

;

/*
* 分账结果回调接口抽象类
*
*/
public abstract class AbstractDivisionRecordChannelNotifyService {

    @Resource
    private RequestKitBean requestKitBean;
    @Resource private ChannelCertConfigKitBean channelCertConfigKitBean;
    @Resource protected ConfigContextQueryService configContextQueryService;


    /** 获取到接口code **/
    public abstract String getIfCode();

    /** 解析参数： 批次号 和 请求参数
     *  异常需要自行捕捉，并返回null , 表示已响应数据。
     * **/
    public abstract MutablePair<String, Object> parseParams(HttpServletRequest request);

    /**
     * 返回需要更新的记录 <ID, 结果> 状态 和响应数据
     *
     * **/
    public abstract DivisionChannelNotifyModel doNotify(HttpServletRequest request, Object params,
                                                        List<PayOrderDivisionRecord> recordList, MchAppConfigContext mchAppConfigContext);

    public ResponseEntity doNotifyOrderNotExists(HttpServletRequest request) {
        return textResp("order not exists");
    }

    public ResponseEntity doNotifyOrderStateUpdateFail(HttpServletRequest request) {
        return textResp("update status error");
    }

    /** 文本类型的响应数据 **/
    protected ResponseEntity textResp(String text){

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity(text, httpHeaders, HttpStatus.OK);
    }

    /** json类型的响应数据 **/
    protected ResponseEntity jsonResp(Object body){

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity(body, httpHeaders, HttpStatus.OK);
    }


    /**request.getParameter 获取参数 并转换为JSON格式 **/
    protected JSONObject getReqParamJSON() {
        return requestKitBean.getReqParamJSON();
    }

    /**request.getParameter 获取参数 并转换为JSON格式 **/
    protected String getReqParamFromBody() {
        return requestKitBean.getReqParamFromBody();
    }

    /** 获取文件路径 **/
    protected String getCertFilePath(String certFilePath) {
        return channelCertConfigKitBean.getCertFilePath(certFilePath);
    }

    /** 获取文件File对象 **/
    protected File getCertFile(String certFilePath) {
        return channelCertConfigKitBean.getCertFile(certFilePath);
    }

}
