# 07 Cold Sequence와 Hot Sequence

## 7.1 Cold와 Hot의 의미

<aside>
💡 Cold는 무언가를 새로 시작하고, Hot은 무언가를 새로 시작하지 않음

</aside>

- How Swap: 컴퓨터 전원이 켜져있는 상태에서 디스크 등의 장치 교체 시 시스템 재시작없이 바로 장치 인식하는 것
- Hot Deploy: 서버를 재시작하지 않고 응용 프로그램의 변경사항을 적용할 수 있는 기능
- Hot Wallet: 인터넷에 연결되어있기 때문에 즉시 사용가능하지만 보안에 취약
- Cold Wallet: 인터넷과 단절되어 있어서 사용성은 떨어지지만 보안이 강화

## 7.2 Cold Sequence

- Sequence는 Publisher가 emit하는 데이터의 연속적인 흐름을 정의해놓은 것
  - Operator 체인 형태
- **Cold Sequence**: Subscriber가 **구독할 때마다 데이터 흐름이 처음부터 다시 시작되는 seq**
  - 구독 시점이 달라도 Publisher가 데이터를 emit하는 과정을 처음부터 다시 시작하는 데이터의 흐름
- Cold Sequence 흐름으로 동작하는 Publisher → Cold Publisher
  - 타임라인의 처음부터 emit된 모든 데이터를 받음

```java
Flux<String> coldFlux =
			Flux
				.fromIterable(Arrays.asList("Korea", "Japan"))
				.map(String::toLowerCase);

coldFlux.subscribe(country -> log.info(country));

Thread.sleep(2000L);
coldFlux.subscribe(country -> log.info(country));
```

- 구독시점이 달라도 모두 동일한 데이터를 전달받음

## 7.3 Hot Sequence

- 구독이 발생한 시점 이전에 Publisher로부터 emit된 데이터는 Subscriber가 전달받지 못하고 **구독이 발생한 시점 이후에 emit된 데이터만 전달받을 수 ㅇ**
  - Cold Sequence의 경우 여러 번의 구독 → 타임라인이 여러 번 생성
  - Hot Sequence의 경우 구독이 많이 발생해도 Publisher가 데이터를 처음부터 emit하지 않음
- Publisher가 데이터를 emit하는 과정이 한 번만 일어나고, Subscriber가 각각의 구독 시점 이후에 emit된 데이터만 전달받는 데이터의 흐름 → Hot Sequence
  - Sequence 타임라인이 하나만 생긴다고 볼 수 ㅇ

```java
String[] singers = {"Singer A", "Singer B"};

Flux<String> concertFlux =
	Flux
			.fromArray(singers)
			.delayElements(Duration.ofSeconds(1)) //데이터소스로 입력된 데이터 emit 시간 지연
			.share();      //Cold Sequence를 Hot Sequence로 동작하게 해주는 Operator

concertFlux.subscribe(singer -> log.info(singer));

Thread.sleep(2500);

concertFlux.subscribe(singer -> log.info(singer));

Thread.sleep(2500);
```

- share()
  - 원본 Flux를 멀티캐스트(공유)하는 새로운 Flux를 return한다!
  - 원본 데이터 소스를 처음으로 emit하는 Flux → fromArray의 반환값 Flux
  - 여러 Subscribe가 하나의 원본 Flux를 공유함
- Subscriber가 Flux 먼저 구독 시 데이터 emit을 시작, 이후 다른 Subscriber가 구독하는 시점에는 원본 Flux에서 이미 emit된 데이터를 전달받을 수 없게됨
- main과 여러 개의 parallel스레드가 실행되는데, 이는 delayElements() Operator의 디폴트 스레드 스케줄러가 parallel이기 때문임

## 7.4 HTTP 요청과 응답에서 Cold Sequence와 Hot Sequence의 동작흐름

```java
URI worldTimeUri = UriComponentBuilder.newInstnace().scheme("http")
				.host("~")
				.port(80)
				.path("/api/timezone/Asia/Seoul")
				.build()
				.encode()
				.toUri();

Mono<String> mono = getWorldTime(worldTimeUri); //WebClient로 받아옴
mono.subscribe(dateTime -> log.info(dateTime);
Thread.sleep(2000);
mono.subscribe(dateTime -> log.info(dateTime);

Thread.sleep(2000);
```

- Non-Blocking통신을 지원하는 WebClient를 RestTemplate대신 사용
- 구독이 발생할 때마다 데이터의 emit과정이 처음부터 새로 시작되는 Cold Sequence의 특징
  - 두 번의 구독 → 두 번의 새로은 HTTP 요청이 발생함

```java
URI worldTimeUri = UriComponentBuilder.newInstnace().scheme("http")
				.host("~")
				.port(80)
				.path("/api/timezone/Asia/Seoul")
				.build()
				.encode()
				.toUri();

Mono<String> mono = getWorldTime(worldTimeUri).cache(); //Hot Sequence로 동작
mono.subscribe(dateTime -> log.info(dateTime);
Thread.sleep(2000);
mono.subscribe(dateTime -> log.info(dateTime);

Thread.sleep(2000);
```

- cache() Operator를 추가함으로써 Cold Sequence가 Hot Sequence로 동작하게 됨
  - hot source → Hot Sequence 같은 의미
- cache() Operator는 Cold Sequence로 동작하는 Mono를 Hot Sequence로 변경해주고 emit된 데이터를 캐시한 뒤, 구독이 발생할 때마다 캐시된 데이터를 전달함

  - 구독 발생시마다 Subscriber는 동일한 데이터를 전달받게 됨

- cache() 활용 예시 → Rest API 요청을 위한 인증 토큰이 필요한 경우

  - getAuthToken() 메서드 호출해서 API서버로부터 인증 토큰을 전달받는다면 토큰이 만료되기 전까지 해당 인증 토큰을 사용해 인증이 필요한 API요청에 사용가능
  - getAuthToken() 메서드 호출시마다 API서버 쪽에서 매번 새로운 인증토큰 전송함 → 불필요한 HTTP 요청
    - 이를 방지하기 위해 cache() 사용해서 캐시된 인증토큰 사용, 효율적 동작과정 구성가능

- +) Reactor에서 Hot의 두 가지 의미
  - 최초 구독이 발생하기 전까지 데이터 emit발생 x (Warm up)
  - 구독 여부와 상관없이 데이터가 emit되는 것 (Hot)
  - share()의 경우 최초 구독이 발생했을 때 데이터를 emit하는 warm up의 의미를 가지는 Hot Sequence다
