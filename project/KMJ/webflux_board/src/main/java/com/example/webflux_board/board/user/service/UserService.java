package com.example.webflux_board.board.user.service;

import com.example.webflux_board.board.user.domain.User;
import com.example.webflux_board.board.user.persistence.UserRepository;
import com.example.webflux_board.board.user.persistence.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Flux<User> findAll() {
        return userRepository.findAll()
                .flatMap(userVO -> Mono.just(new User(userVO)));
    }

    public Mono<User> findOneById(Long userId) {
        return userRepository.findById(userId)
                .flatMap(userVO -> Mono.just(new User(userVO)));
    }

    public Mono<User> save(String name, int age) {
        UserVO userVO = new UserVO(name, age);
        return userRepository.save(userVO)
                .flatMap(vo -> Mono.just(new User(vo)));
    }


}
