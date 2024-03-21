package com.example.opertaor.concat;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class CombineServiceTest {

    CombineService combineService = new CombineService();

    @Test
    public void concatTest() throws Exception {
        Flux<String> concat = combineService.concat();
        StepVerifier.create(concat)
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    public void concatWith() throws Exception {
        Flux<String> concat = combineService.concatWith();
        StepVerifier.create(concat)
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    public void mono_concatWith() throws Exception {
        Flux<String> concat = combineService.concatWithMono();
        StepVerifier.create(concat)
                .expectNext("A","B")
                .verifyComplete();
    }
    
    @Test
    public void merge() throws Exception {
        Flux<String> merge = combineService.merge();
        StepVerifier.create(merge)
                .expectNext("A","D","B","E","C","F")
                .verifyComplete();
    }

    @Test
    public void mergeWith() throws Exception {
        Flux<String> merge = combineService.mergeWith();
        StepVerifier.create(merge)
                .expectNext("A","D","B","E","C","F")
                .verifyComplete();
    }

    @Test
    public void mergeWith_Mono() throws Exception {
        Flux<String> merge = combineService.mergeWithMono();
        StepVerifier.create(merge)
                .expectNext("A","B")
                .verifyComplete();
    }

    @Test
    public void mergeSequential() throws Exception {
        Flux<String> merge = combineService.mergeSequential();
        StepVerifier.create(merge)
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    public void zipFlux() throws Exception {
        Flux<String> zip = combineService.zipFlux();

        StepVerifier.create(zip)
                .expectNext("AD","BE","CF")
                .verifyComplete();
    }

    @Test
    public void zipMultipleFlux() throws Exception {
        Flux<String> zip = combineService.zipMultipleFlux();

        StepVerifier.create(zip)
                .expectNext("AD14","BE25","CF36")
                .verifyComplete();
    }




    
    

}
