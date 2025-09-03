package com.defen.fojbackend.model.vo;

import cn.hutool.json.JSONUtil;
import com.defen.fojbackend.model.dto.questionsubmit.JudgeInfo;
import com.defen.fojbackend.model.entity.QuestionSubmit;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目提交视图实体类
 */
@Data
public class QuestionSubmitVo implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 判题信息（json 对象）
     */
    private JudgeInfo judgeInfo;

    /**
     * 判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）
     */
    private Integer status;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 提交用户信息
     */
    private UserVo userVo;
    /**
     * 题目信息
     */
    private QuestionVo questionVO;

    private static final long serialVersionUID = 1L;

    /**
     * 包装类转实体类
     */
    public static QuestionSubmit voToObj(QuestionSubmitVo questionSubmitVo) {
        if (questionSubmitVo == null) {
            return null;
        }
        QuestionSubmit questionSubmit = new QuestionSubmit();
        BeanUtils.copyProperties(questionSubmitVo, questionSubmit);
        JudgeInfo judgeInfoObj = questionSubmitVo.getJudgeInfo();
        if (judgeInfoObj != null) {
            questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfoObj));
        }
        return questionSubmit;
    }

    /**
     * 对象转包装类
     */
    public static QuestionSubmitVo objToVo(QuestionSubmit questionSubmit) {
        if (questionSubmit == null) {
            return null;
        }
        QuestionSubmitVo questionSubmitVO = new QuestionSubmitVo();
        BeanUtils.copyProperties(questionSubmit, questionSubmitVO);
        String judgeInfoStr = questionSubmit.getJudgeInfo();
        questionSubmitVO.setJudgeInfo(JSONUtil.toBean(judgeInfoStr, JudgeInfo.class));
        return questionSubmitVO;
    }
}