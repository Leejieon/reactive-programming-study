# 03 Blocking I/O와 Non-Blocking I/O

<br>

### Blocking I/O: 네트워크 I/O 에서의 Blocking I/O

1. 클라이언트 PC에서 본사 API 서버에 도서 정보 조회
2. 본사 API 서버는 지점 API 서버에 추가적 요청 보냄
    - 스레드는 차단, 지점 API 서버의 응답을 대기

- 하나의 스레드가 I/O 에 의해 차단되어 대기하는 것 → Blocking I/O
- 문제점 보완을 위해 멀티스레딩 기법으로 추가 스레드 할당 → 문제점 존재
    - 컨텍스트 스위칭 스레드 전환 비용 발생(CPU 전체 대기시간이 길어짐)
    - 과다한 메모리 사용 오버헤드
        - 일반적 서블릿 컨테이너 기반 Java 웹 애플리케이션 → 요청당 하나의 스레드를 할당
        - 스레드 계속 추가할당하면 시스템 감당 힘들어
    - 스레드 풀에서 응답 지연 발생가능
        - 톰캣 서블릿 컨테이너는 요청 처리 위해 스레드 풀 사용함
        - 유휴 스레드 없는 경우 사용 가능 스레드 확보까지 응답 지연 발생

<br>

### Non-Blocking I/O

1. 클라이언트 PC에서 본사 API 서버에 도서 정보 조회
2. 본사 API 서버는 지점 API 서버 2개에 추가적 요청 보냄
    - A 지점에 요청 보내고
    - 즉시 B지점에 요청 보냄

- Non-Blocking I/O 방식의 경우, 작업 스레드의 종료 여부와 관계없이 요청한 스레드는 차단되지 않음
    - 하나의 스레드로 많은 수의 요청 처리가능
    - 적은 수의 스레드 사용 → 멀티 스레딩 기법 문제점 안생김
    - CPU 대기 시간 및 사용량 효율!
- 단점
    1. 스레드 내부에 CPU를 많이 사용하는 작업 포함 시 성능 악영향
    2. 사용자 요청~응답 과정에서 Blocking I/O 요소가 포함된 경우 Non-Blocking 이점 발휘 힘듦

<br><br>

## 3.3 Spring Framework에서의 Blocking I/O와 Non-Blocking I/O

- Spring MVC 기반으로 개발된 웹 애플리케이션의 요청 처리 버거울 시대..
    - Spring MVC 기반 웹 애플리케이션은 Blocking I/O 방식 사용!
    - 스마트폰, IOT 기술 발전으로 Blocking I/O 방식의 애플리케이션이 감당이 힘들만큼 클라이언트 요청 트래픽이 발생하는 상황이 많아짐
- 대안 등장, Spring WebFlux
    - Non-Blocking I/O 방식!
- MVC는 요청 당 하나의 스레드를 사용
    - 대량의 요청을 처리하기 위해 과도한 스레드를 사용 → CPU 대기 시간이 늘어나고 메모리 사용시 오버헤드
- WebFlux는 Netty 같은 비동기 Non-Blocking I/O 기반의 서버 엔진 사용,
    - 적은 수의 스레드로 많은 수의 요청 처리
    - CPU와 메모리를 효율 적으로 사용 가능
    - 적은 컴퓨팅 파워로 고성능의 애플리케이션 운영 가능

```java
@GetMapping
public Mono<Book> getBook() {
  URI getBookUri = UriComponentsBuilder.fromUri(baseUri)
				.path("~")
				.build()
				.expand(bookId)
				.encode()
				.toUri();

	return WebClient.create()
						.get()
						.uri(getBookUri)
						.retrieve()
						.bodyToMono(Book.class);
}
```

- WebClient를 사용해서 요청을 전송한 후, 전달받은 도서 정보를 포함한 Mono 타입으로 바꾸는 과정 거쳐 → Mono 타입 객체 반환
- Spring WebFlux 기반 애플리케이션의 경우 스레드가 차단되지 않음

<br><br>

## 3.4 Non-Blocking I/O 방식의 통신이 적합한 시스템

- Spring WebFlux 를 도입하기 위해 고려해야할 사항

1. 학습 난이도
    - DI, AOP, 서비스 추상화 등 Spring Framework 에서 사용되는 핵심 개념들을 어느정도 이해하고 있다는 가정하에
    - Spring MVC기반 개발 방식은 Spring WebFlux에 비해 상대적으로 학습난이도 낮음
    - 리액티브 스트림즈 표준사양을 구한한 구현체를 능숙하게 사용하기까지 학습 노력, 시간 많이 필요함
2. 리액티브 프로그래밍 경험이 있는 개발 인력 확보가 쉬운가
    - MVC 경우 숙련된 개발 인력 확보가 용이함
    - 리액티브 프로그래밍 지식을 갖춘 숙련된 개발 인력 확보 어려움
    - MVC 기반 프로젝트는 안정적으로 진행될 가능성이 높지만, WebFlux 기반 프로젝트는 인력, 기술적 측면 위험부담 큼

- Spring WebFlux 기술 사용하기 조금 더 적합한 유형 애플리케이션 있음
    1. 대량의 요청 트래픽이 발생하는 시스템
        - 서버의 증설이나 VM확장 등을 통해 트래픽 분산가능하지만, 높은 비용 지불해야함
        - 상대적으로 적은 컴퓨팅 파워를 사용함으로써 저비용으로 고수준 성능을 이끌어낼 수 있는 Spring WebFlux 기반 애플리케이션
    2. 마이크로 서비스 기반 시스템
        - 시스템 특성상 서비스들 간 많은 수의 I/O가 지속적으로 발생함
        - 특정 서비스들 간 통신에서 Blocking 으로 인한 응답지연 발생 시 → 다른 서비스들에도 영향 미칠 가능성 있음
        - 응답 지연 연쇄 작용 → 시스템 전체 마비 가능성도 있음
        - MSA 시스템에서는 Spring WebFlux같은 Non-Blocking I/O 방식의 기술이 필요함
    3. 스트리밍 또는 실시간 시스템
        - 리액티브 프로그래밍은 HTTP 통신 or 디비 조회 같은 일회성 연결 뿐만아니라
        - 무한한 데이터 스트림을 받아 효율적으로 처리 가능
        - 무한 데이터 스트림을 처리하기 위한 스트리밍 또는 실시간 시스템 쉽게 구축가능