# 6. 마블 다이어그램



## 1. 마블 다이어그램이란?



비동기적인 데이터 흐름을 시간의 흐름에 따라 시각적으로 표시한 다이어그램

![image-20240213221005528](C:\Users\ring9\AppData\Roaming\Typora\typora-user-images\image-20240213221005528.png)

## 2. Mono 및 Flux 활용예제



#### Mono 활용예제

![image](https://github.com/bjpublic/Spring-Reactive/assets/62167266/ece08860-d304-48ff-af7a-11b085fa633f)

```java
/**
 * Mono 기본 개념 예제
 *  - 1개의 데이터를 생성해서 emit한다.
 */
public class Example6_1 {
    public static void main(String[] args) {
        Mono.just("Hello Reactor")
                .subscribe(System.out::println);
    }
}
```

위 코드는 Mono를 사용해 "Hello Reactor"를 출력하는 예제이다.

이처럼 데이터 하나만을 출력(Emit) 하는데 Mono를 사용한다. 이때 just operator를 사용하는데 2개이상의 파라미터를 줄 경우 내부적으로 fromArray() Operator를 이용해데이터를 emit한다.



 ```java
 
 /**
  * Mono 기본 개념 예제
  *  - 원본 데이터의 emit 없이 onComplete signal 만 emit 한다.
  */
 public class Example6_2 {
     public static void main(String[] args) {
         Mono
             .empty()
             .subscribe(
                     none -> System.out.println("# emitted onNext signal"),
                     error -> {},
                     () -> System.out.println("# emitted onComplete signal")
             );
     }
 }
 ```

실행결과로는 `emitted onComplete signal` 을 얻을 수 있는데, empty() Operator의 특성상 내부적으로 emit할 데이터가 없는것으로 판단하여 곧바로  onComplete Signal를 전송한다.

-> 작업이 끝났음을 알리고 후처리 할때 보통 사용





```java

/**
 * Mono 활용 예제
 *  - worldtimeapi.org Open API를 이용해서 서울의 현재 시간을 조회한다.
 */
public class Example6_3 {
    public static void main(String[] args) {
        URI worldTimeUri = UriComponentsBuilder.newInstance().scheme("http")
                .host("worldtimeapi.org")
                .port(80)
                .path("/api/timezone/Asia/Seoul")
                .build()
                .encode()
                .toUri();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));


        Mono.just(
                    restTemplate
                            .exchange(worldTimeUri,
                                    HttpMethod.GET,
                                    new HttpEntity<String>(headers),
                                    String.class)
                )
                .map(response -> {
                    DocumentContext jsonContext = JsonPath.parse(response.getBody());
                    String dateTime = jsonContext.read("$.datetime");
                    return dateTime;
                })
                .subscribe(
                        data -> System.out.println("# emitted data: " + data),
                        error -> {
                            System.out.println(error);
                        },
                        () -> System.out.println("# emitted onComplete signal")
                );
    }
}
```

위 코드에서 Mono.just~ 부터는 어플리케이션 외부시스템의 API를 호출해서 응답으로 수신한 데이터를 전달함

(이런예제가 많이 쓰일것 같은데 아직 이해안감) +  이코드가 Non-Blocking이 아닌 이유?







#### Flux 사용예제

![image](https://github.com/bjpublic/Spring-Reactive/assets/62167266/e913c6dd-d3cf-45fb-be38-3192f8828997)

Flux 마블 다이어그램과 Mono 마블다이어 그램의 차이점은 단지 데이터의 개수이다.



```java
import reactor.core.publisher.Flux;

/**
 * Flux 기본 예제
 */
public class Example6_4 {
    public static void main(String[] args) {
        Flux.just(6, 9, 13)
                .map(num -> num % 2)
                .subscribe(System.out::println);
    }
}
```

Mono랑 비슷



#### Mono + Mono

```java
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 2개의 Mono를 연결해서 Flux로 변환하는 예제
 */
public class Example6_6 {
    public static void main(String[] args) {
        Flux<String> flux =
                Mono.justOrEmpty("Steve")
                        .concatWith(Mono.justOrEmpty("Jobs"));
        flux.subscribe(System.out::println);
    }
}
```

위는 두개의 Mono를 연결해 Flux로 변환하는 예제이다. justOrEmpty()라는 새로운 operator를 사용했는데, just와 비슷하지만 null을 인자로 허용한다는 점에서 차이. 이때 두개의 데이터를 하나의 데이터 소스로 모아 새로운 Flux로 리턴한다.



```java
import reactor.core.publisher.Flux;

/**
 * 여러개의 Flux를 연결해서 하나의 Flux로 결합하는 예제
 */
public class Example6_7 {
    public static void main(String[] args) {
        Flux.concat(
                        Flux.just("Mercury", "Venus", "Earth"),
                        Flux.just("Mars", "Jupiter", "Saturn"),
                        Flux.just("Uranus", "Neptune", "Pluto"))
                .collectList()
                .subscribe(planets -> System.out.println(planets));
    }
}
```

위 예제에서도 마찬가지로 여러 데이터들을 concat을 통해 모은다. 이때 여러데이터가 모이는 특성상 concat의 반환은 Flux형이 되며, collectList()경우 리스트 하나 자체를 반환하기 때문에 Mono를 반환하게된다.
