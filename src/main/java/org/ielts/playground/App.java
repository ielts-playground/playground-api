package org.ielts.playground;

import org.ielts.playground.common.constant.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@EnableSwagger2
@EnableCaching
@SpringBootApplication
public class App {
    @PostConstruct
    public void configureTimeZone() {
        DateTimeZone.setDefault(DateTimeZone.forID(DateTimeConstants.GMT7_TIME_ZONE_ID));
        TimeZone.setDefault(TimeZone.getTimeZone(DateTimeConstants.GMT7_TIME_ZONE_ID));
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
