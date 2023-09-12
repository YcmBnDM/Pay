package com.example.model;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

/**
 * BaseModel 封装公共处理函数
 */
public class BaseModel<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ext参数, 用作扩展参数， 会在转换为api数据时自动将ext全部属性放置在对象的主属性上, 并且不包含ext属性
     * <p>
     * api接口扩展字段， 当包含该字段时 将自动填充到实体对象属性中如{id:1, ext:{abc:222}}  则自动转换为： {id:1, abc:222}，
     * 需配合ResponseBodyAdvice使用
     **/
    @TableField(exist = false)
    private JSONObject ext;

    //获取的时候设置默认值
    public JSONObject getExt() {
        return ext;
    }

    //设置扩展字段
    public BaseModel addExt(String key, Object val) {
        if (ext == null) {
            ext = new JSONObject();
        }
        ext.put(key, val);
        return this;
    }

    /**
     * get ext value  可直接使用JSONObject对象的函数
     **/
    public JSONObject extv() {
        return ext == null ? new JSONObject() : ext;
    }


}
