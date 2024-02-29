# Context

# Context란?

Context는

> 어떠한 상황에서 그 상황을 처리하기 위해 필요한 정보
> 

라고 할 수 있습니다.

프로그래밍에서 몇 가지 예시를 들어보면 다음과 같습니다.

- ServletContext := Servlet이 Servlet Container와 통신하기 위해서 필요한 정보를 제공하는 인터페이스
- ApplicationContext := Spring Framework에서 애플리케이션의 정보를 제공하는 인터페이스
- SecurityContext := Spring Security에서 애플리케이션 사용자의 인증 정보를 제공하는 인터페이스

이처럼 프로그래밍 세계에서 Context는 어떠한 상황을 처리하거나 해결하기 위해 필요한 정보를 제공하는 어떤 것이라고 할 수 있습니다. **Java**에서는 인터페이스 또는 클래스가 되겠네요.

Reactor에서의 Context는 무엇일까요?

Reactor API 문서에서는 다음과 같이 설명하고 있습니다.

![https://blog.kakaocdn.net/dn/cl4Fvt/btsFp9WJpaz/twa09ic31ujSKehkwN36h1/img.png](https://blog.kakaocdn.net/dn/cl4Fvt/btsFp9WJpaz/twa09ic31ujSKehkwN36h1/img.png)

Reactor의 Context는 **Operator 같은 Reactor 구성요소 간에 전파되는 key/value 형태의 저장소**라고 정의하고 있습니다. 여기서 "**전파**"라는 것은

> Downstream에서 Upstream으로 Context가 전파되어 Operator 체인상의 **각 Operator가 해당 Context의 정보를 동일하게 이용**할 수 있는 것
> 

을 의미합니다.

또한, Reactor의 Context는 실행 스레드와 매핑되는 것이 아니라, **Subscriber와 매핑**됩니다.  즉, **구독이 발생할 때마다 해당 구독과 연결된 하나의 Context가 생긴다**고 볼 수 있습니다.

```java
@Slf4j
public class DemoApplication {
	public static void main(String[] args) throws InterruptedException {
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
            .contextWrite(context -> context.put("lastName", "Jobs"))
            .contextWrite(context -> context.put("firstName", "Steve"))
            .subscribe(data -> log.info("# onNext : {}", data));

		Thread.sleep(100L);
	}
}
```

위 코드는 Reactor Sequence 상에서 Context를 사용하는 방법을 이해하기 위한 예제입니다.

Context에 key/value 형태의 데이터를 저장할 수 있다는 의미는 Context에 데이터의 쓰기와 읽기가 가능하다는 의미입니다.

## Context에 데이터 쓰기

먼저 Context에 데이터를 쓰는 방법입니다. 위 코드에서 `contextWrite()` Operator를 사용해 Context에 데이터를 쓰고 있습니다. `contextWrite()` 의 파라미터로 람다 표현식을 전달하고 있는데, 이 Operator의 내부를 살펴봅시다.

![https://blog.kakaocdn.net/dn/bemLhi/btsFqNzcOGw/xrLmkFcqapzOxd4f7kzgn1/img.png](https://blog.kakaocdn.net/dn/bemLhi/btsFqNzcOGw/xrLmkFcqapzOxd4f7kzgn1/img.png)

파라미터는 함수형 인터페이스인데, 람다 표현식으로 표현할 경우 람다 파라미터의 타입이 Context이고, 리턴 값 또한 Context인 것을 확인할 수 있습니다.

다시 코드로 돌아와, `contextWrite()` Operator로 **Context에 데이터를 쓰는 작업을 처리**할 수 있는데, **실제로 데이터를 쓰는 동작**은 Context API 중 하나인 **`put()`**을 통해서 쓸 수 있습니다.

따라서, 이제 구독이 발생하면 Context에는 "Steve"와 "Jobs"라는 두 개의 데이터가 저장될 것입니다.

## Context에 쓰인 데이터 읽기

Context에 쓰인 데이터를 읽는 방식은 크게 두 가지입니다.

1. 원본 데이터 소스 레벨에서 읽는 방식
2. Operator 체인의 중간에서 읽는 방식

## 원본 데이터 소스 레벨에서 읽는 방식

이를 위해서는 **`deferContextual()` Operator**를 사용합니다.

주의해야할 사항은 파라미터로 정의된 람다 표현식의 람다 파라미터는 Context 타입의 객체가 아니라 **ContextView 타입의 객체**입니다.

즉,

> Context에 데이터를 쓸 때는 Context를 사용하지만, Context에 저장된 데이터를 읽을 때는 ContextView를 사용한다
> 

는 사실을 기억합시다.

따라서, 이 ContextView의 `get()` 메서드를 통해 Context에 저장된 "firstName" 키에 해당하는 값을 읽어 옵니다.

## Operator 체인의 중간에서 읽는 방식

이 방식은 `transformDeferredContextual()` Operator를 사용합니다. 위 코드에서는 "lastName" 키에 해당하는 값을 읽어 옵니다.

![https://blog.kakaocdn.net/dn/LBz5L/btsFlWqOqRL/RR7pRwETprDlJ5vL91Or3k/img.png](https://blog.kakaocdn.net/dn/LBz5L/btsFlWqOqRL/RR7pRwETprDlJ5vL91Or3k/img.png)

결과를 보면 Context에 저장된 데이터를 정상적으로 두 번 읽어 오는 것을 알 수 있습니다. 여기서 `subscribeOn()`과 `publishOn()`을 사용해 데이터를 emit하는 스레드와 데이터를 처리하는 **스레드를 분리**했기 때문에 Context에서 데이터를 읽어 오는 작업을 각각 다른 스레드에서 수행했음을 알 수 있습니다.

이처럼 Reactor에서는 **Operator 체인상의 서로 다른 스레드들이 Context의 저장된 데이터에 손쉽게 접근할 수 있**습니다. 또한, `context.put()`을 통해 Context에 데이터를 쓴 후, 매번 **불변 객체**를 다음 `contextWrite()`으로 전달함으로써 **스레드 안전성을 보장**합니다.

# Context 관련 API

## Context API

| Context API | 설명 |
| --- | --- |
| put(key, value) | key/value 형태로 Context에 값을 쓴다. |
| of(key1, value1, key2, value2, ...) | key/value 형태로 Context에 여러 개의 값을 쓴다. |
| putAll(ContextView) | 현재 Context와 파라미터로 입력된 ContextView를 merge한다. |
| delete(key) | Context에서 key에 해당하는 value를 삭제한다. |
- `of()`는 한 번의 API 호출로 여러 개의 데이터를 Context에 쓸 수 있는데, 최대 다섯 개의 데이터를 파라미터로 입력할 수 있습니다.
- `putAll()`의 경우, 현재 Context의 데이터와 파라미터로 입력된 ContextView의 데이터를 합쳐 **새로운 Context를 생성**합니다.

```java
@Slf4j
public class DemoApplication {
	public static void main(String[] args) throws InterruptedException {
		final String key1 = "company";
		final String key2 = "firstName";
		final String key3 = "lastName";
		Mono
				.deferContextual(ctx ->
						Mono.just(ctx.get(key1) + ", " + ctx.get(key2) + " " + ctx.get(key3))
				)
				.publishOn(Schedulers.parallel())
				.contextWrite(context ->
						context.putAll(Context.of(key2, "Steve", key3, "Jobs").readOnly()))
				.contextWrite(context -> context.put(key1, "Apple"))
				.subscribe(data -> log.info("# onNext : {}", data));

		Thread.sleep(100L);
	}
}
```

위 코드는 Context API를 사용하는 예제 코드입니다.

총 세 개의 데이터를 Context에 쓰고 있습니다. 먼저, `put()`을 통해 데이터 한 개를 쓰고 있으면, 나머지 두 개의 데이터를 `putAll()`을 이용해 쓰고 있습니다. 이 두 개의 데이터는 `Context.of()`를 사용해서 `putAll()`의 파라미터로 전달합니다.

여기서, 주목할 부분이 있습니다. `putAll()`의 파라미터는 **ContextView** 객체여야 하는데 `Context.of()`의 리턴 값은 새로운 Context 객체입니다. 따라서 이 Context 객체를 ContextView 객체로 변환하기 위해 `readOnly()` API를 사용합니다. `readOnly()`는 말 그대로 Context를 읽기 작업만 가능한 ContextView로 변환해 주는 API입니다.

![https://blog.kakaocdn.net/dn/blIIzD/btsFlVlafTX/0RkzshcdzeDVKufoCfpR00/img.png](https://blog.kakaocdn.net/dn/blIIzD/btsFlVlafTX/0RkzshcdzeDVKufoCfpR00/img.png)

## ContextView API

| ContextView API | 설명 |
| --- | --- |
| get(key) | ContextView에서 key에 해당하는 value를 반환한다. |
| getOrEmpty(key) | ContextView에서 key에 해당하는 value를 Optional로 래핑해서 반환한다. |
| getOrDefult(key, default value) | ContextView에서 key에 해당하는 value를 가져온다. key에 해당하는 value가 없으면 default value를 가져온다. |
| hasKey(key) | ContextView에서 특정 key가 존재하는지를 확인한다. |
| isEmpty() | Context가 비어 있는지 확인한다. |
| size() | Context 내에 있는 key/value의 개수를 반환한다. |

**Context에 저장된 데이터를 읽기** 위해서는 **ContextView API를 사용**해야 합니다.

```java
@Slf4j
public class DemoApplication {
	public static void main(String[] args) throws InterruptedException {
		final String key1 = "company";
		final String key2 = "firstName";
		final String key3 = "lastName";
		Mono
				.deferContextual(ctx ->
						Mono.just(ctx.get(key1) + ", " +
								ctx.getOrEmpty(key2).orElse("no firstName") + " " +
								ctx.getOrDefault(key3, "no lastName"))
				)
				.publishOn(Schedulers.parallel())
				.contextWrite(context -> context.put(key1, "Apple"))
				.subscribe(data -> log.info("# onNext : {}", data));

		Thread.sleep(100L);
	}
}
```

위 코드는 Java의 Collection API 중 하나인 Map과 유사하기 때문에 실행 결과만 첨부하겠습니다.

![https://blog.kakaocdn.net/dn/degJLj/btsFpPYnbP9/GOZmt8HN6uxQDbtkqGT9l0/img.png](https://blog.kakaocdn.net/dn/degJLj/btsFpPYnbP9/GOZmt8HN6uxQDbtkqGT9l0/img.png)

# Context의 특징

- Context는 구독이 발생할 때마다 하나의 Context가 해당 구독에 연결된다.
- Context는 Operator 체인의 아래에서 위로 전파된다.
- 동일한 키에 대한 값을 중복해서 저장하면 Operator 체인에서 가장 위쪽에 위치한 `contextWrite()`이 저장한 값으로 덮어쓴다.
- Inner Sequence 내부에서는 외부 Context에 저장된 데이터를 읽을 수 있다.
- Inner Sequence 외부에서는 Inner Sequence 내부 Context에 저장된 데이터를 읽을 수 없다.
