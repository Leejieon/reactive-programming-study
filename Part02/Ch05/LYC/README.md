# Reactor

- Reactor란?
    
    리액터란 리액티프 스트림즈의 구현체로 Spring WebFlux 기반의 리액티브 애플리케이션 제작하기 위한 핵심 역할을 담당
    
    ![스크린샷 2024-02-14 오후 8.48.30.png](Reactor%20ae290689256d42f88ba0ae877df2bc36/%25E1%2584%2589%25E1%2585%25B3%25E1%2584%258F%25E1%2585%25B3%25E1%2584%2585%25E1%2585%25B5%25E1%2586%25AB%25E1%2584%2589%25E1%2585%25A3%25E1%2586%25BA_2024-02-14_%25E1%2584%258B%25E1%2585%25A9%25E1%2584%2592%25E1%2585%25AE_8.48.30.png)
    
- Reacotr의 특징
    1. Reactive Streams : Reactor는 리액티브 스트림즈를 구현한 리액티브 라이브러리입니다.
    2. Non-Blocking : Reactor는 JVM 위에서 실행되는 Non-Blocking 애플리케이션 제작에 필요한 핵심 기술입니다.
    3. Java’s functional API : Publisher와 Subscriber 간의 상호 작용을 함수형 API를 통해 이루어집니다.
    4. Flux[N] : Reactor의 Publisher는 크게 두 가지 타입이 존재합니다. 그 중 한가지가 Flux입니다. Flux[N]은 N개의 데이터를 emit한다는 것인데, 즉 0개부터 N개, 즉 무한대의 데이터를 emit할 수 있는 Publisher 입니다.
    5. Mono[0|1] : 나머지 다른 하나의 타입으로, Mono는 0과 1로만 표현이 됩니다. 즉 단 한 건만 emit하는 단발성 데이터에 특화된 Publisher 입니다.
    6. Well-suited for microservices : 리액터는 마이크로 서비스 기반 시스템에서 수많은 서비스들 간에 지속적으로 발생하는 I/O를 처리하기에 특화된 기술입니다.
    7. Backpressure-ready network : Publisher로 부터 전달되는 대량의 데이터를 Subscriber가 적절하게 처리하기 위한 제어 방법입니다.

- 코드로 알아보는 Reactor의 구성요소
    
    ```java
    public class Example{
        public static void main(String[] args) {
            Flux<String> sequence = Flux.just("Hello", "Reactor");
            sequence.map(data -> data.toLowerCase())
                    .subscribe(data -> System.out.println(data));
        }
    }
    ```
    
    - Flux의 역할 : 입력으로 들어오는 데이터로, 가공되지 않은 데이터 소스인 “Hello”, “Reactor”라는 여러개의 데이터를 처리하기 위해 Publisher의 타입 중 Flux를 사용합니다.
    - Subscriber의 역할 : subscribe 메서드 안에 람다식으로 표현식이 Subscriber의 역할을 합니다. 즉 람다 표현식은 LambdaSubscriber라는 클래스에 전달되어 데이터를 처리하는 역할을 합니다.
    - just(), map() 메서드 : Reactor에서 지원하는 Operator 메서드들로, just는 데이터를 생성해서 제공하는 역할을, map은 전달받은 데이터를 가공하는 역할을 합니다.