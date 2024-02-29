# Context

- 콘텍스트란?
    
    어떤 것을 이해하는 데 도움이 될 만한 관련 정보나 이벤트, 상황을 의미합니다.
    
    즉, 어떠한 상황에서 그 상황을 처리하기 위해 필요한 정보를 말합니다.
    
- Reactor에서 Context가 중요한 이유
    1. **비동기성(Asynchrony) 관리**: 리액티브 프로그래밍은 비동기적인 이벤트 스트림을 다루는 것이 특징입니다. 즉, 여러 이벤트가 동시에 발생할 수 있고, 이에 대응하여 적절한 처리를 해야 합니다. 이때 콘텍스트는 각 이벤트가 발생한 시점이나 상황을 이해하고 관리하는 데 도움을 줍니다.
    2. **상태 관리(State Management)**: 리액티브 시스템에서는 상태의 변화를 효율적으로 관리해야 합니다. 이때 콘텍스트는 각 상태 변경의 맥락을 제공하여 예상치 못한 동작이나 상태 관리의 어려움을 최소화합니다.
    3. **에러 핸들링(Error Handling)**: 비동기적인 환경에서는 에러가 발생할 가능성이 높습니다. 이때 콘텍스트는 에러가 발생한 위치와 그 원인을 명확히 파악할 수 있도록 도와줍니다.
    4. **스레드 관리(Thread Management)**: 리액티브 프로그래밍은 종종 멀티스레드 환경에서 동작합니다. 이때 콘텍스트는 각 작업이 어떤 스레드에서 실행되고 있는지를 추적하고, 스레드 간의 데이터 공유와 동기화를 관리하는 데 도움을 줍니다.
    
- 콘텍스트 활용예제
    
    ```java
    @Slf4j
    public class Example11_1 {
    
        static Logger log = LoggerFactory.getLogger(Example11_1.class);
    
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
    
    ///
    16:33:40.969 [boundedElastic-1] INFO reactive.Example11_1 -- # just doOnNext : Hello Steve
    16:33:40.975 [parallel-1] INFO reactive.Example11_1 -- # onNext: Hello Steve Jobs
    ```
    
    - contextWrite() Operator의 파라미터는 함수형 인터페이스이고, 람다 표현식으로 표현할 경우 람다 파라미터의 타입이 Context이고, 리턴 값 역시 Context입니다. 이 Operator()를 통해 데이터를 쓰는 작업을 ㅊ리할 수 있는데, 실제로 데이터를 쓰는 동작은 Context API 중 하나인 put()을 통해서 쓸 수 있습니다.
    - deferContextual()은 Context에 저장된 데이터와 원본 데이터 소스의 처리를 지연하는 역할을 합니다. 우선 원본 데이터 소스 레벨에서 Context의 데이터를 읽기 위해 사용한다는 정도만 알아도 무방할 것 같습니다.
    - Reactor에서는 Operator 체인 상의 서로 다른 스레드들이 Context의 저장된 데이터에 손쉽게 접근할 수 있습니다.

- Context를 사용한 인증된 도서 관리자가 신규 도서를 등록하기 위해 도서 정보와 인증 토큰을 서버로 보내는 예제
    
    ```java
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.extern.slf4j.Slf4j;
    import reactor.core.publisher.Mono;
    import reactor.util.context.Context;
    
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