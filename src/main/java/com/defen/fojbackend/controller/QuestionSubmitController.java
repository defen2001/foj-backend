package com.defen.fojbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.defen.fojbackend.common.ApiResponse;
import com.defen.fojbackend.exception.BusinessException;
import com.defen.fojbackend.exception.ErrorCode;
import com.defen.fojbackend.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.defen.fojbackend.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.defen.fojbackend.model.entity.User;
import com.defen.fojbackend.model.vo.QuestionSubmitVo;
import com.defen.fojbackend.service.QuestionSubmitService;
import com.defen.fojbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目提交接口
 */
@RestController
@RequestMapping("/question_submit")
@Slf4j
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return 提交记录id
     */
    @PostMapping("/")
    public ApiResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                              HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数错误");
        }
        User loginUser = userService.getCurrentUser(request);
        long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ApiResponse.success(questionSubmitId);
    }

    /**
     * 分页获取题目提交列表
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    public ApiResponse<Page<QuestionSubmitVo>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest, HttpServletRequest request) {
        //从数据提取到原始的分页信息
        Page<QuestionSubmitVo> questionSubmitVoPage = questionSubmitService.getQuestionSubmitVoPage(questionSubmitQueryRequest, request);
        //返回脱敏信息
        return ApiResponse.success(questionSubmitVoPage);
    }

}