package com.firstclub.membership.config;

import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;

/**
 * Configures asynchronous event handling on virtual threads. Each event listener runs on its own
 * lightweight virtual thread, giving high concurrency for fan-out work (stats update + tier
 * evaluation) without sizing a fixed platform-thread pool.
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean("eventTaskExecutor")
    public SimpleAsyncTaskExecutor eventTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("evt-");
        executor.setVirtualThreads(true);
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable ex, Method method, Object... params) ->
                log.error("Async listener {} failed", method.getName(), ex);
    }
}
