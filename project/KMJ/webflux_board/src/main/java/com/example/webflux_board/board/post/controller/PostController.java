package com.example.webflux_board.board.post.controller;

import com.example.webflux_board.board.post.controller.dto.PostDto;
import com.example.webflux_board.board.post.controller.dto.PostDto.PostRequest;
import com.example.webflux_board.board.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @GetMapping
    public Mono<ResponseEntity<List<PostDto.PostResponse>>> getAllPost() {
        return postService.findAll()
                .map(PostDto.PostResponse::toDto)
                .collectList()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{postId}")
    public Mono<ResponseEntity<PostDto.PostResponse>> getPostById(@PathVariable Long postId) {
        return postService.findOneById(postId)
                .map(PostDto.PostResponse::toDto)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<Void>> savePost(@RequestBody Mono<PostRequest> request) {
        return request.flatMap((req) -> postService.save(req.getTitle(),req.getContent(),req.getAuthorId()))
                .flatMap(post -> Mono.just(ResponseEntity.created(URI.create("/posts/" + post.getPostInfo().getId())).build()));
    }
}
