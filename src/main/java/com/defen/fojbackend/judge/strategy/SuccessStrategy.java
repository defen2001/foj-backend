package com.defen.fojbackend.judge.strategy;

import cn.hutool.core.util.StrUtil;
import com.defen.fojbackend.judge.codesandbox.model.ExecuteCodeResponse;
import com.defen.fojbackend.judge.codesandbox.model.ExecuteMessage;
import com.defen.fojbackend.judge.codesandbox.model.JudgeInfo;
import com.defen.fojbackend.model.dto.question.JudgeConfig;
import com.defen.fojbackend.model.enums.JudgeInfoMessageEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理c成功响应
 */
@Component
public class SuccessStrategy implements JudgeStrategy {


    @Override
    public JudgeInfo doJudge(ExecuteCodeResponse executeCodeResponse, List<String> inputList, List<String> expectedOutputList, JudgeConfig judgeConfig) {
        JudgeInfo judgeInfo = new JudgeInfo();

        int total = inputList.size();
        judgeInfo.setTotal(total);

        // 测试用例详情信息
        List<ExecuteMessage> results = executeCodeResponse.getResults();
        // 时间输出
        List<String> outputList = results.stream().map(executeResult -> executeResult.getMessage().trim())
                .collect(Collectors.toList());
        // 记录通过的测试用例
        int pass = 0;
        // 记录最大执行时间
        long maxTime = Long.MIN_VALUE;
        // 记录最大内存占用
        long maxMemory = Long.MIN_VALUE;
        for (int i = 0; i < total; i++) {
            // 判断执行时间
            Long time = results.get(i).getTime();
            if (time > maxTime) {
                maxTime = time;
            }
            // 判断内存占用
            Long memory = results.get(i).getMemory();
            if (memory > maxMemory) {
                maxMemory = memory;
            }
            if (StrUtil.equals(expectedOutputList.get(i), outputList.get(i))) {
                // 超时
                if (maxTime > judgeConfig.getTimeLimit()){
                    judgeInfo.setPass(pass);
                    judgeInfo.setInput(inputList.get(i));
                    judgeInfo.setStatus(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue());
                    judgeInfo.setMessage(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getText());
                } else if (maxMemory > judgeConfig.getMemoryLimit() * 1024 * 1024) {
                    // 内存超出
                    judgeInfo.setInput(inputList.get(i));
                    judgeInfo.setPass(pass);
                    judgeInfo.setStatus(JudgeInfoMessageEnum.RUN_ERROR.getValue());
                    judgeInfo.setMessage("超出内存限制");
                } else {
                    pass++;
                }
            } else {
                // 遇到一个没通过的
                judgeInfo.setPass(pass);
                judgeInfo.setStatus(JudgeInfoMessageEnum.WRONG_ANSWER.getValue());
                judgeInfo.setMessage(JudgeInfoMessageEnum.WRONG_ANSWER.getText());
                // 设置输出和预期输出信息
                judgeInfo.setInput(inputList.get(i));
                judgeInfo.setOutput(StrUtil.trim(outputList.get(i)));
                judgeInfo.setExpectedOutput(expectedOutputList.get(i));
                return judgeInfo;
            }
        }
        judgeInfo.setPass(total);
        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(maxMemory);
        judgeInfo.setStatus(JudgeInfoMessageEnum.ACCEPT.getValue());
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPT.getText());

        return judgeInfo;
    }
}