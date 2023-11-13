package es.e1sordo.worknote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WorknoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorknoteApplication.class, args);
    }
}
