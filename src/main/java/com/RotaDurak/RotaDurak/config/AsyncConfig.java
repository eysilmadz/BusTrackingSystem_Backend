package com.RotaDurak.RotaDurak.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20); //aynı anda en fazla 20 işçi thread
        executor.setMaxPoolSize(50);  //Tepe kullanımda 50’ye kadar çıkar
        executor.setQueueCapacity(500); //kuyruk kapasitesi(200 görev bekleyebilir)
        executor.setThreadNamePrefix("Async-");
        executor.initialize();

        ThreadPoolExecutor jExec = executor.getThreadPoolExecutor();
        jExec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable ex, Method method, Object... params) ->
                System.err.printf("Async error in %s: %s%n", method.getName(), ex.getMessage());
    }
}
