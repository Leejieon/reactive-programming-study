package com.example.webflux_board.board.post.controller.handler;

import com.example.webflux_board.board.post.controller.dto.PostDto;
import com.example.webflux_board.board.post.domain.Post;
import com.example.webflux_board.board.post.service.PostService;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.schema.Server;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static com.example.webflux_board.board.post.controller.dto.PostDto.*;

@Component
@RequiredArgsConstructor
public class PostHandler {

    private final PostService postService;

    public Mono<ServerResponse> save(ServerRequest request) {
        return request.bodyToMono(PostRequest.class)
                .flatMap(postRequest -> postService.save(postRequest.getTitle(), postRequest.getContent(), postRequest.getAuthorId()))
                .flatMap(id -> ServerResponse.created(URI.create("/posts/" + id)).build())
                .log();
    }

    public Mono<ServerResponse> getAll(ServerRequest request) {
        Flux<PostResponse> all = postService.findAll()
                .flatMap(post -> Mono.just(PostResponse.toDto(post)))
                .log();

        return ServerResponse
                .ok()
                .body(all, PostResponse.class);
    }

    public Mono<ServerResponse> getOneById(ServerRequest request) {
        Long postId = Long.valueOf(request.pathVariable("postId"));
        Mono<PostResponse> oneById = postService.findOneById(postId)
                .flatMap(post -> Mono.just(PostResponse.toDto(post)));
        return ServerResponse
                .ok()
                .body(oneById,PostResponse.class);
    }
}
