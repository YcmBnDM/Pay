package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.SysRole;
import com.example.entity.SysRoleEntRela;
import com.example.entity.SysUserRoleRela;
import com.example.exception.BizException;
import com.example.service.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * 系统角色表 服务实现类
 */
@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    @Resource
    private SysUserRoleRelaService sysUserRoleRelaService;

    @Resource
    private SysRoleEntRelaService sysRoleEntRelaService;


    /**
     * 根据用户查询全部角色集合
     **/
    public List<String> findListByUser(Long sysUserId) {
        List<String> result = new ArrayList<>();
        sysUserRoleRelaService.list(SysUserRoleRela.gw()
                .eq(SysUserRoleRela::getUserId, sysUserId))
                .stream().forEach(r -> result.add(r.getRoleId()));

        return result;
    }


    @Transactional
    public void removeRole(String roleId) {

        if (sysUserRoleRelaService.count(SysUserRoleRela.gw().eq(SysUserRoleRela::getRoleId, roleId)) > 0) {
            throw new BizException("当前角色已分配到用户， 不可删除！");
        }

        //删除当前表
        removeById(roleId);

        //删除关联表
        sysRoleEntRelaService.remove(SysRoleEntRela.gw().eq(SysRoleEntRela::getRoleId, roleId));

    }


}
