
package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 系统角色权限关联表
 *
 */
@ApiModel(value = "系统角色权限关联表", description = "")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_sys_role_ent_rela")
public class SysRoleEntRela implements Serializable {

    //gw
    public static final LambdaQueryWrapper<SysRoleEntRela> gw(){
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID=1L;

    /**
     * 角色ID
     */
    @ApiModelProperty(value = "角色ID")
    private String roleId;

    /**
     * 权限ID
     */
    @ApiModelProperty(value = "权限ID")
    private String entId;


}
