package com.example.util;

import cn.hutool.crypto.SecureUtil;
import com.example.constants.CS;
import com.example.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Pay工具类
 */
@Slf4j
public class PayUtil {
    private static String encodingCharset = "UTF-8";
    public static byte[] AES_KEY = "4ChT08phkz59hquD795X7w==".getBytes();

    /**
     * 加密
     *
     * @param str
     * @return
     */
    public static String aesEncode(String str) {
        return SecureUtil.aes(PayUtil.AES_KEY).encryptHex(str);
    }

    /**
     * 解密
     *
     * @param str
     * @return
     */
    public static String aesDecode(String str) {
        return SecureUtil.aes(PayUtil.AES_KEY).decryptStr(str);
    }


    /**
     * 计算签名摘要
     *
     * @param map
     * @param key
     * @return
     */
    public static String getSign(Map<String, Object> map, String key) {
        ArrayList<String> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (null != entry.getValue() && !"".equals(entry.getValue())) {
                list.add(entry.getKey() + "=" + entry.getValue() + "&");
            }
        }
        int size = list.size();
        String[] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString();
        result += "key=" + key;
        log.info("signStr:{}", result);
        result = md5(result, encodingCharset).toUpperCase();
        log.info("sign:{}", result);
        return result;
    }


    /**
     * MD5 加密算法
     *
     * @param value
     * @param charset
     * @return
     */
    public static String md5(String value, String charset) {
        MessageDigest md = null;
        try {
            byte[] data = value.getBytes(charset);
            md = MessageDigest.getInstance("MD5");
            byte[] digestData = md.digest(data);
            return toHex(digestData);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 转16 进制
     * @param input
     * @return
     */
    public static String toHex(byte input[]) {
        if (input == null) {
            return null;
        }
        StringBuffer output = new StringBuffer(input.length * 2);
        for (int i = 0; i < input.length; i++) {
            int current = input[i] & 0xff;
            if (current < 16) {
                output.append("0");
            }
            output.append(Integer.toString(current, 16));
        }

        return output.toString();
    }

    /**
     * map 转换为  url参数
     **/
    public static String genUrlParams(Map<String, Object> paraMap) {
        if (paraMap == null || paraMap.isEmpty()) {
            return "";
        }
        StringBuffer urlParam = new StringBuffer();
        Set<String> keySet = paraMap.keySet();
        int i = 0;
        for (String key : keySet) {
            urlParam.append(key).append("=").append(paraMap.get(key) == null ? "" : doEncode(paraMap.get(key).toString()));
            if (++i == keySet.size()) {
                break;
            }
            urlParam.append("&");
        }
        return urlParam.toString();
    }

    static String doEncode(String str) {
        if (str.contains("+")) {
            return URLEncoder.encode(str);
        }
        return str;
    }

    /**
     * 校验微信/支付宝二维码是否符合规范， 并根据支付类型返回对应的支付方式
     **/
    public static String getPayWayCodeByBarCode(String barCode) {

        if (StringUtils.isEmpty(barCode)) {
            throw new BizException("条码为空");
        }

        //微信 ： 用户付款码条形码规则：18位纯数字，以10、11、12、13、14、15开头
        //文档： https://pay.weixin.qq.com/wiki/doc/api/micropay.php?chapter=5_1
        if (barCode.length() == 18 && Pattern.matches("^(10|11|12|13|14|15)(.*)", barCode)) {
            return CS.PAY_WAY_CODE.WX_BAR;
        }
        //支付宝： 25~30开头的长度为16~24位的数字
        //文档： https://docs.open.alipay.com/api_1/alipay.trade.pay/
        else if (barCode.length() >= 16 && barCode.length() <= 24 && Pattern.matches("^(25|26|27|28|29|30)(.*)", barCode)) {
            return CS.PAY_WAY_CODE.ALI_BAR;
        }
        //云闪付： 二维码标准： 19位 + 62开头
        //文档：https://wenku.baidu.com/view/b2eddcd09a89680203d8ce2f0066f5335a8167fa.html
        else if (barCode.length() == 19 && Pattern.matches("^(62)(.*)", barCode)) {
            return CS.PAY_WAY_CODE.YSF_BAR;
        } else {  //暂时不支持的条码类型
            throw new BizException("不支持的条码");
        }
    }
}
