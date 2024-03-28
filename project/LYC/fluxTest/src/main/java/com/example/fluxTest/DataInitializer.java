package com.example.fluxTest;

import com.example.fluxTest.model.User;
import com.example.fluxTest.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class DataInitializer {
//
//    @Bean
//    CommandLineRunner init(UserRepository userRepository) {
//        return args -> {
//            Flux<User> userFlux = Flux.range(1, 10000)
//                    .map(i -> new User("User" + i, "user" + i + "@example.com"))
//                    .flatMap(userRepository::save);
//
//            userRepository.deleteAll()
//                    .thenMany(userFlux)
//                    .thenMany(userRepository.findAll())
//                    .subscribe(user -> System.out.println("Inserted User: " + user.getName()));
//        };
//    }
}
