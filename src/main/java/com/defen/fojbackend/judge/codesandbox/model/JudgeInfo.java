package com.defen.fojbackend.judge.codesandbox.model;

import lombok.Data;

/**
 * 判题信息
 */
@Data
public class JudgeInfo {

    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 消耗内存
     */
    private Long memory;

    /**
     * 消耗时间
     */
    private Long time;

    /**
     * 状态码
     */
    private Integer status;

    /**
     * 通过用例数
     */
    private Integer pass;

    /**
     * 总用例数
     */
    private Integer total;

    // 未通过的最后一个用例信息
    private String input;
    private String output;
    private String expectedOutput;
}