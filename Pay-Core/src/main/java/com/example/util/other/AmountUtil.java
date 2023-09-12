package com.example.util.other;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.FieldPosition;

/**
 * 金额工具类
 */
public class AmountUtil {

    /**
     * 将字符 ”元” 转换为 “分”
     *
     * @param str
     * @return
     */
    public static String convertDollarToCent(String str) {
        DecimalFormat df = new DecimalFormat("0.00");
        StringBuffer sb = df.format(Double.parseDouble(str), new StringBuffer(), new FieldPosition(0));
        int idx = sb.toString().indexOf(".");
        sb.deleteCharAt(idx);
        for (; sb.length() != 1; ) {
            if (sb.charAt(0) == '0') {
                sb.deleteCharAt(0);
            } else {
                break;
            }
        }
        return sb.toString();
    }


    /**
     * 将 字符串 类型的 “分” 转换成 “元“（长格式）,如：100分被转换为1.00元。
     *
     * @param cent
     * @return
     */
    public static String convertCentToDollar(String cent) {
        if ("".equals(cent) || cent == null) {
            return "";
        }
        long dollar;
        if (cent.length() != 0) {
            if (cent.charAt(0) != '+') {
                cent = cent.substring(1);
            }
            dollar = Long.parseLong(cent);
        } else {
            return "";
        }
        boolean nagative = false;
        if (dollar < 0) {
            nagative = true;
            dollar = Math.abs(dollar);
        }
        String result = Long.toString(dollar);
        if (result.length() == 1) {
            return (nagative ? ("-0.0" + result) : ("0.0" + result));
        }
        if (result.length() == 2) {
            return (nagative ? ("-0." + result) : ("0." + result));
        } else {
            return (nagative ? ("-" + result.substring(0, result.length() - 2) + "." + result.substring(result.length() - 2))
                    : (result.substring(0, result.length() - 2) + "." + result.substring(result.length() - 2)));
        }
    }

    /**
     * 将Long 类型 “分” 转换成“元”,如：100分被转换为1.00元。
     *
     * @param cent
     * @return
     */
    public static String convertCent2Dollar(Long cent) {
        if (cent == null) {
            return "";
        }
        return new BigDecimal(cent).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }


    /**
     * 将字符串"分"转换成"元"（短格式），如：100分被转换为1元。
     *
     * @param s
     * @return
     */
    public static String convertCent2DollarShort(String s) {
        String ss = convertCentToDollar(s);
        ss = "" + Double.parseDouble(ss);
        if (ss.endsWith(".0")) {
            return ss.substring(0, ss.length() - 2);
        }
        if (ss.endsWith(".00")) {
            return ss.substring(0, ss.length() - 3);
        } else {
            return ss;
        }
    }

    /**
     * 计算百分比类型的各种费用值  （订单金额 * 真实费率  结果四舍五入并保留0位小数 ）
     *
     * @param amount 订单金额  （保持与数据库的格式一致 ，单位：分）
     * @param rate   费率   （保持与数据库的格式一致 ，真实费率值，如费率为0.55%，则传入 0.0055）
     */
    public static Long calPercentageFee(Long amount, BigDecimal rate) {
        return calPercentageFee(amount, rate, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 计算百分比类型的各种费用值  （订单金额 * 真实费率  结果四舍五入并保留0位小数 ）
     *
     * @param amount 订单金额  （保持与数据库的格式一致 ，单位：分）
     * @param rate   费率   （保持与数据库的格式一致 ，真实费率值，如费率为0.55%，则传入 0.0055）
     * @param mode   模式 参考：BigDecimal.ROUND_HALF_UP(四舍五入)   BigDecimal.ROUND_FLOOR（向下取整）
     */
    public static Long calPercentageFee(Long amount, BigDecimal rate, int mode) {
        //费率乘以订单金额   结果四舍五入并保留0位小数
        return new BigDecimal(amount).multiply(rate).setScale(0, mode).longValue();
    }


}
