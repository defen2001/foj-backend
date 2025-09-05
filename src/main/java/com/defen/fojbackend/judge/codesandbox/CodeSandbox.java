package com.defen.fojbackend.judge.codesandbox;

import com.defen.fojbackend.judge.codesandbox.model.ExecuteCodeRequest;
import com.defen.fojbackend.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);

    // 添加查看代码沙箱状态的接口
}