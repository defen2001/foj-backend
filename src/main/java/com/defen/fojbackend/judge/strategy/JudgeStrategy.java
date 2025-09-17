package com.defen.fojbackend.judge.strategy;

import com.defen.fojbackend.judge.codesandbox.model.ExecuteCodeResponse;
import com.defen.fojbackend.judge.codesandbox.model.JudgeInfo;
import com.defen.fojbackend.model.dto.question.JudgeConfig;

import java.util.List;

/**
 * 判题策略
 */
public interface JudgeStrategy {

    /**
     * 执行判题
     */
    JudgeInfo doJudge(ExecuteCodeResponse executeCodeResponse, List<String> inputList, List<String> expectedOutputList, JudgeConfig judgeConfig);
}
