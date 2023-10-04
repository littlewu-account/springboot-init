package com.yupi.springbootinit.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author 吴峻阳
 * @version 1.0
 */
@RestController
@RequestMapping("/queue")
@Slf4j
@Profile({"dev", "local"}) //指定生效的环境，此处指的是开发、本地环境
public class QueueController {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("/add")
    public void add(String name) {

//        threadPoolExecutor.execute(() -> {
//                log.info("任务执行中：" + name + "执行人：" + Thread.currentThread().getName());
//        });

        CompletableFuture.runAsync(() -> {
            log.info("任务执行中：" + name + "执行人：" + Thread.currentThread().getName());
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, threadPoolExecutor);

    }
}
