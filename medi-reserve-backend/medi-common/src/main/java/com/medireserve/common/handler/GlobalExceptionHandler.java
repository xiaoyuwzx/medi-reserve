package com.medireserve.common.handler;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;
import com.medireserve.common.exception.BusinessException;
import com.medireserve.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理 Controller 层抛出的异常，返回规范的 Result 格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        // 如果有自定义 code 就用，否则使用默认错误码
        Integer code = e.getCode() != null ? e.getCode() : StatusCodeConstant.ERROR;
        return Result.error(code, e.getMessage());
    }

    // ==================== 参数校验异常（@Valid 校验失败） ====================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败：{}", errorMsg);
        return Result.error(StatusCodeConstant.PARAM_ERROR, errorMsg);
    }

    // ==================== 参数格式异常（类型转换错误） ====================
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误：{}", e.getMessage());
        return Result.error(StatusCodeConstant.PARAM_ERROR, "参数格式错误");
    }

    // ==================== JSON 解析异常 ====================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        return Result.error(StatusCodeConstant.PARAM_ERROR, "请求参数格式错误");
    }

    // ==================== 其他未知异常（兜底） ====================
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error(StatusCodeConstant.SERVER_ERROR, MessageConstant.UNKNOWN_ERROR);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResourceFound(NoResourceFoundException e) {
        // 只记录 WARN，不记录 ERROR
        log.warn("静态资源未找到: {}", e.getMessage());
        return Result.error(StatusCodeConstant.NOT_FOUND, "资源不存在");
    }

    // ==================== 处理 @Validated 参数校验异常 ====================
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMsg = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败：{}", errorMsg);
        return Result.error(StatusCodeConstant.PARAM_ERROR, errorMsg);
    }

    // ==================== Redisson 异常处理 ====================
    /**
     * 捕获 Redis 操作超时异常（锁获取超时）
     * Redisson 在等待锁超时时抛出 RedisTimeoutException
     */
    @ExceptionHandler(org.redisson.client.RedisTimeoutException.class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    public Result<Void> handleRedisTimeoutException(org.redisson.client.RedisTimeoutException e) {
        log.error("Redis 操作超时: {}", e.getMessage(), e);
        return Result.error(StatusCodeConstant.SYSTEM_BUSY, "系统繁忙，请稍后重试");
    }

    /**
     * 捕获其他 Redis 操作异常（连接失败、命令执行异常等）
     */
    @ExceptionHandler(org.redisson.client.RedisException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRedisException(org.redisson.client.RedisException e) {
        log.error("Redis 操作异常: {}", e.getMessage(), e);
        return Result.error(StatusCodeConstant.SYSTEM_ERROR, "缓存服务异常，请稍后重试");
    }

    /**
     * 捕获分布式锁获取时线程中断异常
     */
    @ExceptionHandler(InterruptedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleInterruptedException(InterruptedException e) {
        Thread.currentThread().interrupt(); // 恢复中断状态
        log.error("分布式锁获取被中断: {}", e.getMessage(), e);
        return Result.error(StatusCodeConstant.SYSTEM_BUSY, "操作被中断，请重试");
    }
}