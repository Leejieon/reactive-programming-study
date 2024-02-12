# 05 Reactor 개요

- Spring Framework 팀의 주도하에 개발된 리액티브 스트림즈의 구현체
    - 리액티브 프로그래밍을 위한 라이브러리
    - Spring Framework 5 버전부터 리액티브 스택에 포함되어 Spring WebFlux 기반의 리액티브 애플리케이션을 제작하기 위한 핵심 역할을 담당함

```java
spring-boot-starter-webflux  
```

- dependency 추가

### Reactor의 특징

1. **Reactive Streams**: 리액티브 스트림즈 사양 구현
2. **Non-Blocking**: Reactor는 JVM위에서 실행되는 Non-Blocking 애플리케이션 제작 핵심기술
3. **Java’s functional API**: Publiher와 Subscriber 간 상호작용은 Java의 함수형 프로그래밍 API를 통해 이루어짐
4. **Flux[N]**: Reactor의 Publisher 타입 중 하나 → N개의 데이터를 emit한다
5. **Mono[0|1]**: Reactor의 Publisher 타입 중 하나 → 0또는 1개의 데이터를 emit (단발성 데이터 emit에 특화)
6. **Well-suited for microservices**: Non-Blocking I/O 특징을 가지는 Reactor는 MSA기반 시스템에서 수많은 서비스들 간에 지속적으로 발생하는 I/O를 처리하기 적합
7. **Backpressure-ready network**: Reactor는 Publisher로부터 전달받은 데이터를 처리하는 데 있어 과부하가 걸리지 않도록 제어하는 Backpressure 지원함/다양한 Backpressure 전략 제공

## 5.2 Hello Reactor 코드로 보는 Reactor의 구성요소

```java
Flux<String> sequence = Flux.just("Hello", "Reactor");
sequence.map(data -> data.toLowerCase())
					.subscribe(data -> System.out.println(data));
```

- Flux: Reactor에서 Publisher 역할
    - 데이터 소스의 데이터 개수가 2개 → N건의 데이터를 처리할 수 있는 Reactor의 Publisher 타입인 Flux 사용
- just: 입력으로 들어오는 데이터 → Publisher가 입력으로 들어오는 데이터를 제공함
    - Publisher가 최초로 제공하는 데이터 → **데이터 소스**
- data → sout(data)가 Subsriber 역할을 함
    - Consumer 함수형 인터페이스
- just와 map은 Reactor에서 지원하는 Operator 메서드
    - just() Operator는 **데이터를 생성**해서 제공하는 역할
    - map() Operator는 **전달받은 데이터 가공**
- 리턴값이 Flux여서 Operator 체인을 형성한다!
- 데이터를 제공/ 가공/ 처리