
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
 * 商户支付通道表
 *
 */
@ApiModel(value = "商户支付通道表", description = "")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_pay_passage")
public class MchPayPassage extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<MchPayPassage> gw(){
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
     * 商户号
     */
    @ApiModelProperty(value = "商户号")
    private String mchNo;

    /**
     * 应用ID
     */
    @ApiModelProperty(value = "应用ID")
    private String appId;

    /**
     * 支付接口
     */
    @ApiModelProperty(value = "支付接口")
    private String ifCode;

    /**
     * 支付方式
     */
    @ApiModelProperty(value = "支付方式")
    private String wayCode;

    /**
     * 支付方式费率
     */
    @ApiModelProperty(value = "支付方式费率")
    private BigDecimal rate;

    /**
     * 风控数据
     */
    @ApiModelProperty(value = "风控数据")
    private String riskConfig;

    /**
     * 状态: 0-停用, 1-启用
     */
    @ApiModelProperty(value = "状态: 0-停用, 1-启用")
    private Byte state;

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
