package com.example.opertaor.concat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class CombineService {


    public Flux<String> concat() {
        Flux<String> abc = Flux.just("A","B","C")
                .delayElements(Duration.ofMillis(1000))
                .log();

        Flux<String> def = Flux.just("D","E","F")
                .delayElements(Duration.ofMillis(1250))
                .log();

        return Flux.concat(abc, def);
    }

    public Flux<String> concatWith() {
        Flux<String> abc = Flux.just("A","B","C");
        Flux<String> def = Flux.just("D","E","F");
        return abc.concatWith(def);
    }

    public Flux<String> concatWithMono() {
        Mono<String> a = Mono.just("A");
        Mono<String> b = Mono.just("B");

        return a.concatWith(b);
    }

    public Flux<String> merge() {
        Flux<String> abc = Flux.just("A","B","C")
                .delayElements(Duration.ofMillis(1000))
                .log();

        Flux<String> def = Flux.just("D","E","F")
                .delayElements(Duration.ofMillis(1250))
                .log();
        return Flux.merge(abc, def);
    }

    public Flux<String> mergeWith() {
        Flux<String> abc = Flux.just("A","B","C")
                .delayElements(Duration.ofMillis(1000));

        Flux<String> def = Flux.just("D","E","F")
                .delayElements(Duration.ofMillis(1250));
        return abc.mergeWith(def);
    }

    public Flux<String> mergeWithMono() {
        Mono<String> a = Mono.just("A")
                .delayElement(Duration.ofMillis(1000));
        Mono<String> b = Mono.just("B")
                .delayElement(Duration.ofMillis(1250));
        return a.mergeWith(b);
    }

    public Flux<String> mergeSequential() {
        Flux<String> abc = Flux.just("A","B","C")
                .delayElements(Duration.ofMillis(1000))
                .log();

        Flux<String> def = Flux.just("D","E","F")
                .delayElements(Duration.ofMillis(1250))
                .log();
        return Flux.mergeSequential(abc, def);
    }

    public Flux<String> zipFlux() {
        Flux<String> abc = Flux.just("A","B","C");
        Flux<String> def = Flux.just("D","E","F");
        return Flux.zip(abc,def, (first, second) -> first + second);
    }

    public Flux<String> zipMultipleFlux() {
        Flux<String> abc = Flux.just("A","B","C").log();
        Flux<String> def = Flux.just("D","E","F").log();

        Flux<String> _123 = Flux.just("1", "2", "3").log();
        Flux<String> _456 = Flux.just("4", "5", "6").log();

        return Flux.zip(abc,def,_123,_456)
                .map((t) -> t.getT1() + t.getT2() + t.getT3() + t.getT4())
                .log();
    }

}
