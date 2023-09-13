
package com.example.controller.sysuser;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.aop.MethodLog;
import com.example.constants.ApiCodeEnum;
import com.example.controller.CommonCtrl;
import com.example.entity.SysLog;
import com.example.model.ApiPageRes;
import com.example.model.ApiRes;
import com.example.service.impl.SysLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

;

/**
 * 系统日志记录类
 */
@Api(tags = "系统管理（系统日志）")
@RestController
@RequestMapping("api/sysLog")
public class SysLogController extends CommonCtrl {

    @Resource
    SysLogService sysLogService;


    /**
     * @describe: 日志记录列表
     */
    @ApiOperation("系统日志列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "pageNumber", value = "分页页码", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数", dataType = "int", defaultValue = "20"),
            @ApiImplicitParam(name = "createdStart", value = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间，查询范围：大于等于此时间"),
            @ApiImplicitParam(name = "createdEnd", value = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间，查询范围：小于等于此时间"),
            @ApiImplicitParam(name = "userId", value = "系统用户ID"),
            @ApiImplicitParam(name = "userName", value = "用户姓名"),
            @ApiImplicitParam(name = "sysType", value = "所属系统： MGR-运营平台, MCH-商户中心")
    })
    @PreAuthorize("hasAuthority('ENT_LOG_LIST')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiPageRes<SysLog> list() {
        SysLog sysLog = getObject(SysLog.class);
        JSONObject paramJSON = getReqParamJSON();
        // 查询列表
        LambdaQueryWrapper<SysLog> condition = SysLog.gw();
        condition.orderByDesc(SysLog::getCreatedAt);
        if (sysLog.getUserId() != null) {
            condition.eq(SysLog::getUserId, sysLog.getUserId());
        }
        if (sysLog.getUserName() != null) {
            condition.eq(SysLog::getUserName, sysLog.getUserName());
        }
        if (StringUtils.isNotEmpty(sysLog.getSysType())) {
            condition.eq(SysLog::getSysType, sysLog.getSysType());
        }
        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                condition.ge(SysLog::getCreatedAt, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                condition.le(SysLog::getCreatedAt, paramJSON.getString("createdEnd"));
            }
        }
        IPage<SysLog> pages = sysLogService.page(getIPage(), condition);
        return ApiPageRes.pages(pages);
    }

    /**
     * @describe: 查看日志信息
     */
    @ApiOperation("系统日志详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "sysLogId", value = "系统日志ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_SYS_LOG_VIEW')")
    @RequestMapping(value = "/{sysLogId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("sysLogId") String sysLogId) {
        SysLog sysLog = sysLogService.getById(sysLogId);
        if (sysLog == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(sysLog);
    }

    /**
     * @describe: 删除日志信息
     */
    @ApiOperation("删除日志信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "iToken", value = "用户身份凭证", required = true, paramType = "header"),
            @ApiImplicitParam(name = "selectedIds", value = "系统日志ID（若干个ID用英文逗号拼接）", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_SYS_LOG_DEL')")
    @MethodLog(remark = "删除日志信息")
    @RequestMapping(value = "/{selectedIds}", method = RequestMethod.DELETE)
    public ApiRes delete(@PathVariable("selectedIds") String selectedIds) {
        String[] ids = selectedIds.split(",");
        List<Long> idsList = new LinkedList<>();
        for (String id : ids) {
            idsList.add(Long.valueOf(id));
        }
        boolean result = sysLogService.removeByIds(idsList);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
        }
        return ApiRes.ok();
    }
}
