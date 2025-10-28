package vn.host;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "vn.host.config.api")
@EnableScheduling
public class UteShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(UteShopApplication.class, args);
    }
}