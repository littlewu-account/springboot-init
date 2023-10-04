package com.yupi.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 吴峻阳
 * @version 1.0
 */

@SpringBootTest
class RedissonManagerTest {

    @Resource
    private RedissonManager redissonManager;

    @Test
    void doRateLimit() {
        String userId = "1";
        for(int i = 0; i < 5; i++) {
            redissonManager.doRateLimit(userId);
            System.out.println("成功");
        }

    }
}