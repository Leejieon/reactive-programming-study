# 11 Context

- Reactor Sequence에서 상태 값을 저장하고 저장된 상태값을 사용할 수 있게 해주는 애

## 11.1 Context란?

- 어떤 것을 이해하는 데 도움이 될 만한 관련 정보나 이벤트, 상황
- 의사 선생님이 아기의 병명 진단을 위해 필요한 정보는 요구하는 것

  - 어떤 상황에서 **그 상황을 처리하기 위해 필요한 정보를 제공**하는 어떤 것(인터페이스 or 클래스)
  - ServletContext는 Servlet이 Servlet Container와 통신하기 위해서 필요한 정보를 제공하는 인터페이스
  - Spring Framework에서 ApplicationContext는 애플리케이션 정보를 제공하는 인터페이스. APplicationContext가 제공하는 대표적인 정보 → Spring bean 객체
  - Spring Security에서 SecurityContextHolder = SecurityContext를 관리하는 주체,
    - SecurityContext는 애플리케이션 사용자의 인증 정보를 제공하는 인터페이스

- Reactor API에서 Context
  - (Operator같은) Reactor 구성요소 간 전파되는 **key/value 형태의 저장소**
  - 전파 = Downstream에서 Upstream으로 Context가 전파되어 Operator 체인상의 각 Operator가 해당 Context의 정보를 동일하게 이용할 수 있다!
- Reactor의 Context는 ThreadLocal과 다소 유사한 면이 있지만
  - 각각의 실행 스레드와 매핑되는 ThreadLocal
  - Reactor의 Context는 실행 스레드와 매핑 ㄴㄴ **Subscriber와 매핑됨**
  - **구독이 발생할 때마다** 해당 구독과 연결된 **하나의 Context가 생긴다!**

```java
Mono
	.deferContextual(ctx ->
		Mono
			.just("Hello" + " " + ctx.get("firstName"))
			.doOnNext(data -> log.info(" # just doOnNext : {}", data))
	)
	.subscribeOn(Schedulers.boundedElastic())
	.publishOn(Schedulers.parallel())
	.transformDeferredContextual(
			(mono, ctx) -> mono.map(data -> data + " " + ctx.get("lastName"))
	)
	.contextWrite(context -> context.put("lastName", "Jobs"))     //쓰기
	.contextWrite(context -> context.put("firstName", "Steve"))
	.subscribe(data -> log.info("# onNext: {}", data));

Threads.sleep(100L);
```

- Context에 key/value 형태의 데이터를 저장할 수 있다 → 데이터 읽기/쓰기 가능하다
  - Context에는 “Steve”, “Jobs” 두 개의 데이터가 저장됨

### 컨텍스트에 데이터 쓰기

- **contextWrite()**
- 실제로 데이터를 쓰는 동작은 Context API중 하나인 put()을 통해 쓸 수 ㅇ

### 컨텍스트 데이터 읽기

- 읽는방법

  1. 원본 데이터 소스레벨에서 읽기: **deferContextual()**
  2. Operator 체인의 중간에서 읽기: **transformDeferredContextual()**

- deferContextual()

  - defer() 와 같은 원리로 동작함
  - Context에 저장된 데이터와 원본 데이터 소스의 처리를 지연시키는 역할
  - 파라미터 ctx는 ContextView 타입 객체임
  - Context에 **데이터를 쓸 때는 Context**를 사용, Cotnext에 저장된 **데이터를 읽을 때는 ContextView를 사용**함
  - ContextView의 get()을 이용해 Context에 저장된 firstName 키 해당 값을 읽어옴

- 데이터를 emit하는 스레드와 데이터를 처리하는 스레드 분리함
  - Context에서 데이터 읽어오는 작업 각각 다른 스레드에서 수행
  - Reactor에서는 **Operator 체인상의 서로 다른 스레드들**이 **Cotnext에 저장된 데이터에 손쉽게 접근가능**
- context.put() 을 통해 Context에 데이터를 쓴 후 매번 불변 객체를 다음 contextWrite() 에 전달 → **스레드 안정성을 보장함**

## 11.2 자주 사용되는 Context 관련 API

- **put(key, value)**: key/value 형태로 Context에 값을 씀
- **of(key1, value1, key2, value2)**: key/value 형태로 Context에 여러 개의 값을 씀
  - 최대 5개의 데이터를 파라미터로 입력가능
- **putAll(ContextView)**: 현재 Context와 파라미터로 입력된 ContextView를 merge
  - 6개 이상의 데이터를 쓰기 위한 것
  - merge한 새로운 Context를 생성함
- **delete(key)**: Context에서 key에 해당하는 value 삭제

```java
final String key1= "company";
final String key2= "firstName";
final String key3= "lastName";

Mono
	.deferContextual(ctx ->
				Mono.just(ctx.get(key1) + ", " ctx.get(key2) + " " + ctx.get(key3))
	)
	.publishOn(Schedulers.parallel())
	.contextWrite(context ->
				context.putAll(Context.of(key2, "Steve", key3, "Jobs").readOnly())
	)
	.contextWrite(context -> context.put(key1, "Apple"))
	.subscribe(data -> log.info("# onNext: {}", data));

Thread.sleep(100L);
```

- 3개의 데이터를 Context에 씀
  - put(), putAll()
  - Context.of()사용함 → Context객체를 반환
  - 해당 Context 객체를 ContextView로 변환해줘야함 → **readOnly() API**를 통해서 이루어짐
    - Context를 읽기 작업만 가능한 ContextView로 변환해주는 API

### 자주 사용되는 ContextView API

- **get(key)**: ContextView에서 key에 해당하는 value반환
- **getOrEmpty(key)**: ContextView에서 key에 해당하는 value를 Optional로 매핑
- **getOrDefault(key, default value)**: key 해당하는 value없으면 default value 가져옴
- **hashKey(key)**: ContextView에서 특정 key 존재여부 확인
- **isEmpty()**: Context가 비어있는지 확인
- **size()**: Context 내에 있는 key/value의 개수

- Context에 저장된 데이터를 읽으려면 ContextView API를 사용해야함
  - ContextView API 사용 방법 ㅇㅇMap이다~

```java
final String key1= "company";
final String key2= "firstName";
final String key3= "lastName";

Mono
	.deferContextual(ctx ->
				Mono.just(ctx.get(key1) + ", " +
							ctx.getOrEmpty(key2).orElse("no firstName") + " " +
							ctx.getOrDefault(key3, "no lastName"))
	)
	.publishOn(Schedulers.parallel())
	.contextWrite(context -> context.put(key1, "Apple"))
	.subscribe(data -> log.info("# onNext: {}", data));

Thread.sleep(100L);
```

1. company 키에 해당하는 context를 씀
2. 3개의 키에 해당하는 데이터를 읽음

## 11.3 Context의 특징

- Context API는 Operator 체인상에서 Context에 데이터를 읽고 쓰는게 전부 ㅇㅇ 심플함
- Context API를 사용함에 있어 주의해야할 특징!!

<**구독이 발생할 때마다 하나의 Context가 해당 구독에 연결**됨>

```java
final String key1 = "company";

Mono<String> mono = Mono.deferContextual(ctx ->
								Mono.just("Company: " + " " + ctx.get(key1))
					)
					.publishOn(Schedulers.parallel());

mono.contextWrite(context -> context.put(key1, "Apple"))
		.subscribe(data -> log.info("# subscribe1 onNext: {}", data));

mono.contextWrite(context -> context.put(key1, "MS"))
		.subscribe(data -> log.info("# subscribe1 onNext: {}", data));

Thread.sleep(100L);
```

- 첫 번째 구독 → Context에 Apple 저장
- 두 번째 구독 → Context에 MS 저장
  - Context는 구독별로 연결됨
  - 구독이 발생할 때마다 해당하는 하나의 Context가 하나의 구독에 연결굄

<**Context는 Operator 체인의 아래에서 위로 전파**된다>

<동일한 키에 대한 값을 중복저장 → **Operator 체인에서 가장 위쪽에 위치한 contextWrite()이 저장한 값으로 덮어쓴다**>

```java
String key1 = "company";
String key2 = "name";

Mono
	.deferContextual(ctx ->
			Mono.just(ctx.get(key1))
	)
	.publishOn(Schedulers.parallel())
	.contextWrite(context -> context.put(key2, "Bill"))
	.transformDeferredContextual((mono, ctx) ->
				mono.map(data -> data + ", " + ctx.getOrDefault(key2, "Steve"))
	)
	.contextWrite(context -> context.put(key1, "Apple"))
	.subscribe(data -> log.info("# onNext: {}", data));

Thread.sleep(100L);
```

- context에 2개의 데이터 저장
  - Apple 회사명 저장/ Bill 이름 저장
  - ContextView 객체를 통해 company 키에 해당하는 값을 downstream으로 emit함
- Apple이랑 Steve를 출력!
- Context의 경우 **Operator 체인 상의 아래에서 위로 전파되는 특징**이 있음

- getOrDefault() API 이 시점에 name 키에 해당하는 값이 Context에 없음
  - getOrDefault() 디폴트 값이 Subscriber에게 전달
  - NoSuchElementException 발생가능
- 일반적으로 모든 Operator에서 Context에 저장된 데이터를 읽을 수 있도록 **contextWrite()을 Operator 체인의 맨 마지막에 둠**
- Operator 체인상에 동일한 키를 가지는 값을 각각 다른 값으로 저장
  - → Context가 아래에서 위로 전파되는 특성 때문에 **Operator 체인상에서 가장 위쪽에 위치한 contextWrite() 저장 값으로 덮어쓴다**

<**Inner Sequence 내부에서는 외부 Context에 저장된 데이터를 읽을 수 있다**>

<**Inner Sequence 외부에서는 Inner Sequence 내부 Context에 저장된 데이터를 읽을 수 없다**>

```java
String key1 = "company";
Mono
		.just("steve")
//				.tranformDeferredContextual((stringMono, ctx) ->
//								ctx.get("role"))
		.flatMap(name ->
				Mono.deferContextual(ctx ->
					Mono
						.just(ctx.get(key1) + ", " + name)
						.transformDeferredContextual((mono, innerCtx) ->
									mono.map(data -> data + ", " + innerCtx.get("role"))
						)
						.contextWrite(context -> context.put("role", "CEO")) //inner
				)
		)
		.publishOn(Schedulers.parallel())
		.contextWrite(context -> context.put(key1, "Apple"))
		.subscribe(data -> log.info("# onNext: {}", data)); //Apple, steve, CEO

Thread.sleep(100L);
```

- Context에 데이터를 2번 씀
  - flatMap() 내부에 존재하는 Operator 체인에서 값을 쓰고있음
  - flatMap() 내부에 있는 Sequence = Inner Sequence
  - Inner Sequence에서는 바깥쪽 sequence에 연결된 Context의 값을 읽을 수 있음
- 주석 해제 → NoSuchElementException 발생
  - Context에 role 키가 없어서 ㅇㅇ
  - Inner Sequence 외부에서는 Inner Sequence 내부 context에 저장된 데이터를 읽을 수 없다.

```java
public static final String HEADER_AUTH_TOKEN = "authToken";
public static void main(String[] args) {
	Mono<String> mono =
			postBook(Mono.just(
						  new Book("asbd-1111-3533-2901"
								, "Reactor"
								, "Kevin")
					)
					.contextWrite(Context.of(HEADER_AUTH_TOKEN, "weasdf")
	    );

	mono.subscribe(data -> log.info("# onNext: {}", data));
}

private static Mono<String> postBook(Mono<Book> book) {
	return Mono
					.zip(
							book,
							Mono
								.deferContextual(ctx ->
											Mono.just(ctx.get(HEADER_AUTH_TOKEN)))
					)
					.flatMap(tuple -> {
							String response = "POST the book!!!!!" + "with token: " + tuple.getT2();

							return Mono.just(response);
					})
}
```

- 인증된 도서 관리자가 신규 도서를 등록하기 위해 도서 정보와 인증 토큰을 서버로 전송
- 도서정보인 Book 전송위해 Mono 객체를 postBook() 파라미터로 전달
- zip() 이용해 Mono<Book> 객체와 인증토큰 정보 Mono<String> 객체를 하나의 Mono로 합침
- flatMap() 내부에서 도서 정보를 전송함

Context에 저장한 인증 토큰을 두 개의 Mono로 합치는 과정에서 다시 Context로부터 읽어와서 사용한다!

- Mono를 구독하기 직전에 contextWrite()로 데이터를 저장함 → Operator 체인의 위쪽으로 전파되고, Operator 체인 어느 위치에서든 Context에 접근할 수 있다.

**Context는 인증 정보 같은 직교성(독립성)을 가지는 정보를 전송하는 데 적합함**
