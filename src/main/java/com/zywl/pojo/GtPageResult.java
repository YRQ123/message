package com.zywl.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Class GtPageResult
 * @Description TODO
 * @Author Mr.Wang
 * @Date 2019/11/2 13:36
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GtPageResult {
    private int code;
    private String msg;
    private int count;
    private List data;
}
