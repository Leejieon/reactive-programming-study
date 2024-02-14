# 5. Reactor 개요



## 1. Reactor

Spring의 리액티브 스트림즈 구현체이자 라이브러리

Non - Blocking의 특성을 가지고 있으며 자바 함수형 API를 사용한다.

수많은 서비스들간에 지속적으로 발생하는 I/O를 처리하기에 적합한 기술이며 이 때 Publisher로부터 받은 데이터를 처리하는데 있어 과부하가 걸리지 않도록 제어하는 Backpressure 지원.



## 2. 'Hello Reactor' 로 보는 Reactor 구성요소

```java
/**
 * Hello Reactor 예제
 */


public class Example5_1 {
    public static void main(String[] args) {
        Flux<String> sequence = Flux.just("Hello", "Reactor");
        sequence.map(data -> data.toLowerCase())
                .subscribe(data -> System.out.println(data));
    }
}
```



Reactive Stream에서 Publisher는 Flux 혹은 Mono를 통해 데이터 타입을 전송하는데 이 코드에서  Flux는 Publisher의 역할을 한다.

Publisher는 "Hello", "Reactor"로 데이터를 제공하며 이 데이터는 Publisher가 최초로 제공하는 가공되지 않은 데이터로 데이터 소스라고 불린다.  이때 이 데이터 소스가 N건이면 Flux, 단일이면 Mono이다.



 just, map등은 Reactor에서 지원하는 Operator 메서드인데, just()는 데이터를 생성해서 제공하는 역할을, map()은 전달받은 데이터를 가공하는 역할을 한다.
