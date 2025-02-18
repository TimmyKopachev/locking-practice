package org.dzmitry.kapachou.locking;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ApplicationRunner {


    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(ApplicationRunner.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
    }

}
