package org.openapitools.config;

import java.util.concurrent.Executor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Test configuration for async operations and thread pools. This configuration is used across all
 * test types to ensure consistent async behavior and proper thread pool management.
 */
@TestConfiguration
@EnableAsync
public class TestAsyncConfig {

  /**
   * Primary task executor for async operations in tests. Configured with smaller pool sizes for
   * test efficiency.
   */
  @Bean
  @Primary
  @Profile("test")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("test-async-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(5);
    executor.initialize();
    return executor;
  }

  /**
   * Task executor specifically for performance tests. Configured with larger pool sizes to handle
   * concurrent load.
   */
  @Bean("performanceTaskExecutor")
  @Profile("performance")
  public Executor performanceTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(200);
    executor.setThreadNamePrefix("perf-async-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(10);
    executor.initialize();
    return executor;
  }
}
