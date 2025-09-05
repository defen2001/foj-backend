package com.defen.fojbackend.judge;

import com.defen.fojbackend.judge.strategy.DefaultJudgeStrategy;
import com.defen.fojbackend.judge.strategy.JavaLanguageJudgeStrategy;
import com.defen.fojbackend.judge.strategy.JudgeContext;
import com.defen.fojbackend.judge.strategy.JudgeStrategy;
import com.defen.fojbackend.model.dto.questionsubmit.JudgeInfo;
import com.defen.fojbackend.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }
}
