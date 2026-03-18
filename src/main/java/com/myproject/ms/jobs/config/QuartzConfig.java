package com.myproject.ms.jobs.config;

import org.springframework.boot.quartz.autoconfigure.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executors;

@Configuration
public class QuartzConfig {

    @Bean
    public SchedulerFactoryBeanCustomizer virtualThreadCustomizer() {
        return schedulerFactoryBean -> {
            // On crée un executor qui génère un nouveau Virtual Thread pour chaque tâche
            // En Java 25, c'est la méthode standard pour Project Loom
            var executor = Executors.newVirtualThreadPerTaskExecutor();

            // On injecte cet executor dans la factory Quartz de Spring
            schedulerFactoryBean.setTaskExecutor(new TaskExecutorAdapter(executor));
        };
    }
}
