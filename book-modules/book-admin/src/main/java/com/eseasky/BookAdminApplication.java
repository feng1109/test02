package com.eseasky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class BookAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookAdminApplication.class, args);
        log.info("启动成功");
    }
}
