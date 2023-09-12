
package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统角色表
 *
 */
@ApiModel(value = "系统角色表", description = "")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_sys_role")
public class SysRole implements Serializable {

    //gw
    public static final LambdaQueryWrapper<SysRole> gw(){
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID=1L;

    /**
     * 角色ID, ROLE_开头
     */
    @ApiModelProperty(value = "角色ID, ROLE_开头")
    @TableId
    private String roleId;

    /**
     * 角色名称
     */
    @ApiModelProperty(value = "角色名称")
    private String roleName;

    /**
     * 所属系统： MGR-运营平台, MCH-商户中心
     */
    @ApiModelProperty(value = "所属系统： MGR-运营平台, MCH-商户中心")
    private String sysType;

    /**
     * 所属商户ID / 0(平台)
     */
    @ApiModelProperty(value = "所属商户ID / 0(平台)")
    private String belongInfoId;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private Date updatedAt;


}
