package com.eastsoft.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableFeignClients(basePackages = "com.zmy")
@SpringBootApplication(exclude = MongoAutoConfiguration.class,scanBasePackages = {"com.zmy","com.eastsoft"})
@AutoConfigurationPackage
@ComponentScan(basePackages = {"com.zmy","com.eastsoft"})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}
