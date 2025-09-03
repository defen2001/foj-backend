package com.defen.fojbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.defen.fojbackend.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.defen.fojbackend.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.defen.fojbackend.model.entity.QuestionSubmit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.defen.fojbackend.model.entity.User;
import com.defen.fojbackend.model.vo.QuestionSubmitVo;

/**
 * @author chendefeng
 * @description 针对表【question_submit(题目提交)】的数据库操作Service
 * @createDate 2025-09-03 10:55:18
 */
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    /**
     * 查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);

    /**
     * 获取题目提交封装
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    QuestionSubmitVo getQuestionSubmitVo(QuestionSubmit questionSubmit, User loginUser);

    /**
     * 分页获取题目提交封装
     *
     * @param questionSubmitPage
     * @param loginUser
     * @return
     */
    Page<QuestionSubmitVo> getQuestionSubmitVoPage(Page<QuestionSubmit> questionSubmitPage, User loginUser);

}
