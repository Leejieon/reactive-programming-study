package com.example.webflux_board;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "Webflux board", version = "1.0", description = "스터디용"))
@SpringBootApplication
public class WebfluxBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebfluxBoardApplication.class, args);
    }

}
