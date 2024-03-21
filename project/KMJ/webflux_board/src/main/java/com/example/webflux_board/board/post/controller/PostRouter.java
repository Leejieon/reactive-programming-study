package com.example.webflux_board.board.post.controller;

import com.example.webflux_board.board.post.controller.handler.PostHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

//@Configuration("post router v1")
public class PostRouter {

    @Bean
    public RouterFunction<?> routerPost(PostHandler handler) {
        return RouterFunctions.route()
                .GET("/posts", handler::getAll)
                .GET("/posts/{postId}", handler::getOneById)
                .POST("/posts", handler::save)
                .build();

    }
}
