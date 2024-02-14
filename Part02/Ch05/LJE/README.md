# Reactor란?

Reactor는

> 리액티브 스트림즈의 구현제

입니다.

Spring WebFlux 기반의 리액티브 애플리케이션을 제작하기 위한 핵심 역할을 담당하고 있습니다. 쉽게 말해서 **리액티브 프로그래밍을 위한 라이브러리**라고 정의 할 수 있습니다. 따라서, Reactor Core 라이브러리는 Spring WebFlux 프레임워크에 라이브러리로 포함되어 있습니다.

# Reactor의 특징

아래 사진은 Reactor 공식 홈페이지의 메인 화면입니다. 이를 통해 Reactor가 어떤 특징들을 가지고 있는지 알아보겠습니다.

![](https://blog.kakaocdn.net/dn/bpMZpU/btsEHGoFVuO/cO0PKxVhEvlaxK6j2rXPs1/img.png)

### Reactive Streams

Reactor는 리액티브 스트림즈 사양을 구현한 리액티브 **라이브러리**입니다.

### Non-Blocking

Reactor는 JVM 위에서 실행되는 Non-Blocking 애플리케이션을 제작하기 위해 필요한 핵심 기술입니다.

### Java's functional API

Reactor에서 **Publisher**와 **Subscriber** 간의 상호작용은 Java의 함수형 프로그래밍 API를 통해서 이루어집니다. 

### Flux[N]

Reactor의 **Publisher**의 2가지 타입 중 하나로, **N개의 데이터를 `emit`** 한다는 의미입니다. 즉, 0개부터 N개(무한대)의 데이터를 `emit` 할 수 있는 Reactor의 Publisher입니다. 

### Mono[0|1]

Reactor에서 지원하는 또다른 Publisher 타입으로, **데이터를 한 건도 `emit` 하지 않거나 단 한 건만 `emit`** 하는 단발성 데이터 `emit`에 특화된 Publisher 입니다. 

### Well-suited for microservices

마이크로 시스템(MSA)은 Non-Blocking I/O에 적합한 시스템 중 하나입니다. 따라서, Non-Blocking I/O 특징을 가지는 Reactor는 MSA에서 수많은 서비스들 간에 지속적으로 발생하는 I/O를 처리하기에 매우 적합한 기술입니다.

### Backpressure-ready network

Publisher로부터 전달받은 데이터를 처리할 때 과부하가 걸리지 않도록 제어하는 **Backpressure를 지원**합니다. Backpressure는 간단히 말해 Publisher로부터 전달되는 대량의 데이터를 Subscriber가 적절하게 처리하기 위한 제어 방법입니다. 

# Reactor 구성요소

Hello, Reactor를 출력하는 간단한 예제를 통해 **Reactor의 구성요소**를 살펴 봅시다. 

```java
public static void main(String[] args) {
    Flux<String> sequence = Flux.just("Hello", "Reactor");
    sequence.map(data -> data.toUpperCase())
    	.subscribe(data -> System.out.println(data));
}
```

위에서 `Flux`는 Reactor에서 **Publisher**의 역할을 합니다. 즉, 입력으로 들어오는 데이터를 제공하는 역할을 하는 것입니다. 위 코드에서 입력으로 들어오는 데이터는 "Hello"와 "Reactor"입니다. 가공되지 않은 데이터이기 때문에 **데이터 소스**라고 불립니다. 

여기서 확인 할 수 있는 것은 위에서 얘기했듯, 데이터 개수가 2개이기 때문에 N개의 데이터를 처리할 수 있는 `Flux`를 사용한 것입니다. 

마지막 라인의 `subscribe` 메서드의 파라미터로 전달된 `data -> System.out.println(data)` 가 **Subscriber**의 역할을 합니다. 

위의 `just()`와 `map()`은 Reactor에서 지원하는 Operator 메서드입니다.

- `just()` := 데이터를 생성해서 제공하는 역할
- `map()` := 전달받은 데이터를 가공하는 역할

위의 코드에서는 전달받은 (문자열)데이터를 `map()`을 통해 대문자로 변경합니다. 

또한, `just()`의 리턴값이 `Flux`인 것을 확인 할 수 있습니다. 이를 통해 Reactor의 Operator는 리턴 값으로 `Flux`(또는 `Mono`)를 반환하기 때문에 체인을 형성해 다른 Operator를 연속적으로 호출할 수 있다는 것을 알 수 있습니다. 

정리하자면,
1.  데이터를 생성해서 제공하고
2.  데이터를 가공한 후에
3.  전달받은 데이터를 처리한다.

라는 위 세 가지 단계는 어떤 추가 작업과 상관없이 수행되는 **필수 단계**입니다.
