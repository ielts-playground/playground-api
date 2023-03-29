package org.ielts.playground;

import org.ielts.playground.common.constant.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

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
