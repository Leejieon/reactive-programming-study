# Testing

Reactor에서 테스트 방식은 Flux 또는 Mono를 Sequence로 정의 후, 구독 시점에 해당 Operator 체인이 시나리오대로 동작하는지 테스트 하는 것 입니다. 이 테스트를 통해 다음에 발생할 Signal이 무엇인지, 기대하던 데이터들이 emit되었는지, 특정 시간 동안 emit된 데이터가 있는지 등을 단계적으로 테스트 할 수 있습니다.

이를 위해 StepVerfier라는 API를 사용합니다.

- Signal 이벤트 테스트
    
    가장 기본적인 테스트 방식으로 Reactor Sequence에서 발생하는 Signal 이벤트를 테스트 하는 것 입니다.
    
    ```java
    import org.junit.jupiter.api.Test;
    import reactor.core.publisher.Mono;
    import reactor.test.StepVerifier;
    
    import static org.junit.jupiter.api.Assertions.*;
    
    class StepVerifierGeneralExample01TestTest {
    
        @Test
        public void sayHelloReactorTest() {
            StepVerifier
                    .create(Mono.just("Hello Reactor"))
                    .expectNext("Hello Reactor")
                    .expectComplete()
                    .verify();
        }
    }
    ```
    
    - StepVerifier API를 이용한 테스트 방법
        1. create()을 통해 테스트 대상 Sequence를 생성합니다.
        2. expectXXXX()를 통해 Sequence에서 예상되는 Signal의 기댓값을 평가합니다.
        3. verfiy()를 호출함으로써 전체 Operator 체인의 테스트를 트리거합니다.
    
    - expectXXX() 메서드 종류
        
        
        | 메서드 | 설명 |
        | --- | --- |
        | expectSubscription() | 리액티브 스트림이 예상대로 구독이 이루어졌는지 기대한다. |
        | expectNext(T t) | onNext Signal을 통해 전달되는 값이 파라미터로 전달된 값과 같음을 기대한다. |
        | expectComplete() | onComplete Signal이 전송되기를 기대한다. |
        | expectError() | onError Signal이 전송되기를 기대한다. |
        | expectNextCount(long count) | 구독 시점 또는 이전 expectNext()를 통해 기댓값이 평가된 데이터 이후부터 emit된 수를 기대한다. |
        | expectNoEvent(Duration d) | 주어진 시간 동안 Signal 이벤트가 발생하지 않았음을 기대한다. |
        | expectAccessibleContext() | 구독 시점 이후에 Context가 전파되었음을 기대한다. |
        | expectNextSequence(Iterable <? extends T> iterable | emit된 데이터들이 파라미터로 전달된 iterable의 요소와 매치됨을 기대한다. |
    - verifyXXX() 메서드 종류
        
        
        | 메서드 | 설명 |
        | --- | --- |
        | verify() | 검증을 트리거한다. |
        | verifyComplete() | 검증을 트리거하고, onComplete Signal을 기대한다. |
        | verifyError() | 검증을 트리거하고, onError Signal을 기대한다. |
        | verifyTimeout(Duration d) | 검을을 트리거하고, 주어진 시간이 초과되어도 Publisher가 종료되지 않음을 기대한다. |
        
        여기서 검증을 트리거한다는 게 무엇일까?
        
        질문, 왜 마지막 코드에서 검증을 트리거할까? 트리거라는 것은 방아쇠를 당겨 시작한다는 건데, 앞에서 expect()를 통해 예측을 하는데, 마지막엔 검증을 종료하는 것이 아닌 왜 시작인지 궁금
        
    
    ```java
    import reactor.core.publisher.Flux;
    
    public class GeneralTestExample {
        public static Flux<String> sayHello() {
            return Flux
                    .just("Hello", "Reactor");
        }
    }
    
    import org.junit.jupiter.api.Test;
    import reactor.test.StepVerifier;
    
    public class ExampleTest13_3 {
    
        @Test
        public void sayHelloTest() {
            StepVerifier
                    .create(GeneralTestExample.sayHello())
                    .expectSubscription()
                    .as("# expect subscription")
                    .expectNext("Hi")
                    .as("# expect Hi")
                    .expectNext("Reactor")
                    .as("# expect Reactor")
                    .verifyComplete();
        }
    }
    
    //
    java.lang.AssertionError: expectation "# expect Hi" failed (expected value: Hi; actual value: Hello)
    	at reactor.test.MessageFormatter.assertionError(MessageFormatter.java:115)
    	at reactor.test.MessageFormatter.failPrefix(MessageFormatter.java:104)
    	at reactor.test.MessageFormatter.fail(MessageFormatter.java:73)
    	at reactor.test.MessageFormatter.failOptional(MessageFormatter.java:88)
    	at reactor.test.DefaultStepVerifierBuilder.lambda$addExpectedValue$10(DefaultStepVerifierBuilder.java:512)
    	at reactor.test.DefaultStepVerifierBuilder$SignalEvent.test(DefaultStepVerifierBuilder.java:2289)
    	at reactor.test.DefaultStepVerifierBuilder$DefaultVerifySubscriber.onSignal(DefaultStepVerifierBuilder.java:1529)
    	at reactor.test.DefaultStepVerifierBuilder$DefaultVerifySubscriber.onExpectation(DefaultStepVerifierBuilder.java:1477)
    	at reactor.test.DefaultStepVerifierBuilder$DefaultVerifySubscriber.onNext(DefaultStepVerifierBuilder.java:1146)
    	at reactor.core.publisher.FluxArray$ArraySubscription.fastPath(FluxArray.java:171)
    	at reactor.core.publisher.FluxArray$ArraySubscription.request(FluxArray.java:96)
    	at reactor.test.DefaultStepVerifierBuilder$DefaultVerifySubscriber.onSubscribe(DefaultStepVerifierBuilder.java:1161)
    	at reactor.core.publisher.FluxArray.subscribe(FluxArray.java:53)
    	at reactor.core.publisher.FluxArray.subscribe(FluxArray.java:59)
    	
    ```
    
    위의 코드는 sayHello() 메서드를 테스트하는 예제입니다. as() 메서드가 보이는데 as()를 사용해서 이전 기댓값 평가 단계에 대한 설명을 추가할 수 있습니다. 이 로그는 실패하게 될 경우 실패한 단게에 해당하는 설명이 로그로 출력됩니다.
    
    현재 Hello가 emit되지만 기대하는 것은 Hi 이기에, 테스트를 실패합니다!
    
    ```java
    import reactor.core.publisher.Flux;
    
    public class GeneralTestExample {
       
        public static Flux<Integer> divideByTwo(Flux<Integer> source) {
            return source
                    .zipWith(Flux.just(2, 2, 2, 2, 0), (x, y) -> x/y);
        }
    }
    
    import org.junit.jupiter.api.Test;
    import reactor.core.publisher.Flux;
    import reactor.test.StepVerifier;
    
    public class ExampleTest13_4 {
    
        @Test
        public void divideByTwoTest() {
            Flux<Integer> source = Flux.just(2,4,6,8,10);
            StepVerifier
                    .create(GeneralTestExample.divideByTwo(source))
                    .expectSubscription()
                    .expectNext(1)
                    .expectNext(2)
                    .expectNext(3)
                    .expectNext(4)
                    .expectError()
                    .verify();
        }
    }
    ```
    
    위의 코드느 파라미터로 전달받은 Source Flux에서 emit되는 각각의 데이터를 2로 나누는 작업을 수행한 결과 값을 emit하게 됩니다. 마지막에 emit된 데이터느 2가 아닌 0으로 나누는 작업을 했기 때문에 예외가 발생합니다. 하지만 expectError()를 통해 테스트에 통과합니다.
    
    ```java
    import reactor.core.publisher.Flux;
    
    public class GeneralTestExample {
       
        public static Flux<Integer> takeNumber(Flux<Integer> source, long n) {
            return source
                    .take(n);
        }
    }
    
    import org.junit.jupiter.api.Test;
    import reactor.core.publisher.Flux;
    import reactor.test.StepVerifier;
    import reactor.test.StepVerifierOptions;
    
    public class Example13_5 {
    
        @Test
        public void takeNumberTest() {
            Flux<Integer> source = Flux.range(0,1000);
            StepVerifier
                    .create(GeneralTestExample.takeNumber(source, 500),
                            StepVerifierOptions.create().scenarioName("Verify from 0 to 499"))
                    .expectSubscription()
                    .expectNext(0)
                    .expectNextCount(498)
                    .expectNext(500)
                    .expectComplete()
                    .verify();
        }
    }
    
    //
    java.lang.AssertionError: [Verify from 0 to 499] expectation "expectNext(498)" failed (expected value: 498; actual value: 1)
    	at reactor.test.MessageFormatter.assertionError(MessageFormatter.java:115)
    	at reactor.test.MessageFormatter.failPrefix(MessageFormatter.java:104)
    	at reactor.test.MessageFormatter.fail(MessageFormatter.java:73)
    	at reactor.test.MessageFormatter.failOptional(MessageFormatter.java:88)
    ```
    
    takeNumber() 메서드는 Source Flux에서 파라미처로 전달된 숫자의 개수만큼만 데이터를 emit하는 메서드입니다.
    
    StepVerifierOptions는 이름 그대로 StepVerifier에 옵션, 즉 추가적 기능을 덧붙이는 작업을 하는 클래스인데, 예제 코드에서는 테스트에 실패할 경우 파라미터로 입력한 시나리오명을 출력합니다.
    
    위 테스트는 0부터 500을 emit합니다. 여기서 expectNextCount로 498깨의 숫자가 emit됨을 기대합니다. 그렇다면 현재 0부터 시작하니 총 emit된 데이터는 499개입니다. 이제 1개의 데이터만 emit되면 되는데 498다음은 499이지만 500을 예상함으로 오류를 발생합니다.
    
- 시간 기반(Time-based) 테스트
    
    SetpVerifier는 가상의 시간을 이용해 미래에 실행되는 Sequence의 시간을 앞당겨 테스트할 수 있는 기능을 지원합니다.
    
    ```java
    import reactor.core.publisher.Flux;
    import reactor.util.function.Tuple2;
    import reactor.util.function.Tuples;
    
    public class TimeBasedTestExample {
        public static Flux<Tuple2<String, Integer>> getCOVID19Count(Flux<Long> source) {
            return source
                    .flatMap(notUse -> Flux.just(
                                    Tuples.of("서울", 10),
                                    Tuples.of("경기도", 5),
                                    Tuples.of("강원도", 3),
                                    Tuples.of("충청도", 6),
                                    Tuples.of("경상도", 5),
                                    Tuples.of("전라도", 8),
                                    Tuples.of("인천", 2),
                                    Tuples.of("대전", 1),
                                    Tuples.of("대구", 2),
                                    Tuples.of("부산", 3),
                                    Tuples.of("제주도", 0)
                            )
                    );
        }
    }
    
    import org.junit.jupiter.api.Test;
    import reactor.core.publisher.Flux;
    import reactor.test.StepVerifier;
    import reactor.test.scheduler.VirtualTimeScheduler;
    
    import java.time.Duration;
    
    public class Example13_7 {
    
        @Test
        public void getCOVID19CountTest() {
            StepVerifier
                    .withVirtualTime(() -> TimeBasedTestExample.getCOVID19Count(
                            Flux.interval(Duration.ofHours(1)).take(1)
                    ))
                    .expectSubscription()
                    .then(() -> VirtualTimeScheduler
                            .get()
                            .advanceTimeBy(Duration.ofHours(1)))
                    .expectNextCount(11)
                    .expectComplete()
                    .verify();
        }
    }
    ```
    
    이번 테스트의 목적은 현재 시점에서 1시간 뒤에 COVID-19 확진자 발생 현황을 체크하고자 하는데 테스트
    
    대상 메서드의 Sequence가 1시간 뒤에 실제로 동작하는지 확인하는 것 입니다.
    
    withVirtualTime() 메서드는 VirtualTimeScheduler라는 가상 스케줄러의 제어를 받도록 해줍니다. 따라서 구독에 대한 기댓값을 평가하고 난 후 then() 메서드를 사용해 후속 작업을 할 수 있도록 하는데, 여기서 VirtualTimeScheduler의 advamceTimeBy()를 이용해 시간을 1시간 당기는 작업을 수행합니다.
    
    현재 위의 데이터는 11개 이기에 테스트는 통과합니다.
    
    ```java
    import reactor.core.publisher.Flux;
    import reactor.util.function.Tuple2;
    import reactor.util.function.Tuples;
    
    public class TimeBasedTestExample {
    
        public static Flux<Tuple2<String, Integer>> getVoteCount(Flux<Long> source) {
            return source
                    .zipWith(Flux.just(
                                    Tuples.of("중구", 15400),
                                    Tuples.of("서초구", 20020),
                                    Tuples.of("강서구", 32040),
                                    Tuples.of("강동구", 14506),
                                    Tuples.of("서대문구", 35650)
                            )
                    )
                    .map(Tuple2::getT2);
        }
    }
    
    import org.junit.jupiter.api.Test;
    import reactor.core.publisher.Flux;
    import reactor.test.StepVerifier;
    
    import java.time.Duration;
    
    public class Example13_8 {
    
        @Test
        public void getCOVID19CountTest() {
            StepVerifier
                    .create(TimeBasedTestExample.getCOVID19Count(
                            Flux.interval(Duration.ofMinutes(1)).take(1)
                    ))
                    .expectSubscription()
                    .expectNextCount(11)
                    .expectComplete()
                    .verify(Duration.ofSeconds(3));
        }
    }
    
    //
    java.lang.AssertionError: VerifySubscriber timed out on reactor.core.publisher.FluxFlatMap$FlatMapMain@7502291e
    	at reactor.test.MessageFormatter.assertionError(MessageFormatter.java:115)
    	at reactor.test.DefaultStepVerifierBuilder$DefaultVerifySubscriber.pollTaskEventOrComplete(DefaultStepVerifierBuilder.java:1728)
    	at reactor.test.DefaultStepVerifierBuilder$DefaultVerifySubscriber.verify(DefaultStepVerifierBuilder.java:1298)
    	at reactor.test.DefaultStepVerifierBuilder$DefaultStepVerifier.verify(DefaultStepVerifierBuilder.java:832)
    	at com.example.reactive.Testing.Example13_8.getCOVID19CountTest(Example13_8.java:20)
    	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
    	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    	at java.base/java.lang.reflect.Method.invoke(Method.java:568)
    ```
    
    위의 코드는 다른 부분과 달리 검증을 시작할 때 3초의 시간을 지정하였습니다. 이는 3초 내에 기댓값의 평가가 끝나지 않으면 시간 초과로 간주하겠다는 의미입니다.
    
    위 코드는 3초 이내에 이루어지기 원하지만 1분 뒤에 데이터가 emit됨으로 시간초과로 에러가 발생합니다.
    
- Backpressure 테스트
    
    ```java
    import reactor.core.publisher.Flux;
    import reactor.core.publisher.FluxSink;
    
    public class BackpressureTestExample {
        public static Flux<Integer> generateNumber() {
            return Flux
                    .create(emitter -> {
                        for (int i = 1; i <= 100; i++) {
                            emitter.next(i);
                        }
                        emitter.complete();
                    }, FluxSink.OverflowStrategy.ERROR);
        }
    }
    
    import org.junit.jupiter.api.Test;
    import reactor.test.StepVerifier;
    
    public class Example13_11 {
    
        @Test
        public void generateNumberTest() {
            StepVerifier
                    .create(BackpressureTestExample.generateNumber(), 1L)
                    .thenConsumeWhile(num -> num>=1)
                    .verifyComplete();
        }
    }
    
    //
    java.lang.AssertionError: expectation "expectComplete" failed (expected: onComplete(); actual: onError(reactor.core.Exceptions$OverflowException: The receiver is overrun by more signals than expected (bounded queue...)))
    	at reactor.test.MessageFormatter.assertionError(MessageFormatter.java:115)
    ```
    
    위의 코드는 데이터를 1부터 100까지 emit합니다. Backpressure 전략으로 ERROR 전략을 지정했기 때문에 오버플로가 발생하면 OverflowException이 발생합니다.
    
    테스트가 오류가 나는 이유는 SetpVerifier가 create할 때 요청 갯수를 1개로 지정했기에 오버플로가 발생하였기 때문입니다.
    

- TestPublisher를 사용한 테스트
    - 정상동작하는 TestPublisher
        
        ```java
        public class Example13_18 {
        
            @Test
            public void divideByTwoTest() {
                TestPublisher<Integer> source = TestPublisher.create();
        
                StepVerifier
                        .create(GeneralTestExample.divideByTwo(source.flux()))
                        .expectSubscription()
                        .then(() -> source.emit(2, 4, 6, 8, 10))
                        .expectNext(1, 2, 3, 4)
                        .expectError()
                        .verify();
            }
        }
        ```
        
        1. TestPublisher를 생성합니다.
        2. 테스트 대상 클래스에 파라미터로 Flux를 전달을 위해 flux() 메서드를 이용합니다.
        3. emit() 메서드를 사용해 테스트에 필요한 데이터를 emit합니다.
        
        TestPublisher를 사용하면, 예제 코드처럼 간단한 숫자를 테스트하는 것이 아니라 복잡한 로직이 포함된 대상 메서드를 테스트하거나 조건에 따라서 Signal을 변경해야 되는 등의 특정 상황을 테스트하기가 용이할 것입니다.
        
    - 오동작하는 TestPublisher
        
        스트림즈의 사양을 위반하는 상황이 발생하는지 테스트할 수 있습니다. 오동작의 의미는 리액티브 스트림즈 사양 위반 여부를 사전에 체크하지 않는다는 의미입니다. 따라서 스트림즈 사양에 위반되더라도 TestPublisher는 데이터를 emit할 수 있습니다.
        
        ```java
        import org.junit.jupiter.api.Test;
        import reactor.test.StepVerifier;
        import reactor.test.publisher.TestPublisher;
        
        import java.util.Arrays;
        import java.util.List;
        
        public class Example13_19 {
            @Test
            public void divideByTwoTest() {
        
                TestPublisher<Integer> source = TestPublisher.createNoncompliant(TestPublisher.Violation.ALLOW_NULL);
        
                StepVerifier
                        .create(GeneralTestExample.divideByTwo(source.flux()))
                        .expectSubscription()
                        .then(() -> {
                            getDataSource().stream()
                                    .forEach(data -> source.next(data));
                            source.complete();
                        })
                        .expectNext(1, 2, 3, 4, 5)
                        .expectComplete()
                        .verify();
            }
            private static List<Integer> getDataSource() {
                return Arrays.asList(2,4,6,8,null);
            }
        }
        
        ```
        
        위에서 TestPublisher.Violation.ALLOW_NULL 이 부분을 통해 위반 조건을 지정하여 데이터의 값이 null이라도 정상 동작하는 TestPublisher를 생성합니다.
        
        따라서 onNext Signal을 전송하는 과정에서 에러가 발생할 수 있습니다.
        
        만약 Publisher가 위반 조건을 허용하지 않는다면 onNext 전에 Validation을 통해 전송할 데이터가 null이면 에러가 발생합니다.
        
        "오동작하는(TestPublisher)"이라는 용어는 테스트 시나리오에서 의도적으로 예상치 못한 동작을 유발하고자 할 때 사용됩니다. 이를 통해 소프트웨어 시스템이 예기치 않은 조건에서도 올바르게 작동하는지 확인할 수 있습니다. 주로 오동작 시나리오는 소프트웨어의 안정성과 견고성을 평가하거나 경계 조건에서의 동작을 테스트할 때 사용됩니다. 오동작을 일으키는 테스트 케이스를 통해 시스템이 예상치 못한 상황에서도 예상대로 처리할 수 있는지 확인할 수 있습니다. 이를 통해 소프트웨어의 신뢰성을 향상시키고 예기치 못한 오류를 방지할 수 있습니다.
        
- PublisherProbe를 사용한 테스팅
    
    주로 조건에 따라 Sequence가 분기되는 경우, Sequence의 실행 경로를 추적해서 정상적으로 실행 되었는지 테스트 할 수 있습니다.
    
    ```java
    import reactor.core.publisher.Mono;
    
    public class PublisherProbeTestExample {
        public static Mono<String> processTask(Mono<String> main, Mono<String> standby) {
            return main
                    .flatMap(massage -> Mono.just(massage))
                    .switchIfEmpty(standby);
        }
    
        public static Mono<String> supplyMainPower() {
            return Mono.empty();
        }
    
        public static Mono supplyStandbyPower() {
            return Mono.just("# supply Standby Power");
        }
    }
    ```
    
    processTask() 메서드는 평소에 주전력을 사용해 작업을 진행하다 주전력이 끊겼을 경우 예비 전력을 사용해 작업을 진행하는 상황을 시뮬레이션 합니다. 예제 코드에서 예비 전략을 사용하는 코드가 switchIfEmpty() Operator입니다.
    
    switchIfEmpty()는 Upstream Publisher가 데이터 emit 없이 종료되는 경우, 대체 Publisher가 데이터를 emit합니다.
    
    ```java
    import org.junit.jupiter.api.Test;
    import reactor.test.StepVerifier;
    import reactor.test.publisher.PublisherProbe;
    
    public class Example13_21 {
        @Test
        public void publisherProbeTest() {
            PublisherProbe<String> probe =
                    PublisherProbe.of(PublisherProbeTestExample.supplyStandbyPower());
    
            StepVerifier
                    .create(PublisherProbeTestExample
                            .processTask(PublisherProbeTestExample.supplyMainPower(),
                                    probe.mono()))
                    .expectNextCount(1)
                    .verifyComplete();
    
            probe.assertWasSubscribed();
            probe.assertWasRequested();
            probe.assertWasNotCancelled();
        }
    }
    ```
    
     PublisherProbe는 리액티브 스트림에서 특정 Publisher의 동작을 모의하고 테스트하는데 사용합니다.
    
    processTask() 메서드는 두 개의 Mono를 받아들여 첫 번째 Mono인 main을 확인하고, 만약 비어있다면 두 번째 Mono인 standby 로 전환하는 메서드 입니다.
    
    1. supplyMainPower() 메서드가 비어있는 Mono를 반환합니다.
    2. supplyStandbyPower() 메서드가 “# supply Standby Power” 문자열을 포함하는 Mono를 반환합니다.
    3. processTask() 메서드에 supplyMainPower()와 supplyStandPower() 를 전달하고 해당 결과를 테스트합니다.
    4. StepVerifier를 사용하여 processTask() 의 결과가 올바른지 확인합니다. 여기서 expectNextCount(1)으로 첫 번째 Mono에서 값이 전달되는지 확인하고, verifyComplete로 완료 여부를 확인합니다.
    5. 마지막으로 probe 객체 상태를 확인합니다. 해당 PubliserProbe가 구독되어 있는지, 데이터가 요청되었는지, 취소되지 않았는지를 확인합니다.