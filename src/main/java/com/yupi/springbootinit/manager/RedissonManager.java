package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author 吴峻阳
 * @version 1.0
 * 专门提供RedisLimit的功能服务
 */

@Service
public class RedissonManager {
    @Resource
    private RedissonClient redissonClient;

    //key用来区分不同的限流器
    public void doRateLimit(String key) {
        //根据传入的key创建一个限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        //设置限流器的参数： 限流作用域（全局）、限流数：2个、限流时间窗口：1s、限流时间单位：秒
        rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);
        boolean token = rateLimiter.tryAcquire(1);
        if(!token) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
