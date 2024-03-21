package com.example.opertaor.map;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class MapOperatorTestService {

    public Flux<String> namesFluxUpperCase() {
        return Flux.just("adam" , "ben" , "chloe")
                .map(String::toUpperCase)
                .log();
    }

    public Flux<String> namesFluxUpperCaseLongerThan(int length) {
        return Flux.just("adam" , "ben" , "chloe")
                .map(String::toUpperCase)
                .filter(name -> length < name.length())
                .log();
    }

    public Flux<String> nameFlux_Immutable() {
        var namesFlux = Flux.just("adam","ben","chloe");
        Flux<String> map = namesFlux.map(String::toUpperCase)
                .map((name) -> {
                    System.out.println("name = " + name);
                    return name;
                }).log();
        Flux<String> filter = namesFlux.filter((name) -> name.length() >  3)
                .map((name) -> {
                    System.out.println("name = " + name);
                    return name;
                }).log();
        return namesFlux;
    }

    public Flux<String> namesFlux_flatMap() {
        return Flux.just("adam", "chloe")
                .flatMap((name) -> Flux.fromArray(name.split("")))
                .map(String::toUpperCase)
                .log();
    }

    public Flux<String> namesFlux_flatMapWithDelay() {
        return Flux.just("adam" , "chloe")
                .map(String::toUpperCase)
                .flatMap((name -> {
//                    var rand = new Random().nextInt(1000);
                    var rand = 1000;
                    return Flux.fromArray(name.split(""))
                            .delayElements(Duration.ofMillis(rand));
                })).log();
    }

    public Flux<String> namesFlux_concatMapWithDelay() {
        return Flux.just("adam" , "chloe")
                .map(String::toUpperCase)
                .concatMap((name -> {
//                    var rand = new Random().nextInt(1000);
                    var rand = 1000;
                    return Flux.fromArray(name.split(""))
                            .delayElements(Duration.ofMillis(rand));
                })).log();
    }

    public Flux<String> namesFlux_flatMapMany() {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .flatMapMany(name -> Flux.just(name.split("")))
                .log();
    }

    public Flux<String> namesFlux_transform(int stringLength) {

        Function<Flux<String>, Flux<String>> functionalInterface = (name) -> {
            return name.filter(s -> s.length() > stringLength)
                    .map(String::toUpperCase)
                    .log();
        };

        return Flux.fromIterable(List.of("adam","ben","chloe"))
                .transform(functionalInterface)
                .flatMap(s -> Flux.just(s.split("")))
                .log();
    }

    public Mono<List<String>> namesMono_flatMap() {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .flatMap(this::splitStringMono)
                .log();
    }


    public Flux<String> namesFlux_defaultIfEmpty(int stringLength) {
        Function<Flux<String>, Flux<String>> functionalInterface = (name) -> {
            return name.filter(s -> s.length() > stringLength)
                    .map(String::toUpperCase)
                    .log();
        };

        return Flux.fromIterable(List.of("adam","ben","chloe"))
                .transform(functionalInterface)
                .flatMap(s -> Flux.just(s.split("")))
                .defaultIfEmpty("default")
                .log();
    }

    public Flux<String> namesFlux_switchIfEmpty(int stringLength) {
        Function<Flux<String>, Flux<String>> functionalInterface = (name) -> {
            return name.filter(s -> s.length() > stringLength)
                    .map(String::toUpperCase)
                    .log();
        };

        Flux<String> defaultFlux = Flux.just("default")
                .map(String::toUpperCase)
                .log();

        return Flux.fromIterable(List.of("adam","ben","chloe"))
                .transform(functionalInterface)
                .switchIfEmpty(defaultFlux)
                .flatMap(s -> Flux.just(s.split("")))
                .log();
    }

    public Mono<List<String>> splitStringMono(String s) {
        String[] charArray = s.split("");
        return Mono.just(List.of(charArray));
    }

}
