package com.defen.fojbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.defen.fojbackend.exception.BusinessException;
import com.defen.fojbackend.exception.ErrorCode;
import com.defen.fojbackend.exception.ExceptionUtils;
import com.defen.fojbackend.judge.JudgeService;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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

    @Resource
    @Lazy
    private JudgeService judgeService;

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
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的编程语言");
        }
        if (StrUtil.isEmpty(code)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不能提交空代码");
        }
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 7.修改题目提交数
        questionService.update(new UpdateWrapper<Question>()
                .setSql("submit_num = submit_num + 1")
                .eq("id", question.getId()));
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
        Long questionSubmitId = questionSubmit.getId();
        //执行判题服务
        CompletableFuture.runAsync(() -> {
            judgeService.doJudge(questionSubmitId);
        });
        return questionSubmitId;
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
        queryWrapper.like(StrUtil.isNotBlank(language), "language", language);
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
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @Override
    public Page<QuestionSubmitVo> getQuestionSubmitVoPage(QuestionSubmitQueryRequest questionSubmitQueryRequest, HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        // 查询数据库
        Page<QuestionSubmit> questionSubmitPage = this.page(new Page<>(current, size),
                this.getQueryWrapper(questionSubmitQueryRequest));
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVo> questionSubmitVoPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollUtil.isEmpty(questionSubmitList)) {
            return questionSubmitVoPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionSubmitVo> questionSubmitVoList = questionSubmitList.stream().map(QuestionSubmitVo::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionSubmitList.stream().map(QuestionSubmit::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        questionSubmitVoList.forEach(questionSubmitVo -> {
            Long userId = questionSubmitVo.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionSubmitVo.setUserVo(userService.getUserVo(user));
        });
        questionSubmitVoPage.setRecords(questionSubmitVoList);
        return questionSubmitVoPage;

    }
}




