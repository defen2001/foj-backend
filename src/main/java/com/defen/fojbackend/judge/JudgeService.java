package com.defen.fojbackend.judge;

import com.defen.fojbackend.model.entity.QuestionSubmit;

public interface JudgeService {

    QuestionSubmit doJudge(long questionSubmitId);
}
