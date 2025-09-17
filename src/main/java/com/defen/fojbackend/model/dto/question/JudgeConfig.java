package com.defen.fojbackend.model.dto.question;

import lombok.Data;

@Data
public class JudgeConfig {

    /**
     * 时间限制
     */
    private Long timeLimit;

    /**
     * 内存限制
     */
    private Long memoryLimit;
}