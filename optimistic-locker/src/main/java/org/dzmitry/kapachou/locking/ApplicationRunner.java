package org.dzmitry.kapachou.locking;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication

public class ApplicationRunner implements org.springframework.boot.ApplicationRunner
{

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(ApplicationRunner.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

    }
}
