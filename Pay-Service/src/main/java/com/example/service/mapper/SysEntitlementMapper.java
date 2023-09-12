package com.example.service.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.SysEntitlement;
import org.apache.ibatis.annotations.Param;

/**
 * 系统权限表 Mapper 接口
 *
 */
public interface SysEntitlementMapper extends BaseMapper<SysEntitlement> {

    Integer userHasLeftMenu(@Param("userId") Long userId, @Param("sysType") String sysType);

}
