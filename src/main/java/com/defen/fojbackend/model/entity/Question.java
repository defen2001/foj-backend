package com.defen.fojbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 题目
 *
 * @TableName question
 */
@TableName(value = "question")
@Data
public class Question implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 分类（简单，中等，困难）
     */
    @TableField(value = "category")
    private String category;

    /**
     * 标签列表（json 数组）
     */
    @TableField(value = "tags")
    private String tags;

    /**
     * 题目答案
     */
    @TableField(value = "answer")
    private String answer;

    /**
     * 题目提交数
     */
    @TableField(value = "submit_num")
    private Integer submitNum;

    /**
     * 题目通过数
     */
    @TableField(value = "accepted_num")
    private Integer acceptedNum;

    /**
     * 判题用例（json 数组）
     */
    @TableField(value = "judge_case")
    private String judgeCase;

    /**
     * 判题配置（json 对象）
     */
    @TableField(value = "judge_config")
    private String judgeConfig;

    /**
     * 创建用户 id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 编辑时间
     */
    @TableField(value = "edit_time")
    private Date editTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField(value = "is_delete")
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}