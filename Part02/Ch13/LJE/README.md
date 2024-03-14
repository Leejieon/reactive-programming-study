우선, reactor-test 모듈의 기능을 사용하기 위해 다음 dependency를 추가해줍시다. 

```java
dependencies {
	testImplementation 'io.projectreactor:reactor-test'
}
```

# StepVerifier를 사용한 테스팅

Reactor의 가장 일반적인 테스트 방식은 

> Flux or Mono를 Reactor Sequence로 정의한 후, 구독 시점에 해당 Operator 체인이 시나리오대로 동작하는지를 테스트
> 

하는 것입니다. 

Reactor에서는 Operator 체인의 다양한 동작 방식을 테스트하기 위해 `StepVerifier` 라는 API를 제공합니다. 

## Signal 이벤트 테스트

> Reactor Sequence에서 발생하는 Signal 이벤트를 테스트
> 

```java
@Test
public void sayHelloReactorTest() {
	StepVerifier
			.create(Mono.just("Hello Reactor")) // 테스트 대상 Sequence 생성
			.expectNext("Hello Reactor")	// emit 된 데이터 기댓값 평가
			.expectComplete() //onComplete Signal 기댓값 평가
			.verify(); // 검증 실행
}
```

테스트 방법은 다음과 같습니다.

1. `create()` 를 통해 테스트 대상 Sequence를 생성합니다.
2. `expectXXXX()` 를 통해 Sequence에서 예상되는 Signal의 기댓값을 평가합니다.
3. `verify()`를 호출함으로써 전체 Operator 체인의 테스트를 트리거합니다. 

위 코드에서는 `expectNext()`를 이용해서 기대했던 데이터가 정상적으로 emit 되었는지, `expectComplete()`을 이용해서 Sequence가 기대했던 대로 종료되었는지를 단계적으로 테스트합니다.

![Untitled](https://github.com/Leejieon/reactive-programming-study/assets/42794501/eba04267-3401-44d5-bd68-ce238d388d2a)

### 대표적인 expectXXX() 메서드

| 메서드 | 설명 |
| --- | --- |
| expectSubscription() | 구독이 이루어짐을 기대 |
| expectNext(T t) | onNext Signal을 통해 전달되는 값이 파라미터로 전달된 값과 같음을 기대 |
| expectComplete() | onComplete Signal이 전송되기를 기대 |
| expectError() | onError Signal이 전송되기를 기대 |
| expectNextCount(long count) | 구독 시점 또는 이전 expectNext()를 통해 기댓값이 평가된 데이터 이후부터 emit 된 수를 기대 |
| expectNoEvent(Duration duration) | 주어진 시간 동안 Signal 이벤트가 발생하지 않았음을 기대 |
| expectAccessibleContext() | 구독 시점 이후에 Context가 전파되었음을 기대 |
| expectNextSequence(Iterable <? extendsT> iterable) | emit 된 데이터들이 파라미터로 전달된 Iterable의 요소와 매치됨을 기대 |

### 테스트 대상 Operator 체인에 대한 검증을 트리거(verifyXXX())하는 메서드

| 메서드 | 설명 |
| --- | --- |
| verify() | 검증을 트리거 |
| verifyComplete() | 검증을 트리거하고, onComplete Signal을 기대 |
| verifyError() | 검증을 트리거하고, onError Signal을 기대 |
| verifyTimeout(Duration duration)  | 검증을 트리거하고, 주어진 시간이 초과되어도 Publisher가 종료되지 않음을 기대 |

```java
public class GeneralTestExample {
    public static Flux<String> sayHello() {
        return Flux.just("Hello", "Reactor");
    }

    public static Flux<Integer> divideByTwo(Flux<Integer> source) {
        return source.zipWith(Flux.just(2, 2, 2, 2, 0), (x, y) -> x / y);
    }

    public static Flux<Integer> takeNumber(Flux<Integer> source, long n) {
        return source.take(n);
    }
}

...

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
```

위 코드의 `as()` 메서드는 

> 이전 기댓값 평가 단계에 대한 설명(description)을 추가
> 

할 수 있는 메서드입니다. 만약 테스트에 실패하게 되면 실패한 단계에 해당하는 설명이 로그로 출력됩니다. 

![Untitled (1)](https://github.com/Leejieon/reactive-programming-study/assets/42794501/18f5df22-7acd-43f9-ae2b-1440b22b7496)

위 코드는 총 4 가지 단계(Subscription, 두 번의 onNext, onComplete)를 테스트합니다. 이때, 첫 번째로 emit된 데이터가 ‘Hi’라 기대했지만, 실제로는 ‘Hello’이기 때문에 테스트 결과는 **failed**입니다. 

```java
@Test
public void divideByTwoTest() {
    Flux<Integer> source = Flux.just(2, 4, 6, 8, 10);
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
```

위 테스트의 실행 결과는 **passed**입니다. 마지막에 emit 된 데이터는 0으로 나누어 ArithmeticException이 발생했지만,  `expectError()`를 통해서 에러를 기대하기 때문에 최종 테스트 결과는 passed가 되는 것입니다. 

```java
@Test
public void takeNumberTest() {
    Flux<Integer> source = Flux.range(0, 1000);
    StepVerifier
            .create(GeneralTestExample.takeNumber(source, 500),
                    StepVerifierOptions.create().scenarioName("verify from 0 to 499"))
            .expectSubscription()
            .expectNext(0)
            .expectNextCount(498)
            .expectNext(500)
            .expectComplete()
            .verify();
}
```

### StepVerifierOptions

> `StepVerifier`에 옵션, 즉 추가적인 기능을 덧붙이는 작업을 하는 클래스
> 

위 코드의 takeNumber() 메서드는 Source Flux에서 파라미터로 전달된 숫자의 개수만큼만 데이터를 emit 하는 메서드 입니다. 

Flux가 총 500개의 숫자를 emit하는데 첫 번째 emit된 숫자 0을 평가하고, 다음 emit된 데이터의 개수가 498개라고 기대했습니다. 그리고 나머지 1개의 데이터만 평가하면 되는데, 기댓값은 499여야 하는데 500을 기대합니다. 따라서 테스트 결과는 **failed** 입니다. 

![Untitled (2)](https://github.com/Leejieon/reactive-programming-study/assets/42794501/c62c1c14-555f-4d69-87b6-989dcd64d575)

## 시간 기반(Time-based) 테스트

`StepVerifier`는 가상의 시간을 이용해 **미래에 실행되는 Reactor Sequence의 시간을 앞당겨 테스트할 수 있는 기능**을 지원합니다. 

```java
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
```

### Tuples

> 서로 다른 타입의 데이터를 저장할 수 있는 Reactor에서 제공하는 Collection
> 
- `of()` := 총 8개의 데이터를 저장할 수 있다.

### 테스트 1

```java
/**
 * StepVerifier 활용 - 1
 * - 주어진 시간을 앞당겨서 테스트 한다.
 */
@Test
public void getCOVID19CountTest() {
    StepVerifier
            .withVirtualTime(() -> TimeBasedTestExample.getCOVID19Count(
                            Flux.interval(Duration.ofHours(1)).take(1)
                    )
            )
            .expectSubscription()
            .then(() -> VirtualTimeScheduler
                                .get()
                                .advanceTimeBy(Duration.ofHours(1)))
            .expectNextCount(11)
            .expectComplete()
            .verify();

}
```

이번 테스트의 목적

- 현재 시점에서 1시간 뒤에 COVID-19 확진자 발생 현황을 체크하고자 하는데, 테스트 대상 메서드의 Sequence가 1시간 뒤에 실제로 동작하는지 확인하는 것

### `withVirtualTime()`, `advanceTimeBy()`

원래라면 실제로 1시간을 기다려야 알 수 있지만, `withVirtualTime()` 메서드를 이용해 **VirtualTimeScheduler**라는 가상 스케줄러의 제어를 받도록 해줍니다. 

따라서, 구독에 대한 기댓값을 평가하고 난 후 `then()` 메서드를 사용해서 후속 작업을 할 수 있도록 하는데, 여기서 `advanceTimeBy()` 를 이용해서 시간을 1시간 당기는 작업을 수행합니다. 

최종적으로 테스트 결과는 **passed** 입니다.

### 테스트 2

```java
/**
 * StepVerifier 활용 - 2
 *  -검증에 소요되는 시간을 제한한다.
 */
@Test
public void getCOVID19CountTest() {
    StepVerifier
            .create(TimeBasedTestExample.getCOVID19Count(
                            Flux.interval(Duration.ofMinutes(1)).take(1)
                    )
            )
            .expectSubscription()
            .expectNextCount(11)
            .expectComplete()
            .verify(Duration.ofSeconds(3));
}
```

테스트 대상 메서드에 대한 **기댓값을 평가하는 데 걸리는 시간을 제한**하는 테스트입니다. 

코드를 보면, `verify()` 메서드에 3초의 시간을 지정했습니다. 즉, 3초 내에 기댓값의 평가가 끝나지 않으면 시간 초과로 간주하겠다는 의미입니다.

따라서, 결과적으로 시간 초과로 인해 **AssertionError** 가 발생합니다. 

### 테스트 3

```java
/**
 * StepVerifier 활용 - 3
 *  - 지정된 대기 시간동안 이벤트가 없을을 확인한다.
 */
@Test
public void getVoteCountTest() {
    StepVerifier
            .withVirtualTime(() -> TimeBasedTestExample.getVoteCount(
                            Flux.interval(Duration.ofMinutes(1))
                    )
            )
            .expectSubscription()
            .expectNoEvent(Duration.ofMinutes(1))
            .expectNoEvent(Duration.ofMinutes(1))
            .expectNoEvent(Duration.ofMinutes(1))
            .expectNoEvent(Duration.ofMinutes(1))
            .expectNoEvent(Duration.ofMinutes(1))
            .expectNextCount(5)
            .expectComplete()
            .verify();
}

```

위는 `expectNoEvent()`를 사용해서 **지정한 시간 동안 어떤 Signal 이벤트도 발생하지 않았음**을 기대하는 테스트입니다.

위 코드는 다섯 개 구 중에서 한 개 구의 투표 현황을 1분에 한 번씩 순차적으로 확인하기 때문에 원래는 총 5분의 시간이 소요되며, onNext Signal 이벤트가 한 번 발생하면 다음 이벤트가 발생하기 전까지 1분 동안 아무 이벤트도 발생하지 않습니다.

우선 시간을 줄이기 위해 `withVirtualTime()`을 사용했습니다. 그리고 `expectNoEvent()` 메서드를 통해 1분 동안 onNext Signal 이벤트가 발생하지 않을 것이라고 기대합니다.

여기서 `expectNoEvent()`의 **파라미터로 시간을 지정**하면 

- 지정한 시간 동안 어떤 이벤트도 발생하지 않을 것을 기대
- 지정한 시간만큼 시간을 앞당김

이 두 가지 기능을 수행할 수 있습니다. 

결과적으로 테스트 결과는 **passed** 입니다.

## Backpressure 테스트

```java
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
```

위 코드에서는 `create()` Operator 내부에서 100개의 숫자를 emit 하고 있으며, **Backpressure 전략**으로 **ERROR 전략을 지정**했기 때문에 오버플로가 발생하면 OverflowException이 발생할 것입니다. 

### 테스트 1

```java
/**
 * StepVerifier Backpressure 테스트
 */
@Test
public void generateNumberTest() {
    StepVerifier
            .create(BackpressureTestExample.generateNumber(), 1L)
            .thenConsumeWhile(num -> num >= 1)
            .verifyComplete();
}
```

generateNumber() 메서드는 한 번에 100개의 숫자 데이터를 emit 합니다. 하지만, StepVerifier의 create() 메서드에서 데이터의 요청 개수를 1로 지정해서 오버플로가 발생했기 때문에 테스트 결과는 failed 가 됩니다.  

`thenConsumeWhile()` 메서드를 사용해 emit 되는 데이터를 소비하고 있지만, 예상한 것보다 더 많은 데이터를 수신함으로써 결국 오버플로로 인한 에러가 발생한 것입니다.  

```java
java.lang.AssertionError: expectation "expectComplete" failed (expected: onComplete(); actual: onError(reactor.core.Exceptions$OverflowException: The receiver is overrun by more signals than expected (bounded queue...)))
```

### 테스트 2

```java
@Test
public void generateNumberTest() {
    StepVerifier
            .create(BackpressureTestExample.generateNumber(), 1L)
            .thenConsumeWhile(num -> num >= 1)
            .expectError()
            .verifyThenAssertThat()
            .hasDroppedElements();

}
```

위 테스트는 

- `expectError()`를 통해 에러를 기대
- `verifyThenAssertThat()` 메서드를 통해 검증을 트리거하고 난 후, 추가적인 Assertion 실행
- `hasDroppedElements()` 메서드를 통해 Drop된 데이터가 있음을 Assertion

결과적으로, passed 입니다. 

## Context 테스트

```java
public class ContextTestExample {
    public static Mono<String> getSecretMessage(Mono<String> keySource) {
        return keySource
                .zipWith(Mono.deferContextual(ctx ->
                                               Mono.just((String)ctx.get("secretKey"))))
                .filter(tp ->
                            tp.getT1().equals(
                                   new String(Base64Utils.decodeFromString(tp.getT2())))
                )
                .transformDeferredContextual(
                        (mono, ctx) -> mono.map(notUse -> ctx.get("secretMessage"))
                );
    }
}
```

위 코드에서 Context에는 두 개의 데이터가 저장되어 있습니다. 

1. Base64 형식으로 인코딩 된 secret key
2. secret key에 해당하는 secret message

getSecretMessage()에서 Context에 저장된 데이터를 정상적으로 사용하는지 테스트 해봅시다.

```java
/**
 * StepVerifier Context 테스트
 */
@Test
public void getSecretMessageTest() {
    Mono<String> source = Mono.just("hello");

    StepVerifier
            .create(
                ContextTestExample
                    .getSecretMessage(source)
                    .contextWrite(context ->
                                    context.put("secretMessage", "Hello, Reactor"))
                    .contextWrite(context -> context.put("secretKey", "aGVsbG8="))
            )
            .expectSubscription()
            .expectAccessibleContext()
            .hasKey("secretKey")
            .hasKey("secretMessage")
            .then()
            .expectNext("Hello, Reactor")
            .expectComplete()
            .verify();
}

```

테스트 단계는 다음과  같습니다. 

1. `expectSubscription()`으로 구독이 발생함을 기대
2. `expectAccessibleContext()`로 **구독 이후, Context가 전파됨을 기대**
3. `hasKey()`로 전파된 Context에 “secretKey” 키에 해당하는 값이 있음을 기대
4. `hasKey()`로 전파된 Context에 “secretMessage” 키에 해당하는 값이 있음을 기대
5. `then()` 메서드로 Sequence의 다음 Signal 이벤트의 기댓값 평가
6. `expectNext()`로 “Hello, Reactor” 문자열이 emit 되었음을 기대
7. `expectComplete()`로 onComplete Signal이 전송됨을 기대

테스트의 결과는 passed  입니다.

## Record 기반 테스트

`expectNext()`로 emit 된 데이터의 단순 기댓값보다 더 구체적인 조건으로 Assertion해야 하는 경우가 많습니다. 

이런 경우, `recordWith()`를 사용할 수 있습니다.

### recordWith()

> 파라미터로 전달한 Java의 컬렉션에 emit 된 데이터를 추가(기록)하는 세션을 시작한다.
이렇게 컬렉션에 기록된 데이터에 다양한 조건을 지정함으로써 emit 된 데이터를 Assertion할 수 있다.
> 

```java
public class RecordTestExample {
    public static Flux<String> getCapitalizedCountry(Flux<String> source) {
        return source
                .map(country -> country.substring(0, 1).toUpperCase() +
                                country.substring(1));
    }
}
```

### 테스트 1

```java
/**
	* StepVerifier Record 테스트
	*/
@Test
public void getCountryTest() {
    StepVerifier
            .create(RecordTestExample.getCapitalizedCountry(
                    Flux.just("korea", "england", "canada", "india")))
            .expectSubscription()
            .recordWith(ArrayList::new)
            .thenConsumeWhile(country -> !country.isEmpty())
            .consumeRecordedWith(countries -> {
                assertThat(
                        countries
                                .stream()
                                .allMatch(country ->
                                        Character.isUpperCase(country.charAt(0))),
                        is(true)
                );
            })
            .expectComplete()
            .verify();
}

```

테스트 단계는 다음과 같습니다.

1. `expectSubscription()`으로 구독이 발생함을 기대
2. `recordWith()`로 emit된 데이터에 대한 기록을 시작
3. `thenComsumeWhile()`로 파라미터로 전달한 Predicate와 일치하는 데이터는 다음 단계에서 소비
4. `consumeRecordedWith()`로 컬렉션에 기록된 데이터를 소비. 여기서 모든 데이터의 첫 글자가 대문자인지 여부를 확인함으로써 getCapitalizedCountry() 메서드를 Assertion
5. `expectComplete()`로 onComplete Signal이 전송됨을 기대

테스트 결과는 passed 입니다.

이처럼 `recordWith()`를 사용하면 emit 되는 데이터에 대한 세밀한 테스트가 가능합니다. 

### 테스트 2

```java
/**
 * StepVerifier Record 테스트 예제
 */
@Test
public void getCountryTest() {
    StepVerifier
            .create(RecordTestExample.getCapitalizedCountry(
                    Flux.just("korea", "england", "canada", "india")))
            .expectSubscription()
            .recordWith(ArrayList::new)
            .thenConsumeWhile(country -> !country.isEmpty())
            .expectRecordedMatches(countries ->
                    countries
                            .stream()
                            .allMatch(country ->
                                    Character.isUpperCase(country.charAt(0))))
            .expectComplete()
            .verify();
}
```

# TestPublisher를 사용한 테스팅

테스트 전용 Publisher인 TestPublisher를 이용해 테스트를 진행할 수 있습니다. 

## 정상 동작하는(Well-behaved) TestPublisher

TestPublisher를 사용하면,

> 개발자가 직접 프로그래밍 방식으로 Signal을 발생시키며 원하는 상황을 미세하게 재연하며 테스트를 진행
> 

할 수 있습니다.

```java
/**
 * 정상동작 하는 TestPublisher 예제
 */
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
```

정상 동작하는 `TestPublisher`를 사용해서 테스트를 진행하는 코드입니다. 

1. `Testpublisher.create()`로 TestPublisher를 생성
2. 테스트 대상 클래스에 파라미터로 Flux를 전달하기 위해 `flux()` 메서드를 이용해 Flux로 변환
3. `emit()` 메서드를 사용해 테스트에 필요한 데이터를 emit

테스트 결과는 passed 입니다.

위의 예제에서는 간단한 테스트이기 때문에 굳이 사용할 필요가 있는가에 대한 의문이 들 수 있지만, **복잡한 로직이 포함된 대상 메서드를 테스트**하거나 **조건에 따라 Signal을 변경해야 되는 등의 상황**에서는 용이할 것입니다. 

## 오동작하는(Misbehaving) TestPublisher

```java
/**
 * 오동작 하는 TestPublisher 예제
 */
@Test
public void divideByTwoTest() {
//    TestPublisher<Integer> source = TestPublisher.create();
		TestPublisher<Integer> source =                
			     TestPublisher.createNoncompliant(TestPublisher.Violation.ALLOW_NULL);

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
    return Arrays.asList(2, 4, 6, 8, null);
}

```

위 코드에서는 TestPublisher가 오동작하도록 ALLOW_NULL 위반 조건을 지정해 데이터의 값이 null이라도 정상 동작하는 TestPublisher를 생성합니다. 

# PublisherProbe를 사용한 테스팅

`PublisherProbe`를 이용해 Sequence의 실행 경로를 테스트할 수 있습니다. 

⇒ 조건에 따라 Sequence가 분기되는 경우, Sequence의 실행 경로를 추적해 정상적으로 실행되었는지 테스트

```java
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

위 processTask() 메서드는 평소에 주전력을 사용해 작업을 진행하다가 주전력이 끊겼을 경우에만 예비 전력을 사용해 작업을 진행하는 상황을 시뮬레이션합니다.

### switchIfEmpty()

> Upstream Publisher가 데이터 emit 없이 종료되는 경우, 대체 Publisher가 데이터를 emit
> 

### 테스트

```java
/**
 * PublisherProbe 예제
 */
@Test
public void publisherProbeTest() {
    PublisherProbe<String> probe =
            PublisherProbe.of(PublisherProbeTestExample.supplyStandbyPower());

    StepVerifier
            .create(PublisherProbeTestExample
                    .processTask(
                            PublisherProbeTestExample.supplyMainPower(),
                            probe.mono())
            )
            .expectNextCount(1)
            .verifyComplete();

    probe.assertWasSubscribed();
    probe.assertWasRequested();
    probe.assertWasNotCancelled();
}
```

1. 실행 경로를 테스트할 테스트 대상 Publisher를 `PublisherProbe.of()` 메서드로 래핑
2. `probe.mono()` 에서 리턴된 Mono 객체를 processTask() 메서드의 두 번째 파라미터로 전달
3. StepVerifier를 이용해 processTask() 메서드가 데이터를 emit하고 정상적으로 종료되는지 테스트

### 이 테스트 코드의 주목적

- `switchIfEmpty()` 로 인해 Sequence가 분기되는 상황에서 실제로 어느 Publisher가 동작하는지 해당 Publisher의 실행 경로를 테스트
- `assertWasSubscribed()` , `probe.assertWasRequested()` , `probe.assertWasNotCancelled()` 메서드를 통해 기대하는 Publisher가 구독을 했는지, 요청을 했는지, 중간에 취소가 되지 않았는지를 Assertion함으로써 Publisher의 실행 경로를 테스트
