package com.example.components.oss.service.impl;

import com.example.components.oss.config.OssYmlConfig;
import com.example.components.oss.constant.OssSavePlaceEnum;
import com.example.components.oss.service.IOssService;
import com.example.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;

;

/**
 * 本地存储 实现类
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "isys.oss.service-type", havingValue = "local")
public class LocalFileService implements IOssService {

    @Resource
    private ISysConfigService sysConfigService;
    @Resource
    private OssYmlConfig ossYmlConfig;

    @Override
    public String upload2PreviewUrl(OssSavePlaceEnum ossSavePlaceEnum, MultipartFile multipartFile, String saveDirAndFileName) {

        try {

            String savePath = ossSavePlaceEnum ==
                    OssSavePlaceEnum.PUBLIC ? ossYmlConfig.getOss().getFilePublicPath() : ossYmlConfig.getOss().getFilePrivatePath();

            File saveFile = new File(savePath + File.separator + saveDirAndFileName);

            //如果文件夹不存在则创建文件夹
            File dir = saveFile.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            multipartFile.transferTo(saveFile);

        } catch (Exception e) {

            log.error("", e);
        }

        // 私有文件 不返回预览文件地址
        if (ossSavePlaceEnum == OssSavePlaceEnum.PRIVATE) {
            return saveDirAndFileName;
        }

        return sysConfigService.getDBApplicationConfig().getOssPublicSiteUrl() + "/" + saveDirAndFileName;
    }

    @Override
    public boolean downloadFile(OssSavePlaceEnum ossSavePlaceEnum, String source, String target) {
        return false;
    }
}
