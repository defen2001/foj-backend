package com.defen.fojbackend.judge.strategy;

import com.defen.fojbackend.judge.codesandbox.model.ExecuteCodeResponse;
import com.defen.fojbackend.judge.codesandbox.model.JudgeInfo;
import com.defen.fojbackend.model.dto.question.JudgeConfig;
import com.defen.fojbackend.model.enums.JudgeInfoMessageEnum;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 处理运行失败响应
 */
@Component
public class RunErrorStrategy implements JudgeStrategy {


    @Override
    public JudgeInfo doJudge(ExecuteCodeResponse executeCodeResponse, List<String> inputList, List<String> expectedOutputList, JudgeConfig judgeConfig) {
        JudgeInfo judgeInfo = new JudgeInfo();

        judgeInfo.setTotal(inputList.size());
        int pass = executeCodeResponse.getResults().size() - 1;
        judgeInfo.setPass(pass);

        // 最后执行的输入
        judgeInfo.setInput(inputList.get(pass));
        judgeInfo.setStatus(JudgeInfoMessageEnum.RUN_ERROR.getValue());
        judgeInfo.setMessage(executeCodeResponse.getMessage());

        return judgeInfo;
    }
}