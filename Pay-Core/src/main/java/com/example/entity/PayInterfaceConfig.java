
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
import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付接口配置参数表
 *
 */
@ApiModel(value = "支付接口配置参数表", description = "")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pay_interface_config")
public class PayInterfaceConfig extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<PayInterfaceConfig> gw(){
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 账号类型:1-服务商 2-商户
     */
    @ApiModelProperty(value = "账号类型:1-服务商 2-商户")
    private Byte infoType;

    /**
     * 服务商或商户No
     */
    @ApiModelProperty(value = "服务商号（服务商支付参数）或应用AppId（商户支付参数）")
    private String infoId;

    /**
     * 支付接口代码
     */
    @ApiModelProperty(value = "支付接口代码")
    private String ifCode;

    /**
     * 接口配置参数,json字符串
     */
    @ApiModelProperty(value = "接口配置参数,json字符串")
    private String ifParams;

    /**
     * 支付接口费率
     */
    @ApiModelProperty(value = "支付接口费率")
    private BigDecimal ifRate;

    /**
     * 状态: 0-停用, 1-启用
     */
    @ApiModelProperty(value = "状态: 0-停用, 1-启用")
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
     * 更新者用户ID
     */
    @ApiModelProperty(value = "更新者用户ID")
    private Long updatedUid;

    /**
     * 更新者姓名
     */
    @ApiModelProperty(value = "更新者姓名")
    private String updatedBy;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private Date updatedAt;


}
