package org.dzmitry.kapachou.locking;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ApplicationRunner {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(ApplicationRunner.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
    }

}
