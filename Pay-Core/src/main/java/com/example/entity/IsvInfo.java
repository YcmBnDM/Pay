
package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.model.BaseModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 服务商信息表
 *
 */
@ApiModel(value = "服务商信息表", description = "")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_isv_info")
public class IsvInfo extends BaseModel implements Serializable {

    //gw
    public static final LambdaQueryWrapper<IsvInfo> gw(){
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID=1L;

    /**
     * 服务商号
     */
    @ApiModelProperty(value = "服务商号")
    @TableId(value = "isv_no", type = IdType.INPUT)
    private String isvNo;

    /**
     * 服务商名称
     */
    @ApiModelProperty(value = "服务商名称")
    private String isvName;

    /**
     * 服务商简称
     */
    @ApiModelProperty(value = "服务商简称")
    private String isvShortName;

    /**
     * 联系人姓名
     */
    @ApiModelProperty(value = "联系人姓名")
    private String contactName;

    /**
     * 联系人手机号
     */
    @ApiModelProperty(value = "联系人手机号")
    private String contactTel;

    /**
     * 联系人邮箱
     */
    @ApiModelProperty(value = "联系人邮箱")
    private String contactEmail;

    /**
     * 状态: 0-停用, 1-正常
     */
    @ApiModelProperty(value = "状态: 0-停用, 1-正常")
    private Byte state;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 创建者用户ID
     */
    @ApiModelProperty(value = "创建者用户ID")
    private Long createdUid;

    /**
     * 创建者姓名
     */
    @ApiModelProperty(value = "创建者姓名")
    private String createdBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createdAt;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private Date updatedAt;


}
