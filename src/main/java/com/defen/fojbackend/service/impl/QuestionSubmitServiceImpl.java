package com.defen.fojbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.defen.fojbackend.exception.BusinessException;
import com.defen.fojbackend.exception.ErrorCode;
import com.defen.fojbackend.exception.ExceptionUtils;
import com.defen.fojbackend.mapper.QuestionSubmitMapper;
import com.defen.fojbackend.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.defen.fojbackend.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.defen.fojbackend.model.entity.Question;
import com.defen.fojbackend.model.entity.QuestionSubmit;
import com.defen.fojbackend.model.entity.User;
import com.defen.fojbackend.model.enums.QuestionSubmitLanguageEnum;
import com.defen.fojbackend.model.enums.QuestionSubmitStatusEnum;
import com.defen.fojbackend.model.vo.QuestionSubmitVo;
import com.defen.fojbackend.service.QuestionService;
import com.defen.fojbackend.service.QuestionSubmitService;
import com.defen.fojbackend.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chendefeng
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2025-09-03 10:55:18
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        String language = questionSubmitAddRequest.getLanguage();
        String code = questionSubmitAddRequest.getCode();
        Long questionId = questionSubmitAddRequest.getQuestionId();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "编程语言错误");
        }
        if (StrUtil.isEmpty(code)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不能提交空代码");
        }
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        Long userId = loginUser.getId();
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setLanguage(language);
        questionSubmit.setCode(code);
        questionSubmit.setJudgeInfo("{}");
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setUserId(userId);
        boolean result = this.save(questionSubmit);
        ExceptionUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "操作失败");
        return questionSubmit.getId();
    }

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(StrUtil.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtil.isNotEmpty(userId), "user_id", userId);
        queryWrapper.eq(ObjectUtil.isNotEmpty(questionId), "question_id", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 获取题目提交封装
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    @Override
    public QuestionSubmitVo getQuestionSubmitVo(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVo questionSubmitVO = QuestionSubmitVo.objToVo(questionSubmit);
        //仅本人和管理员能看见自己的答案
        Long userId = loginUser.getId();
        //处理脱敏
        if (questionSubmit.getUserId().equals(userId) && !userService.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    /**
     * 分页获取题目提交封装
     *
     * @param questionSubmitPage
     * @param loginUser
     * @return
     */
    @Override
    public Page<QuestionSubmitVo> getQuestionSubmitVoPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVo> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollUtil.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        List<QuestionSubmitVo> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit ->
                getQuestionSubmitVo(questionSubmit, loginUser)
        ).collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;

    }
}




