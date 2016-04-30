package app;

import app.service.GridService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;


@EnableCaching
@SpringBootApplication
@EnableScheduling
@Configuration
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class);
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
