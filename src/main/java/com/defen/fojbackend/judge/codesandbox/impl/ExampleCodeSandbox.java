package com.defen.fojbackend.judge.codesandbox.impl;

import com.defen.fojbackend.judge.codesandbox.CodeSandbox;
import com.defen.fojbackend.judge.codesandbox.model.ExecuteCodeRequest;
import com.defen.fojbackend.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 示例代码沙箱
 */
public class ExampleCodeSandbox implements CodeSandbox {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("示例代码沙箱");
        return null;
    }
}
