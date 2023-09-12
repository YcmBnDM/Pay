package com.example.service.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.SysUserAuth;
import org.apache.ibatis.annotations.Param;

/**
 * 操作员认证表 Mapper 接口
 *
 */
public interface SysUserAuthMapper extends BaseMapper<SysUserAuth> {

    SysUserAuth selectByLogin(@Param("identifier")String identifier,
                              @Param("identityType")Byte identityType, @Param("sysType")String sysType);

}
