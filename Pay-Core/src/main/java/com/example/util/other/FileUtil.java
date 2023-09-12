package com.example.util.other;

import com.example.constants.CS;
import com.example.exception.BizException;


/**
 * 文件工具类
 */
public class FileUtil {
    /**
     * 获取文件的后缀名
     *
     * @param appendDot 是否拼接.
     * @return
     */
    public static String getFileSuffix(String fullFileName, boolean appendDot) {
        if (fullFileName == null || fullFileName.indexOf(".") < 0 || fullFileName.length() <= 1) {
            return "";
        }
        return (appendDot ? "." : "") + fullFileName.substring(fullFileName.lastIndexOf(".") + 1);
    }


    /**
     * 获取有效的图片格式， 返回null： 不支持的图片类型
     **/
    public static String getImgSuffix(String filePath) {

        String suffix = getFileSuffix(filePath, false).toLowerCase();
        if (CS.ALLOW_UPLOAD_IMG_SUFFIX.contains(suffix)) {
            return suffix;
        }
        throw new BizException("不支持的图片类型");
    }

}
