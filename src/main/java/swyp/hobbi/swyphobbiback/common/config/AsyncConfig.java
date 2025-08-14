package swyp.hobbi.swyphobbiback.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import swyp.hobbi.swyphobbiback.common.exception.LoggingRejectedExecutionHandler;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "notificationExecute")
    public Executor notificationExecute() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Executor-");
        executor.setRejectedExecutionHandler(new LoggingRejectedExecutionHandler());
        executor.initialize();

        return executor;
    }
}
