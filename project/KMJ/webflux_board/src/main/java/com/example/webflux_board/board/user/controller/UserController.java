package com.example.webflux_board.board.user.controller;

import com.example.webflux_board.board.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static com.example.webflux_board.board.user.controller.dto.UserDto.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Mono<ResponseEntity<List<UserResponse>>> getAllUsers() {
        return userService.findAll()
                .map(UserResponse::toDto)
                .collectList()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<UserResponse>> getOneUser(@PathVariable Long userId) {
        return userService.findOneById(userId)
                .map(UserResponse::toDto)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<Void>> savePost(@RequestBody Mono<UserRequest> request) {
        return request.flatMap((req) -> userService.save(req.getName(),req.getAge()))
                .thenReturn(ResponseEntity.created(URI.create("/")).build());
    }
}
