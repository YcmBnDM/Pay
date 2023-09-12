package com.example.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.SysRoleEntRela;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统角色权限关联表 Mapper 接口
 *
 */
public interface SysRoleEntRelaMapper extends BaseMapper<SysRoleEntRela> {

    List<String> selectEntIdsByUserId(@Param("userId") Long userId, @Param("sysType") String sysType);

}
