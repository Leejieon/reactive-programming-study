package com.example.fluxTest.service;

import com.example.fluxTest.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<User> saveUser(User user);
    Flux<User> getAllUsers();
    Mono<User> getUserById(Long id);
    Mono<User> updateUser(Long id, User user);
    Mono<Void> deleteUser(Long id);
}
