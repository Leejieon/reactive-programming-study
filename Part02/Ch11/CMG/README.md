# 11. Context

### Context란?

어떠한 상황에서 그 상황을 처리하게 위해 필요한 정보

ex) Spring Framework에서 Application Context는 애플리케이션의 정보를 제공하는 인터페이스이다.



Reactor에서 Context는 Operator 같은 Reactor 구성요소간에 전파되는 key / value 형태의 저장소라고 정의한다.

여기서 전파는 Downstream에서 Upstream으로 Context가 전파되어 Operator 체인상의 각 Operator가 해당 Context의 정보를 동일하게 이용할 수 있음을 의미한다.

Reactor에서는 구독이 발생할 때마다 해당 구독과 연결된 하나의 Context가 생긴다.



Context 와 단순히 데이터를 보내는것과 차이?



- Context에 데이터 읽기 쓰기

```java
/**
 * Context 기본 예제
 *  - contextWrite() Operator로 Context에 데이터 쓰기 작업을 할 수 있다.
 *  - Context.put()으로 Context에 데이터를 쓸 수 있다.
 *  - deferContextual() Operator로 Context에 데이터 읽기 작업을 할 수 있다.
 *  - Context.get()으로 Context에서 데이터를 읽을 수 있다.
 *  - transformDeferredContextual() Operator로 Operator 중간에서 Context에 데이터 읽기 작업을 할 수 있다.
 */
@Slf4j
public class Example11_1 {
    public static void main(String[] args) throws InterruptedException {
        Mono
            .deferContextual(ctx ->
                Mono
                    .just("Hello" + " " + ctx.get("firstName"))
                    .doOnNext(data -> log.info("# just doOnNext : {}", data))
            )
            .subscribeOn(Schedulers.boundedElastic())
            .publishOn(Schedulers.parallel())
            .transformDeferredContextual(
                    (mono, ctx) -> mono.map(data -> data + " " + ctx.get("lastName"))
            )
            .contextWrite(context -> context.put("lastName", "Jobs"))
            .contextWrite(context -> context.put("firstName", "Steve"))
            .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(100L);
    }
}
```



데이터 읽기의 두가지 방식 : 1) 원본데이터 소스레벨에서 읽는 방식 : `deferContextual()`  2) Operator 체인의 중간에서 읽는 방식











### Context API

- 자주 사용되는 API

![image](https://github.com/bjpublic/Spring-Reactive/assets/62167266/a3283578-d467-4a5b-8ab3-dc691c45b217)



- Context 특징
  - 구독이 발생할 때마다 해당되는 하나의 Context가 하나의 구독에 연결된다.
  - Operator의 체인상의 아래에서 위로 전파되는 특징이 있다. (따라서 ContextWrite로 덮어 쓰여지면 가장 위의 값을 가지게 된다)
  - Inner Sequence 외부에서는 Inner Sequence내부 Context에 저장된 데이터를 읽을 수 없다.



- Context 활용 예제

```java

/**
 * Context 활용 예제
 *  - 직교성을 가지는 정보를 표현할 때 주로 사용된다.
 */
@Slf4j
public class Example11_8 {
    public static final String HEADER_AUTH_TOKEN = "authToken";
    public static void main(String[] args) {
        Mono<String> mono =
                postBook(Mono.just(
                        new Book("abcd-1111-3533-2809"
                                , "Reactor's Bible"
                                ,"Kevin"))
                )
                .contextWrite(Context.of(HEADER_AUTH_TOKEN, "eyJhbGciOi"));

        mono.subscribe(data -> log.info("# onNext: {}", data));

    }

    private static Mono<String> postBook(Mono<Book> book) {
        return Mono
                .zip(book,
                        Mono
                            .deferContextual(ctx ->
                                    Mono.just(ctx.get(HEADER_AUTH_TOKEN)))
                )
                .flatMap(tuple -> {
                    String response = "POST the book(" + tuple.getT1().getBookName() +
                            "," + tuple.getT1().getAuthor() + ") with token: " +
                            tuple.getT2();
                    return Mono.just(response); // HTTP POST 전송을 했다고 가정
                });
    }
}

@AllArgsConstructor
@Data
class Book {
    private String isbn;
    private String bookName;
    private String author;
}
```

위 예제처럼 COntext는 인증정보같은 독립성을 가지는 정보를 전송하는데 적합하다!