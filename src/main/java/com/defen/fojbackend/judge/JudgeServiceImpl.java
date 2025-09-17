package com.defen.fojbackend.judge;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.defen.fojbackend.exception.BusinessException;
import com.defen.fojbackend.exception.ErrorCode;
import com.defen.fojbackend.judge.codesandbox.CodeSandbox;
import com.defen.fojbackend.judge.codesandbox.CodeSandboxFactory;
import com.defen.fojbackend.judge.codesandbox.CodeSandboxProxy;
import com.defen.fojbackend.judge.codesandbox.model.ExecuteCodeRequest;
import com.defen.fojbackend.judge.codesandbox.model.ExecuteCodeResponse;
import com.defen.fojbackend.judge.codesandbox.model.JudgeInfo;
import com.defen.fojbackend.judge.codesandbox.model.enums.ExecuteCodeStatusEnum;
import com.defen.fojbackend.judge.strategy.JudgeStrategyFactory;
import com.defen.fojbackend.model.dto.question.JudgeCase;
import com.defen.fojbackend.model.dto.question.JudgeConfig;
import com.defen.fojbackend.model.entity.Question;
import com.defen.fojbackend.model.entity.QuestionSubmit;
import com.defen.fojbackend.model.enums.JudgeInfoMessageEnum;
import com.defen.fojbackend.model.enums.QuestionSubmitStatusEnum;
import com.defen.fojbackend.service.QuestionService;
import com.defen.fojbackend.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private JudgeStrategyFactory judgeStrategyFactory;

    @Value("${codesandbox.type:example}")
    private String codesandboxType;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 1. 传入提交的id，获取对应的题目和提交信息
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2.如果不为等待状态，抛出异常
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3.更改题目状态为"判题中"，防止重复执行
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "判题状态更新错误");
        }
        // 4.调用代码沙箱，获取执行结果
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(codesandboxType);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        // 获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        // 期望输出
        List<String> expectedOutput = judgeCaseList.stream().map(judgeCase -> judgeCase.getOutput().trim())
                .collect(Collectors.toList());
        // 判题配置
        JudgeConfig judgeConfig = JSONUtil.toBean(question.getJudgeConfig(), JudgeConfig.class);
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);

        // 5.根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeInfo judgeInfo = judgeStrategyFactory.getStrategy(ExecuteCodeStatusEnum.getEnumByValue(executeCodeResponse.getStatus()))
                .doJudge(executeCodeResponse, inputList, expectedOutput, judgeConfig);

        // 6.修改数据库中的判题结果
        boolean submitStatus = judgeInfo.getStatus().equals(JudgeInfoMessageEnum.ACCEPT.getValue());
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(
                submitStatus ? QuestionSubmitStatusEnum.SUCCESS.getValue() : QuestionSubmitStatusEnum.FAILED.getValue()
        );
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        // 7.修改题目通过数
        if (submitStatus) {
            questionService.update(new UpdateWrapper<Question>()
                    .setSql("accepted_num = accepted_num + 1")
                    .eq("id", question.getId()));
        }
        QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionId);
        return questionSubmitResult;
    }
}
