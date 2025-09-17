package com.defen.fojbackend.judge.strategy;

import com.defen.fojbackend.judge.codesandbox.model.enums.ExecuteCodeStatusEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 判题结果工厂
 */
@Component
public class JudgeStrategyFactory {

    @Resource
    private SuccessStrategy successStrategy;

    @Resource
    private CompileErrorStrategy compileErrorStrategy;

    @Resource
    private RunErrorStrategy runErrorStrategy;

    @Resource
    private TimeoutStrategy timeoutStrategy;

    public JudgeStrategy getStrategy(ExecuteCodeStatusEnum status) {
        switch (status) {
            case SUCCESS:
                return successStrategy;
            case COMPILE_ERROR:
                return compileErrorStrategy;
            case TIME_LIMIT_EXCEEDED:
                return timeoutStrategy;
            case RUNTIME_ERROR:
                return runErrorStrategy;
            default:
                return null;
        }
    }
}
