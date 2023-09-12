package com.example.ctrl;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.bean.RequestKitBean;
import com.example.constants.ApiCodeEnum;
import com.example.exception.BizException;
import com.example.model.BaseModel;
import com.example.util.DateKitUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 抽象公共Ctrl
 *
 */
@Slf4j
public abstract class AbstractCtrl {


    private static final String PAGE_INDEX_PARAM_NAME = "pageNumber";  //分页页码 参数名
    private static final String PAGE_SIZE_PARAM_NAME = "pageSize";  //分页条数 参数名
    private static final int DEFAULT_PAGE_INDEX = 1;  // 默认页码： 第一页
    private static final int DEFAULT_PAGE_SIZE = 20;  // 默认条数： 20

    private static final String SORT_FIELD_PARAM_NAME = "sortField";  //排序字段
    private static final String SORT_ORDER_FLAG_PARAM_NAME = "sortOrder";  // 排序正序， 倒序标志

    @Resource
    protected HttpServletRequest request;   //自动注入request

    @Resource
    protected HttpServletResponse response;  //自动注入response

    @Resource
    protected RequestKitBean requestKitBean;

    /**
     * 获取json格式的请求参数
     **/
    protected JSONObject getReqParamJSON() {
        return requestKitBean.getReqParamJSON();
    }

    /**
     * 获取页码
     **/
    protected int getPageIndex() {
        Integer pageIndex = getReqParamJSON().getInteger(PAGE_INDEX_PARAM_NAME);
        if (pageIndex == null) {
            return DEFAULT_PAGE_INDEX;
        }
        return pageIndex;
    }

    /**
     * 获取条数， 默认不允许查询全部数据
     **/
    protected int getPageSize() {
        return getPageSize(false);
    }

    /**
     * 获取条数,  加入条件：是否允许获取全部数据
     **/
    protected int getPageSize(boolean allowQueryAll) {
        Integer pageSize = getReqParamJSON().getInteger(PAGE_SIZE_PARAM_NAME);

        if (allowQueryAll && pageSize != null && pageSize == -1) {
            return Integer.MAX_VALUE; // -1代表获取全部数据，查询int最大值的数据
        }
        if (pageSize == null || pageSize < 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }

    /**
     * 获取Ipage分页信息, 默认不允许获取全部数据
     **/
    protected IPage getIPage() {
        return new Page(getPageIndex(), getPageSize());
    }

    /**
     * 获取Ipage分页信息, 加入条件：是否允许获取全部数据
     **/
    protected IPage getIPage(boolean allowQueryAll) {
        return new Page(getPageIndex(), getPageSize(allowQueryAll));
    }




    /**
     * 获取排序字段 MutablePair<是否正序， 排序字段>
     **/
    protected MutablePair<Boolean, String> getSortInfo() {

        String sortField = getReqParamJSON().getString(SORT_FIELD_PARAM_NAME);
        String sortOrderFlag = getReqParamJSON().getString(SORT_ORDER_FLAG_PARAM_NAME);
        if (StringUtils.isAllEmpty(sortField, sortField)) {
            return null;
        }

        return MutablePair.of("ascend".equalsIgnoreCase(sortOrderFlag), CharSequenceUtil.toUnderlineCase(sortField).toLowerCase());
    }


    /**
     * 获取请求参数值 [ T 类型 ], [ 非必填 ]
     **/
    protected <T> T getVal(String key, Class<T> cls) {
        return getReqParamJSON().getObject(key, cls);
    }

    /**
     * 获取请求参数值 [ T 类型 ], [ 必填 ]
     **/
    protected <T> T getValRequired(String key, Class<T> cls) {
        T value = getVal(key, cls);
        if (ObjectUtils.isEmpty(value)) {
            throw new BizException(ApiCodeEnum.PARAMS_ERROR, genParamRequiredMsg(key));
        }
        return value;
    }

    /**
     * 获取请求参数值 [ T 类型 ], [ 如为null返回默认值 ]
     **/
    protected <T> T getValDefault(String key, T defaultValue, Class<T> cls) {
        T value = getVal(key, cls);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 获取请求参数值 String 类型相关函数
     **/
    protected String getValString(String key) {
        return getVal(key, String.class);
    }

    protected String getValStringRequired(String key) {
        return getValRequired(key, String.class);
    }

    protected String getValStringDefault(String key, String defaultValue) {
        return getValDefault(key, defaultValue, String.class);
    }

    /**
     * 获取请求参数值 Byte 类型相关函数
     **/
    protected Byte getValByte(String key) {
        return getVal(key, Byte.class);
    }

    protected Byte getValByteRequired(String key) {
        return getValRequired(key, Byte.class);
    }

    protected Byte getValByteDefault(String key, Byte defaultValue) {
        return getValDefault(key, defaultValue, Byte.class);
    }

    /**
     * 获取请求参数值 Integer 类型相关函数
     **/
    protected Integer getValInteger(String key) {
        return getVal(key, Integer.class);
    }

    protected Integer getValIntegerRequired(String key) {
        return getValRequired(key, Integer.class);
    }

    protected Integer getValIntegerDefault(String key, Integer defaultValue) {
        return getValDefault(key, defaultValue, Integer.class);
    }

    /**
     * 获取请求参数值 Long 类型相关函数
     **/
    protected Long getValLong(String key) {
        return getVal(key, Long.class);
    }

    protected Long getValLongRequired(String key) {
        return getValRequired(key, Long.class);
    }

    protected Long getValLongDefault(String key, Long defaultValue) {
        return getValDefault(key, defaultValue, Long.class);
    }

    /**
     * 获取请求参数值 BigDecimal 类型相关函数
     **/
    protected BigDecimal getValBigDecimal(String key) {
        return getVal(key, BigDecimal.class);
    }

    protected BigDecimal getValBigDecimalRequired(String key) {
        return getValRequired(key, BigDecimal.class);
    }

    protected BigDecimal getValBigDecimalDefault(String key, BigDecimal defaultValue) {
        return getValDefault(key, defaultValue, BigDecimal.class);
    }

    /**
     * 获取对象类型
     **/
    protected <T> T getObject(Class<T> clazz) {

        JSONObject params = getReqParamJSON();
        T result = params.toJavaObject(clazz);

        //如果属于BaseModel, 处理apiExtVal
        if (result instanceof BaseModel) {
            JSONObject resultTemp = (JSONObject) JSON.toJSON(result);
            //遍历原始参数
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (!resultTemp.containsKey(entry.getKey())) {
                    ((BaseModel) result).addExt(entry.getKey(), entry.getValue());
                }
            }
        }

        return result;
    }

    /**
     * 生成参数必填错误信息
     **/
    private String genParamRequiredMsg(String key) {
        return "参数" + key + "必填";
    }

    /**
     * 校验参数值不能为空
     */
    protected void checkRequired(String... keys) {

        for (String key : keys) {
            String value = getReqParamJSON().getString(key);
            if (StringUtils.isEmpty(value)) {
                throw new BizException(ApiCodeEnum.PARAMS_ERROR, genParamRequiredMsg(key));
            }
        }
    }

    /**
     * 得到前端传入的金额元,转换成长整型分
     **/
    public Long getRequiredAmountL(String name) {
        // 前端填写的为元,可以为小数点2位
        String amountStr = getValStringRequired(name);
        // 转成分
        Long amountL = new BigDecimal(amountStr.trim()).multiply(new BigDecimal(100)).longValue();
        return amountL;
    }

    /**
     * 得到前端传入的金额元,转换成长整型分 (非必填)
     **/
    public Long getAmountL(String name) {
        // 前端填写的为元,可以为小数点2位
        String amountStr = getValString(name);
        if (StringUtils.isEmpty(amountStr)) {
            return null;
        }
        // 转成单位分
        Long amountL = new BigDecimal(amountStr.trim()).multiply(new BigDecimal(100)).longValue();
        return amountL;
    }

    /**
     * 处理参数中的金额(将前端传入金额元转成分)
     * modify: 20181206 添加JSON对象中的对象属性转换为分 格式[xxx.xxx]
     *
     * @param names
     */
    public void handleParamAmount(String... names) {
        for (String name : names) {
            // 前端填写的为元,可以为小数点2位
            String amountStr = getValString(name);
            if (StringUtils.isNotBlank(amountStr)) {
                // 转成单位分
                Long amountL = new BigDecimal(amountStr.trim()).multiply(new BigDecimal(100)).longValue();
                if (name.indexOf(".") < 0) {
                    getReqParamJSON().put(name, amountL);
                    continue;
                }
                getReqParamJSON().getJSONObject(name.substring(0, name.indexOf("."))).put(name.substring(name.indexOf(".") + 1), amountL);
            }
        }
    }

    /**
     * 获取查询的时间范围
     *
     * @return
     */
    protected Date[] getQueryDateRange() {
        //默认参数为 queryDateRange
        return DateKitUtil.getqueryDateRange(getReqParamJSON().getString("queryDateRange"));
    }

    /**
     * 请求参数转换为map格式
     **/
    public Map<String, Object> request2payResponseMap(HttpServletRequest request, String[] paramArray) {
        Map<String, Object> responseMap = new HashMap<>();
        for (int i = 0; i < paramArray.length; i++) {
            String key = paramArray[i];
            String v = request.getParameter(key);
            if (v != null) {
                responseMap.put(key, v);
            }
        }
        return responseMap;
    }

    /**
     * 将上传的文件进行保存 - 公共函数
     **/
    protected void saveFile(MultipartFile file, String savePath) throws Exception {
        File saveFile = new File(savePath);
        //如果文件夹不存在则创建文件夹
        File dir = saveFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        file.transferTo(saveFile);
    }

    /**
     * 获取客户端ip地址
     **/
    public String getClientIp() {
        return requestKitBean.getClientIp();
    }


    /**
     * 获取用户代理
     * @return
     */
    public String getUserAgent() {
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.isNotEmpty(userAgent) ? userAgent : "未知";
    }
}
