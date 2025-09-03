package com.defen.fojbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.defen.fojbackend.model.dto.question.QuestionQueryRequest;
import com.defen.fojbackend.model.entity.Question;
import com.defen.fojbackend.model.vo.QuestionVo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author chendefeng
 * @description 针对表【question(题目)】的数据库操作Service
 * @createDate 2025-09-03 10:55:02
 */
public interface QuestionService extends IService<Question> {

    /**
     * 校验参数
     *
     * @param question
     * @param add
     */
    void validQuestion(Question question, boolean add);

    /**
     * 查询请求
     *
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);

    /**
     * 获取单个题目封装
     *
     * @param question
     * @param request
     * @return
     */
    QuestionVo getQuestionVo(Question question, HttpServletRequest request);

    /**
     * 分页获取题目封装
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    Page<QuestionVo> getQuestionVoPage(QuestionQueryRequest questionQueryRequest, HttpServletRequest request);

}
