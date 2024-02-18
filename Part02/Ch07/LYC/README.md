# Cold / Hot Sequence

- Cold / Hot의 의미
    
    Cold는 무언가를 새로 시작, Hot은 무언가를 새로 시작하지 않습니다.
    
- Sequence의 의미
    
    Publisher가 emit하는 데이터의 연속적인 흐름을 정의해 놓은 것으로 코드로 표현하면 Operator 체인 형태로 정의됩니다.
    
- Cold Sequence
    
    Subscriber가 구독할 때마다 데이터 흐름이 처음부터 다시 시작되는 Sequence입니다.
    
    ![image.png](https://file.notion.so/f/f/9e132be8-6c00-4f39-b489-0bf5bacbff36/8da452e1-e786-40a8-944e-6dcbc40549d0/image.png?id=da552241-b0ae-410e-9d8c-9ec33b789bd3&table=block&spaceId=9e132be8-6c00-4f39-b489-0bf5bacbff36&expirationTimestamp=1708365600000&signature=5Ab9ALns2UMvO-sDMAmnXjN8okAUdCvRVyUKoVA2hgo&downloadName=image.png)
    
    위의 그림을 보면, A와 B의 구독 시점이 다른 것을 볼 수 있습니다. 하지만 구독 시점이 달라도 양측 모두 동일한 데이터를 전달받습니다. 이처럼 각 Subscriber마다 구독 시점이 달라도 구독할 때마다 데이터를 emit하는 과정을 처음부터 다시 시작하는 과정을 Cold Sequence라고 합니다.
    
    ```java
    public static void main(String[] args) {
        Flux<String> coldFlux = Flux.fromIterable(Arrays.asList("KOREA", "JAPAN", "USA"))
                .map(String::toLowerCase);
    
        coldFlux.subscribe(country -> log.info("구독 1 : {}", country));
        log.info("-------------------------------");
    		Thread.sleep(2000L);
        coldFlux.subscribe(country -> log.info("구독 2 : {}", country));
    }
    
    구독 1 : KOREA
    구독 1 : JAPAN
    구독 1 : USA
    ----------------------
    구독 2 : KOREA
    구독 2 : JAPAN
    구독 2 : USA
    ```
    

- Hot Sequence
    
    Hot Seuquence는 구독이 발생한 시점 이전에 Publisher로부터 emit된 데이터는 Subscriber가 전달받지 못하고 구독이 발생한 시점 이후에 emit된 데이터만 전달받을 수 있습니다.
    
    ![image (1).png](https://file.notion.so/f/f/9e132be8-6c00-4f39-b489-0bf5bacbff36/55111b64-5fad-4af6-9f9c-4f6ad5ff5f2f/image_(1).png?id=0067a5a9-cb14-4083-86bf-6a7d4f324cb4&table=block&spaceId=9e132be8-6c00-4f39-b489-0bf5bacbff36&expirationTimestamp=1708365600000&signature=J0flRD2qglM2xi6UpatqY7zVbdIvAAiOYluxUuk4-CI&downloadName=image+%281%29.png)
    
    그림을 확인하면, 세 차례의 구독이 발생함을 볼 수 있습니다. 하지만 타임라인은 하나밖에 생성되지 않았습니다. 이 말은 즉슨 구독이 아무리 발생해도 Publisher가 데이터를 처음부터 emit하지 않는다는 것을 의미합니다.
    
    ```java
    public static void main(String[] args) throws InterruptedException {
        Flux<String> concertflux = Flux
                .fromStream(Stream.of("Singer A", "Singer B", "Singer C", "Singer D", "Singer E"))
                // delay 를 사용해 1초에 한번씩 data 를 publish 하도록 설정
                .delayElements(Duration.ofSeconds(1))
                // 하나의 타임라인으로 다수의 subscriber 가 공유하도록 설정
                .share();
    
        concertflux.subscribe(singer -> System.out.println("구독 1 : " + singer));
    
        Thread.sleep(2500);
    
        concertflux.subscribe(singer -> System.out.println("구독 2 : " + singer));
    
        // 요청이 종료되지 않고 모든 타임라인이 끝날 때 까지 지연시킴
        Thread.sleep(3000);
    }
    
    구독 1 : Singer A
    구독 1 : Singer B
    구독 1 : Singer C
    구독 2 : Singer C
    구독 1 : Singer D
    구독 2 : Singer D
    구독 1 : Singer E
    구독 2 : Singer E
    ```
    
    delayElements() Operator는 데이터 소스로 입력된 각 데이터의 emit을 일정시간 동안 지연시키는 연산자입니다.
    
    위 처럼, Hot Sequence가 가능한 이유는 share() Operator 연산자를 
    
    사용했기 때문입니다. 공식문서를 번역해보면 
    
    ‘share()는 원본 Flux를 멀티캐스트 하는 새로운 Flux를 리턴한다’로 정의합니다. 즉 원본 Flux라는 의미는 Operator를 통해 가공되지 않은 원본 데이터 소스를 처음으로 emit하는 Flux를 의미합니다. 이 원본 Flux를 멀티캐스트 한다는 의미는 여러 Subscriber가 하나의 원본 Flux를 공유(멀티캐스트)한다는 의미입니다.
    

- HTTP 요청과 응답에서 Cold Sequence
    
    ```java
    import com.jayway.jsonpath.DocumentContext;
    import com.jayway.jsonpath.JsonPath;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.web.reactive.function.client.WebClient;
    import org.springframework.web.util.UriComponentsBuilder;
    import reactor.core.publisher.Mono;
    
    import java.net.URI;
    
    @Slf4j
    public class Example {
        public static void main(String[] args) throws InterruptedException {
            URI worldTimeUri = UriComponentsBuilder.newInstance().scheme("http")
                    .host("worldtimeapi.org")
                    .port(80)
                    .path("/api/timezone/Asia/Seoul")
                    .build()
                    .encode()
                    .toUri();
    
            Mono<String> mono = getWorldTime(worldTimeUri);
            mono.subscribe(dateTime -> log.info("# dateTime 1: {}", dateTime));
            Thread.sleep(2000);
            mono.subscribe(dateTime -> log.info("# dateTime 2: {}", dateTime));
    
            Thread.sleep(2000);
        }
    
        private static Mono<String> getWorldTime(URI worldTimeUri) {
            return WebClient.create()
                    .get()
                    .uri(worldTimeUri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> {
                        DocumentContext jsonContext = JsonPath.parse(response);
                        String dateTime = jsonContext.read("$.datetime");
                        return dateTime;
                    });
        }
    }
    ```
    
    위의 코드는 Cold Sequence에서의 HTTP 요청/응답 동작 과정 코드입니다.
    
    먼저, Non-blocking 통신을 위해 WebClient를 사용하였습니다.
    
    WebClient는 비동기적인 방식으로 HTTP 리소스를 소비하는 데 사용되는 클라이언트 라이브러리입니다. 이는 기존의 RestTemplate과 비슷한 기능을 제공하지만, 논블로킹 및 리액티브 프로그래밍 모델을 따릅니다.
    
    getWorldTime() 메서드를 통해 리턴 값으로 Mono 타입의 dateTime을 전달받습니다. 구독이 발생하지 않으면 데이터 emit이 일어나지 않기 때문에 첫 구독 이후 2초가 지연 시점에 두 번째 구독을 발생시킵니다. 두 번의 구독이 발생하게 되므로, 두 번의 새로운 HTTP 요청을 발생시킵니다.
    
- HTTP 요청과 응답에서 Hot Sequence
    
    ```java
    import com.jayway.jsonpath.DocumentContext;
    import com.jayway.jsonpath.JsonPath;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.web.reactive.function.client.WebClient;
    import org.springframework.web.util.UriComponentsBuilder;
    import reactor.core.publisher.Mono;
    
    import java.net.URI;
    
    @Slf4j
    public class Example {
        public static void main(String[] args) throws InterruptedException {
            URI worldTimeUri = UriComponentsBuilder.newInstance().scheme("http")
                    .host("worldtimeapi.org")
                    .port(80)
                    .path("/api/timezone/Asia/Seoul")
                    .build()
                    .encode()
                    .toUri();
    
            Mono<String> mono = getWorldTime(worldTimeUri).cache();
            mono.subscribe(dateTime -> log.info("# dateTime 1: {}", dateTime));
            Thread.sleep(2000);
            mono.subscribe(dateTime -> log.info("# dateTime 2: {}", dateTime));
    
            Thread.sleep(2000);
        }
    
        private static Mono<String> getWorldTime(URI worldTimeUri) {
            return WebClient.create()
                    .get()
                    .uri(worldTimeUri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> {
                        DocumentContext jsonContext = JsonPath.parse(response);
                        String dateTime = jsonContext.read("$.datetime");
                        return dateTime;
                    });
        }
    }
    ```
    
    Cold Seqeunce와 다르게 Hot Sequence에서는 cache() 라는 Operator가 추가됨을 확인할 수 있습니다. 
    
    cache() 연산자는 리액티브 스트림에서 중간에 사용되어 결과를 캐시하고, 이후 구독자들이 해당 결과를 재사용할 수 있도록 합니다. 이를 통해 동일한 데이터에 대한 중복된 요청을 방지하고, 네트워크 또는 비용이 많이 드는 작업의 결과를 효율적으로 재활용할 수 있습니다.
    
    cache() 연산자는 기본적으로 구독자가 처음으로 구독할 때까지 아무 작업도 수행하지 않고, 구독자가 처음 요청한 결과를 캐시합니다. 그 후에는 이전에 캐시된 결과를 반환하고, 새로운 구독자가 등록되더라도 새로운 요청을 하지 않고 캐시된 결과를 전달합니다. 즉 Cold Sequence와 다르게 구독이 발생할 때마다 캐시된 데이터를 전달합니다.
    
    첫 번째 구독을 통해 응답 데이터를 캐시한  이 후 두번째 구독에서 캐시된 데이터를 전달하기 때문에 출력된 시간이 동일하게 됩니다.
    
    ⭐ cache()를 잘 활용한 예로 대표적으로 REST API 요청을 위해서 인증 토큰이 필요한 경우가 있습니다. 
    
    예를 들어, getAuthToken() 이라는 메서드를 호출하여 API서버로부터 인증 토큰을 전달받을 때 토큰이 만료되기 전까지 인증 토큰을 사용하여 인증이 필요한 API 요청에 사용할 수 있습니다.
    
    만약 getAuthToken() 메서드를 호출할 떄마다 서버에 요청한다면, 불필요한 트래픽이 증가하게 됩니다. 이를 방지하기 위해 캐시하여 저장된 인증 토큰을 사용하여 효율적으로 제어를 할 수 있습니다.