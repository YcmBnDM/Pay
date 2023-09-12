package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 返回原始数据
 */
@Data
@AllArgsConstructor
public class OriginalRes {

    /**
     * 返回数据
     **/
    private Object data;

    public static OriginalRes ok(Object data) {
        return new OriginalRes(data);
    }
}
