package app;

import app.service.GridService;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@EnableCaching
@SpringBootApplication
@EnableScheduling
@Configuration
public class Application {

    public static void main(String[] args) throws Exception {
       SpringApplication.run(Application.class);
    }

    @Bean(destroyMethod = "shutdown")
    public Executor taskScheduler() {
        return Executors.newScheduledThreadPool(5);
    }

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        GuavaCache guavaCache1 = new GuavaCache("holidayCache", CacheBuilder.newBuilder()
                .maximumSize(5)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .expireAfterAccess(10, TimeUnit.MINUTES).build());
        GuavaCache guavaCache2 = new GuavaCache("gridCache", CacheBuilder.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .expireAfterAccess(60, TimeUnit.MINUTES).build());
        cacheManager.setCaches(Arrays.asList(guavaCache1, guavaCache2));
        return cacheManager;
    }

    private static final Logger log = LoggerFactory.getLogger(Application.class);

 /*   @Component
    static class Runner implements CommandLineRunner {
        @Autowired
        private GridService gridService;

        @Override
        public void run(String... args) throws Exception {
            log.info(".... Fetching books");
            log.info("isbn-1234 -->" + gridService.findAll());
            log.info("isbn-1234 -->" + gridService.findAll());
            log.info("isbn-1234 -->" + gridService.findAll());
            log.info("isbn-1234 -->" + gridService.findAll());
        }
    }*/

}
