package com.example.pay.controller.scanimg;

import com.example.pay.controller.payorder.AbstractPayOrderController;
import com.example.pay.util.CodeImgUtil;
import com.example.util.PayUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
* jeepay 扫描图片生成器
*
*/
@RestController
@RequestMapping("/api/scan")
public class ScanImgController extends AbstractPayOrderController {

    /** 返回 图片地址信息  **/
    @RequestMapping("/imgs/{aesStr}.png")
    public void qrImgs(@PathVariable("aesStr") String aesStr) throws Exception {
        String str = PayUtil.aesDecode(aesStr);
        int width = getValIntegerDefault("width", 200);
        int height = getValIntegerDefault("height", 200);
        CodeImgUtil.writeQrCode(response.getOutputStream(), str, width, height);
    }
}
