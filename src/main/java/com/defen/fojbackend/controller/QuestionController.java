package com.defen.fojbackend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.defen.fojbackend.annotation.AuthCheck;
import com.defen.fojbackend.common.ApiResponse;
import com.defen.fojbackend.common.DeleteRequest;
import com.defen.fojbackend.constant.UserConstant;
import com.defen.fojbackend.exception.BusinessException;
import com.defen.fojbackend.exception.ErrorCode;
import com.defen.fojbackend.exception.ExceptionUtils;
import com.defen.fojbackend.model.dto.question.*;
import com.defen.fojbackend.model.entity.Question;
import com.defen.fojbackend.model.entity.User;
import com.defen.fojbackend.model.vo.QuestionVo;
import com.defen.fojbackend.service.QuestionService;
import com.defen.fojbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 题目接口
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public ApiResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        List<String> tags = questionAddRequest.getTags();
        if (tags != null) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        List<JudgeCase> judgeCase = questionAddRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCase));
        }
        JudgeConfig judgeConfig = questionAddRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        questionService.validQuestion(question, true);
        User loginUser = userService.getCurrentUser(request);
        question.setUserId(loginUser.getId());
        boolean result = questionService.save(question);
        ExceptionUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ApiResponse.success(question.getId());
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public ApiResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User user = userService.getCurrentUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ExceptionUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(user)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ERROR);
        }
        boolean b = questionService.removeById(id);
        return ApiResponse.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        List<JudgeCase> judgeCase = questionUpdateRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCase));
        }
        JudgeConfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        long id = questionUpdateRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ExceptionUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = questionService.updateById(question);
        return ApiResponse.success(result);
    }

    /**
     * 根据 id 获取图片（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Question> getQuestionById(long id, HttpServletRequest request) {
        ExceptionUtils.throwIf(id <= 0, ErrorCode.PARAM_ERROR);
        // 查询数据库
        Question question = questionService.getById(id);
        ExceptionUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ApiResponse.success(question);
    }

    /**
     * 根据 id 获取(封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public ApiResponse<QuestionVo> getQuestionVoById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ApiResponse.success(questionService.getQuestionVo(question, request));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest, HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ApiResponse.success(questionPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public ApiResponse<Page<QuestionVo>> listQuestionVoByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                              HttpServletRequest request) {
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ExceptionUtils.throwIf(size > 20, ErrorCode.PARAM_ERROR);
        Page<QuestionVo> questionVoPage = questionService.getQuestionVoPage(questionQueryRequest, request);
        return ApiResponse.success(questionVoPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public ApiResponse<Page<QuestionVo>> listMyQuestionVoByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                HttpServletRequest request) {
        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ExceptionUtils.throwIf(size > 20, ErrorCode.PARAM_ERROR);
        Page<QuestionVo> questionVoPage = questionService.getQuestionVoPage(questionQueryRequest, request);
        return ApiResponse.success(questionVoPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public ApiResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        List<String> tags = questionEditRequest.getTags();
        if (tags != null) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        List<JudgeCase> judgeCase = questionEditRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCase));
        }
        JudgeConfig judgeConfig = questionEditRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        // 设置编辑时间
        question.setEditTime(new Date());
        // 参数校验
        questionService.validQuestion(question, false);
        User loginUser = userService.getCurrentUser(request);
        long id = questionEditRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ExceptionUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ERROR);
        }
        boolean result = questionService.updateById(question);
        return ApiResponse.success(result);
    }

    /**
     * 获取预置标签和分类
     *
     * @return
     */
    @GetMapping("/tag_category")
    public ApiResponse<QuestionTagCategory> listQuestionTagCategory() {
        QuestionTagCategory questionTagCategory = new QuestionTagCategory();
        List<String> tagList = Arrays.asList("数组", "字符串", "链表", "栈与队列", "二叉树", "动态规范", "回溯算法", "贪心算法");
        List<String> categoryList = Arrays.asList("简单", "中等", "困难");
        questionTagCategory.setTagList(tagList);
        questionTagCategory.setCategoryList(categoryList);
        return ApiResponse.success(questionTagCategory);
    }

}