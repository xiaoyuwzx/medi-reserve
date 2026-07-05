package com.medireserve.common.result;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;
import lombok.Data;
import java.io.Serializable;

/**
 * 后端统一返回结果
 * @param <T> 数据类型
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;   // 状态码：1成功，0和其它数字为失败
    private String msg;     // 提示信息：错误信息
    private T data;         // 返回数据

    // -------- 成功（无数据） ----------
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = StatusCodeConstant.SUCCESS;
        result.msg = MessageConstant.SUCCESS;
        return result;
    }

    // -------- 成功（带数据） ----------
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = StatusCodeConstant.SUCCESS;
        result.msg = MessageConstant.SUCCESS;
        result.data = data;
        return result;
    }

    // -------- 成功（自定义消息 + 数据） ----------
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.code = StatusCodeConstant.SUCCESS;
        result.msg = msg;
        result.data = data;
        return result;
    }

    // -------- 失败（默认错误码 0） ----------
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = StatusCodeConstant.ERROR;
        result.msg = msg;
        return result;
    }

    // -------- 失败（自定义状态码） ----------
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        return result;
    }
}