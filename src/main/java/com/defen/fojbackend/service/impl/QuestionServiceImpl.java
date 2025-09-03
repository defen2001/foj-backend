package com.defen.fojbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.defen.fojbackend.exception.BusinessException;
import com.defen.fojbackend.exception.ErrorCode;
import com.defen.fojbackend.exception.ExceptionUtils;
import com.defen.fojbackend.mapper.QuestionMapper;
import com.defen.fojbackend.model.dto.question.QuestionQueryRequest;
import com.defen.fojbackend.model.entity.Question;
import com.defen.fojbackend.model.entity.User;
import com.defen.fojbackend.model.vo.QuestionVo;
import com.defen.fojbackend.model.vo.UserVo;
import com.defen.fojbackend.service.QuestionService;
import com.defen.fojbackend.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author chendefeng
 * @description 针对表【question(题目)】的数据库操作Service实现
 * @createDate 2025-09-03 10:55:02
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
        implements QuestionService {

    @Resource
    private UserService userService;

    @Override
    public void validQuestion(Question question, boolean add) {
        ExceptionUtils.throwIf(question == null, ErrorCode.PARAM_ERROR);
        String title = question.getTitle();
        String content = question.getContent();
        String category = question.getCategory();
        String tags = question.getTags();
        String answer = question.getAnswer();
        String judgeCase = question.getJudgeCase();
        String judgeConfig = question.getJudgeConfig();
        if (add) {
            ExceptionUtils.throwIf(StrUtil.hasBlank(title, content, category, tags), ErrorCode.PARAM_ERROR);
        }
        if (StrUtil.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "标题过长");
        }
        if (StrUtil.isNotBlank(content) && title.length() > 8000) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "内容过长");
        }
        if (StrUtil.isNotBlank(answer) && title.length() > 8000) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "答案过长");
        }
        if (StrUtil.isNotBlank(judgeCase) && title.length() > 8000) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "判题示例过长");
        }
        if (StrUtil.isNotBlank(judgeConfig) && title.length() > 8000) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "判题配置过长");
        }
    }

    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        String category = questionQueryRequest.getCategory();
        List<String> tags = questionQueryRequest.getTags();
        String answer = questionQueryRequest.getAnswer();
        Long userId = questionQueryRequest.getUserId();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "user_id", userId);
        queryWrapper.like(StrUtil.isNotBlank(title), "title", title);
        queryWrapper.like(StrUtil.isNotBlank(content), "content", content);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.like(StrUtil.isNotBlank(answer), "answer", answer);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 获取单个题目封装
     *
     * @param question
     * @param request
     * @return
     */
    @Override
    public QuestionVo getQuestionVo(Question question, HttpServletRequest request) {
        QuestionVo questionVo = QuestionVo.objToVo(question);
        // 关联查询用户信息
        Long userId = questionVo.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVo userVo = userService.getUserVo(user);
            questionVo.setUserVo(userVo);
        }
        return questionVo;
    }

    /**
     * 分页获取题目封装
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @Override
    public Page<QuestionVo> getQuestionVoPage(QuestionQueryRequest questionQueryRequest, HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 查询数据库
        Page<Question> picturePage = this.page(new Page<>(current, size),
                this.getQueryWrapper(questionQueryRequest));
        List<Question> questionList = picturePage.getRecords();
        Page<QuestionVo> questionVoPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVoPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionVo> questionVoList = questionList.stream().map(QuestionVo::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        questionVoList.forEach(pictureVo -> {
            Long userId = pictureVo.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVo.setUserVo(userService.getUserVo(user));
        });
        questionVoPage.setRecords(questionVoList);
        return questionVoPage;
    }
}




