package com.lucasmedeiros.creditengine.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
@EnableAsync
class AsyncConfig {

    @Bean(name = ["taskExecutor"])
    fun simulationTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()

        executor.corePoolSize = 50
        executor.maxPoolSize = 200
        executor.queueCapacity = 15000

        executor.setThreadNamePrefix("SimulationAsync-")

        executor.setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())

        executor.setKeepAliveSeconds(60)
        executor.setAllowCoreThreadTimeOut(true)

        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(60)

        executor.initialize()
        return executor
    }
}
