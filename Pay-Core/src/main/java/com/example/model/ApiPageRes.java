
package com.example.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.constants.ApiCodeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
* 接口返回对象
*
*/
@Data
@ApiModel
public class ApiPageRes<M> extends ApiRes {

    /** 数据对象 **/
    @ApiModelProperty(value = "业务数据")
    private PageBean<M> data;


    /** 业务处理成功， 封装分页数据， 仅返回必要参数 **/
    public static <M> ApiPageRes<M> pages(IPage<M> iPage){

        PageBean<M> innerPage = new PageBean<>();
        innerPage.setRecords(iPage.getRecords());  //记录明细
        innerPage.setTotal(iPage.getTotal()); //总条数
        innerPage.setCurrent(iPage.getCurrent()); //当前页码
        innerPage.setHasNext( iPage.getPages() > iPage.getCurrent()); //是否有下一页

        ApiPageRes result = new ApiPageRes();
        result.setData(innerPage);
        result.setCode(ApiCodeEnum.SUCCESS.getCode());
        result.setMsg(ApiCodeEnum.SUCCESS.getMsg());

        return result;
    }


    @Data
    @ApiModel
    public static class PageBean<M> {

        /** 数据列表 */
        @ApiModelProperty(value = "数据列表")
        private List<M> records;

        /** 总数量 */
        @ApiModelProperty(value = "总数量")
        private Long total;

        /** 当前页码 */
        @ApiModelProperty(value = "当前页码")
        private Long current;

        /** 是否包含下一页， true:包含 ，false: 不包含 */
        @ApiModelProperty(value = "是否包含下一页， true:包含 ，false: 不包含")
        private boolean hasNext;

    }

}
