package com.example.opertaor.map;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

public class MapOperatorTest {

    private MapOperatorTestService mapOperatorTestService = new MapOperatorTestService();


    @Test
    public void map_uppercase() throws Exception {

        Flux<String> namesFlux = mapOperatorTestService.namesFluxUpperCase();


        StepVerifier.create(namesFlux)
                .expectNext("ADAM","BEN","CHLOE")
                .verifyComplete();
    }


    @Test
    public void map_uppercase_length3() throws Exception {
        Flux<String> namesFlux = mapOperatorTestService.namesFluxUpperCaseLongerThan(3);

        StepVerifier.create(namesFlux)
                .expectNext("ADAM","CHLOE")
                .verifyComplete();
    }

    @Test
    public void reactive_streams_is_immutable() throws Exception {
        Flux<String> stringFlux = mapOperatorTestService.nameFlux_Immutable();

        StepVerifier.create(stringFlux)
                .expectNext("adam","ben","chloe")
                .verifyComplete();

        Flux<String> map = stringFlux.map(String::toUpperCase)
                .map((name) -> {
                    System.out.println("name = " + name);
                    return name;
                }).log();

        Flux<String> filter = stringFlux.filter((name) -> name.length() >  3)
                .map((name) -> {
                    System.out.println("name = " + name);
                    return name;
                }).log();

        StepVerifier.create(map)
                .expectNext("ADAM","BEN","CHLOE")
                .verifyComplete();

        StepVerifier.create(filter)
                .expectNext("adam","chloe")
                .verifyComplete();

    }

    @Test
    public void flatMap_is_flatten_flux_element() throws Exception {
        Flux<String> namesFlux = mapOperatorTestService.namesFlux_flatMap();

        StepVerifier.create(namesFlux)
                .expectNext("A","D","A","M","C","H","L","O","E")
                .verifyComplete();
    }
    
    @Test
    public void flatMap_is_async_operator() throws Exception {
        Flux<String> namesFlux = mapOperatorTestService.namesFlux_flatMapWithDelay();

//        StepVerifier.create(namesFlux)
//                .expectNext("A","D","A","M","C","H","L","O","E")
//                .verifyComplete();
        // 5 seconds
        StepVerifier.create(namesFlux)
                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    public void concatMap_using_ordering_matter() throws Exception {
        Flux<String> namesFlux = mapOperatorTestService.namesFlux_concatMapWithDelay();
        // 9 seconds
        StepVerifier.create(namesFlux)
                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    public void flatMap_Mono() throws Exception {
        Mono<List<String>> namesFlux = mapOperatorTestService.namesMono_flatMap();
        // 9 seconds
        StepVerifier.create(namesFlux)
                .expectNext(List.of("A","L","E","X"))
                .verifyComplete();
    }
    
    @Test
    public void flatMapMany_is_Mono_to_Flux() throws Exception {
        var namesFlux = mapOperatorTestService.namesFlux_flatMapMany();

        StepVerifier.create(namesFlux)
                .expectNext("A","L","E","X")
                .verifyComplete();
    }
    
    @Test
    public void transform() throws Exception {
        var nameFlux = mapOperatorTestService.namesFlux_transform(3);

        StepVerifier.create(nameFlux)
                .expectNext("A","D","A","M","C","H","L","O","E")
                .verifyComplete();
    }
    
    @Test
    public void defaultIsEmpty_is_change_Flux_of_default() throws Exception {
        var nameFlux = mapOperatorTestService.namesFlux_defaultIfEmpty(6);

        StepVerifier.create(nameFlux)
                .expectNext("default")
                .verifyComplete();
    }

    @Test
    public void switchIsEmpty_is_change_Flux() throws Exception {
        var nameFlux = mapOperatorTestService.namesFlux_switchIfEmpty(6);

        StepVerifier.create(nameFlux)
                .expectNext("D","E","F","A","U","L","T")
                .verifyComplete();
    }

}
