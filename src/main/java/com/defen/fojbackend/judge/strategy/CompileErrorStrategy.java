package com.defen.fojbackend.judge.strategy;

import com.defen.fojbackend.judge.codesandbox.model.ExecuteCodeResponse;
import com.defen.fojbackend.judge.codesandbox.model.JudgeInfo;
import com.defen.fojbackend.model.dto.question.JudgeConfig;
import com.defen.fojbackend.model.enums.JudgeInfoMessageEnum;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 处理编译失败响应
 */
@Component
public class CompileErrorStrategy implements JudgeStrategy {


    @Override
    public JudgeInfo doJudge(ExecuteCodeResponse executeCodeResponse, List<String> inputList, List<String> expectedOutputList, JudgeConfig judgeConfig) {
        JudgeInfo judgeInfo = new JudgeInfo();

        judgeInfo.setTotal(inputList.size());
        judgeInfo.setPass(0);
        judgeInfo.setStatus(JudgeInfoMessageEnum.COMPILE_ERROR.getValue());
        judgeInfo.setMessage(executeCodeResponse.getMessage());

        return judgeInfo;
    }
}