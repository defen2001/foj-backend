package com.defen.fojbackend.judge.codesandbox.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.defen.fojbackend.exception.BusinessException;
import com.defen.fojbackend.exception.ErrorCode;
import com.defen.fojbackend.judge.codesandbox.CodeSandbox;
import com.defen.fojbackend.judge.codesandbox.model.ExecuteCodeRequest;
import com.defen.fojbackend.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 远程代码沙箱
 */
public class RemoteCodeSandbox implements CodeSandbox {

    private static final String AUTH_HEADER = "Authorization";

    private static final String AUTH_SECRET = "33a9f64d-a07a-44b4-96a3-04004b49430d";

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱 ");
        String url = "http://localhost:8090/executeCode";
        String json = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = HttpUtil.createPost(url)
                .header(AUTH_HEADER, AUTH_SECRET)
                .body(json)
                .execute()
                .body();
        if (StrUtil.isBlank(responseStr)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "executeCOde remoteSandbox error = " + responseStr);
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }
}
