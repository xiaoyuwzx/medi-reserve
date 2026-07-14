package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 缓存刷新失败异常
 * 当热门医生排行榜缓存刷新失败时抛出
 */
public class CacheRefreshFailedException extends BusinessException {

    public CacheRefreshFailedException() {
        super(StatusCodeConstant.CACHE_REFRESH_FAILED, MessageConstant.CACHE_REFRESH_FAILED);
    }

    public CacheRefreshFailedException(String message) {
        super(StatusCodeConstant.CACHE_REFRESH_FAILED, message);
    }
}