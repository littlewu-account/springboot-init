package com.yupi.springbootinit.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @author 吴峻阳
 * @version 1.0
 * public ThreadPoolExecutor(int corePoolSize,
 *                               int maximumPoolSize,
 *                               long keepAliveTime,
 *                               TimeUnit unit,
 *                               BlockingQueue<Runnable> workQueue,
 *                               RejectedExecutionHandler handler) {
 *         this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
 *              Executors.defaultThreadFactory(), handler);
 *     }
 */
@Configuration
public class ThreadPoolExecutorConfig {


    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {

        //ThreadFactory用于定制生成的线程，例如名字等属性
        ThreadFactory threadFactory = new ThreadFactory() {

            private int count = 1;

            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程" + count);
                count++;
                return thread;
            }
        };

        ThreadPoolExecutor threadPoolExecutor
                = new ThreadPoolExecutor(2, 4, 100L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), threadFactory);

        return threadPoolExecutor;
    }
}
