# 마블 다이어그램(Marble Diagram)이란?

마블 다이어그램은

> 여러 가지 구슬 모양의 도형으로 구성된 도표

로 Reactor에서 지원하는 Operator를 이해하는 데 중요한 역할을 합니다. 

> 비동기적인 데이터 흐름을 시간의 흐름에 따라 시각적으로 표시한 다이어그램

이라고 이해 할 수 있습니다. 

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FlfdCF%2FbtsERA2l3oH%2FmZrAaAp9yZXPwEBD6GWaOk%2Fimg.png)

위 그림은 마블 다이어그램의 기본 구성을 설명한 그림입니다. 하나씩 살펴 봅시다.

1.  다이어그램에는 두 개의 타임라인이 존재하는데, 첫째가 **1**과 같이 **Publisher가 데이터를 emit하는 타임라인**입니다. Operator 함수(**5**)를 기준으로 상위에 있는, 즉 **Upstream의 Publisher**입니다. Reactor에서 Flux의 경우, Source Flux라고 부릅니다. 
2.  **2**는 **Publisher가 emit하는 데이터**를 의미합니다. 타임라인은 왼쪽에서 오른쪽으로 시간이 흐르는 것을 의미하기 때문에 가장 왼쪽에 있는 녹색 구슬이 시간상 가장 먼저 emit 된 데이터입니다. 
3.  **3**의 수직으로 된 바는 **데이터의 emit이 정상적으로 끝났음**을 의미합니다. `onComplete` Signal에 해당됩니다. 
4.  **4**와 같이 Operator 함수 쪽으로 들어가는 점선 화살표는 Publisher로부터 emit 된 **데이터가 Operator 함수의 입력으로 전달되는 것**을 의미합니다. 
5.  **5**는 Publisher로부터 전달받은 **데이터를 처리하는 Operator 함수**입니다. 
6.  **6**과 같이 Operator 함수에서 나가는 점선 화살표는 데이터를 가공 처리한 후에 **출력으로 보내는 것**을 의미합니다. 정확히 표현하자면, Operator 함수에서 리턴하는 _새로운 Publisher_ 를 이용해 **Downstream**에 가공된 데이터를 전달하는 것을 의미합니다. 
7.  **7**에서의 타임라인은 Operator 함수에서 가공 처리되어 **출력으로 내보내진 데이터의 타임라인**입니다. Operator의 출력으로 리턴된 Flux의 경우, Output Flux라고 부릅니다. 
8.  **8**은 Operator 함수에서 **가공 처리된 데이터**를 의미합니다. 
9.  **9**와 같은 X 표시는 **에러가 발생해 데이터 처리가 종료되었음**을 의미합니다. `onError` Signal에 해당됩니다. 

---

# Publisher with Marble Diagram

마블 다이어그램을 통해 Publisher인 `Mono` 와 `Flux`에 대해 조금 더 구체적으로 알아봅시다. 

## Mono

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FdrO7UM%2FbtsENUOdrYQ%2FMK1GDcuBzkZ3oirHNTkbu0%2Fimg.png)

위 그림은 Reactor Documentation 에서 `Mono`를 마블 다이어그램으로 표현해 놓은 그림입니다. 

`Mono`는 **단 하나의 데이터를 emit** 하는 Publisher이기 때문에 위 그림에서도 단 하나의 데이터만 표현합니다. 정확히 말하면 0개 또는 1개의 데이터를 emit 합니다. 따라서, 데이터가 emit 되지 않고 `onComplete` Signal만 전송될 수 있습니다. 

다음 코드들을 통해 어떤 식으로 `Mono`를 사용할 수 있는지 알아봅시다. 

```java
public static void main(String[] args) {
    Mono.just("Hello Reactor")
            .subscribe(System.out::println);
}
```

위와 같이 "Hello Reactor"처럼 데이터 한 건을 emit하기 위해서 `Mono`를 사용할 수 있습니다. 

`just()` Operator는 **한 개 이상의 데이터를 emit** 하기 위한 대표적인 Operator입니다. 2개 이상의 데이터를 파라미터로 전달할 경우, 아래와 같이 내부적으로 `fromArray()` Operator를 이용해 데이터를 emit 합니다. 

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbAXcsv%2FbtsEP3RtB4K%2FHLNknnmAsZzkil6NsHX7OK%2Fimg.png)

다음 코드를 봅시다.

```java
public static void main(String[] args) {
    Mono.empty()
            .subscribe(
                    none -> System.out.println("# emitted onNext signal"),
                    error -> {
                    },
                    () -> System.out.println("# emitted onComplete signal")
            );
}
```

위 코드는 데이터를 한 건도 emit 하지 않는 코드입니다. `empty()` Operator를 사용하면 데이터를 emit 하지 않고 `onComplete` Signal을 전송합니다. 위 코드의 실행 결과는 아래와 같습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fx850w%2FbtsEOS3NPy4%2FXWBh2aWFxgWlqAcXBv7njK%2Fimg.png)

실행 결과를 보면, 세 번째 람다 표현식이 실행되는 것을 알 수 있습니다. `map()` Operator가 있더라도 `empty()` Operator를 사용하면 내부적으로 **emit 할 데이터가 없는 것으로 간주**하고 곧바로 `onComplete` Signal을 전송합니다. 

`empty()` Operator는

> 주로 작업이 끝났음을 알리고 이에 따른 후처리를 하고 싶은 경우

사용합니다. 

위 코드의 `subscribe()` 메서드에 대해 조금 더 설명하자면 다음과 같습니다. 

-   첫 번째 람다 표현식 := Publisher가 `onNext` Signal을 전송하면 실행. Subscriber가 Publisher로부터 데이터를 전달받기 위해 사용.
-   두 번째 람다 표현식 := Publisher가 `onErro` Signal을 전송하면 실행. 데이터를 전송하는 도중에 에러가 발생하는 경우.
-   세 번째 람다 표현식 := Publisher가 `onComplete` Signal을 전송하면 실행. 이를 통해 Publisher으 데이터 emit이 종료되었음을 알림.

이번에는 `Mono`를 활용하는 예제를 살펴 보겠습니다.

[worldtimeapi.org](https://worldtimeapi.org/)의 Open API를 이용해 서울의 현재 시간을 조회하는 코드입니다. 외부 시스템의 API 호출을 통해 데이터를 요청하도록 했습니다. 

```java
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
```

코드에서 Mono의 `just()` Operator에 외부 시스템의 API를 호출해서 응답으로 수신한 데이터를 전달합니다. 여기서 외부 시스템으로 [World Time Open API](https://worldtimeapi.org/)를 사용해 현재 날짜와 시간을 JSON 형태의 응답으로 수신합니다. 그리고 `map()` Operator에서 응답으로 수신한 JSON 형태의 데이터를 파싱해 현재 날짜/시간 정보를 Subscriber에게 전달한 후, 로그로 출력합니다. 

위 코드의 실행 결과는 다음과 같습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fbl4A0Y%2FbtsEM0ud6fS%2F5krPGxuQMHfY1Rtzflsy4k%2Fimg.png)

위 예제처럼, `Mono`는 단 한 건의 데이터를 응답으로 보내는 HTTP 요청/응답에 사용하기 매우 적합한 Publihser 타입입니다.

## Flux

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FQ0Oc6%2FbtsEOSiocos%2FAeOg6JCsXoMXuce5YeFS8k%2Fimg.png)

위 그림은 Reactor Documentation 에서 `Flux`를 마블 다이어그램으로 표현해 놓은 그림입니다. 

`Flux`는 Mono 와 다르게 **여러 건의 데이터를 emit 할 수 있는 Publisher 타입**입니다. 따라서, 구슬 모양의 데이터가 한 개인 Mono와 달리 `Flux`의 마블 다이어그램에서는 emit 되는 구슬 모양의 데이터가 여러 개입니다. 0개 또는 1개 이상의 데이터를 emit 할 수 있기 때문에 `Mono`의 데이터 emit 범위를 포함한다고 볼 수 있습니다. 

```java
public static void main(String[] args) {
    Flux.just(6, 9, 13)
            .map(num -> num % 2)
            .subscribe(System.out::println);
}
```

위 코드는 `Flux`의 기본 예제 코드입니다. 

`just()` Operator에서 emit 하는 3개의 숫자들을 전달받은 후에 `map()` Operator에서 2로 나눈 나머지를 Subscriber에게 전달하여 출력합니다. 

실행 결과는 다음과 같습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fese6QG%2FbtsETnVu4MW%2FgEkJ9OIqTNbLlAiaTQuDx1%2Fimg.png)

다음 코드를 살펴 봅시다.

```java
public static void main(String[] args) {
    Flux.fromArray(new Integer[]{3, 6, 7, 9})
            .filter(num -> num > 6)
            .map(num -> num * 2)
            .subscribe(System.out::println);
}
```

데이터 소스로 제공되는 배열 데이터를 처리하기 위해 `fromArray()` Operator를 사용합니다. 전달받은 배열 원소를 하나씩 차례대로 emit 하면 `filter()` Operator에서 이 원소를 전달받아 6보다 큰 숫자만 필터링 한 뒤, `map()` Operator를 거쳐 Subscriber에게 전달하여 출력됩니다. 

실행 결과는 다음과 같습니다. 

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fc6BSBN%2FbtsEP4iAsOa%2Fq1nW65uH3F1B2jO56nUln1%2Fimg.png)

마지막으로 두 개의 `Mono`를 연결해서 `Flux`로 변환하는 코드를 살펴 보겠습니다. 

```java
public static void main(String[] args) {
    Flux<String> flux =
            Mono.justOrEmpty("Steve")
                    .concatWith(Mono.justOrEmpty("Jobs"));
    flux.subscribe(System.out::println);
}
```

`justOrEmpty()` Operator는 파라미터 값으로 null을 허용하지 않는 `just()` Operator와 달리, **null을 허용**합니다. `justOrEmpty()`의 파라미터로 null이 전달되면 내부적으로 `empty()` Operator를 호출합니다. 

`concatWith()` Operator는 아래 마블 다이어그램을 통해 설명될 수 있습니다. 

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FH7dUw%2FbtsETZfLlCN%2FHAKAaXZzqP2kvMFpnqLi8K%2Fimg.png)

`concatWith()` 위쪽에 있는 Publisher의 데이터 소스와 concatWith 내부에 있는 Publisher의 데이터 소스를 연결하고 있다는 것을 쉽게 확인할 수 있습니다. 

이렇게 연결된 데이터 소스는 **새로운 `Flux`의 데이터 소스**가 되어 차례대로 emit 됩니다. 

실행 결과는 두 개의 `Mono`에서 emit 하는 데이터를 하나의 데이터 소스로 연결하여 새로운 `Flux`로 리턴되어 다음과 같습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FBFU6T%2FbtsESK4JnPB%2FZKvFKoCpGnT2DU6JvQXG0K%2Fimg.png)

```java
public static void main(String[] args) {
    Flux.concat(
                    Flux.just("Mercury", "Venus", "Earth"),
                    Flux.just("Mars", "Jupiter", "Saturn"),
                    Flux.just("Uranus", "Neptune", "Pluto"))
            .collectList()
            .subscribe(System.out::println);
}
```

위 코드에서는 `concatWith()` 대신에 `concat()` Operator를 사용했습니다. `concatWith()`의 경우 **두 개**의 데이터 소스만 연결할 수 있지만, `concat()`은 **여러 개의 데이터 소스를 원하는 만큼 연결**할 수 있습니다. 

이때, 리턴하는 Publisher는 `Flux` 입니다.

`collectList()` Operator는 Upstream Publisher에서 emit 하는 데이터를 모아서 **List의 원소로 포함시킨 새로운 데이터 소스**로 만들어 주는 Operator입니다.

이때, 리턴하는 Publisher는 `Mono`입니다. List에 포함된 원소는 여러 개이지만, List 자체는 하나이기 때문입니다. 

참고로 마블 다이어그램은 아래와 같습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FzG7wQ%2FbtsETpse82U%2FVwD1p1yK8C2fJkKMNqCCX0%2Fimg.png)

위 코드의 실행 결과는 다음과 같습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Femcdq7%2FbtsERzPXc9Q%2FiuLMjYRt1AH8hPRgJJmsf1%2Fimg.png)

---

# 참고

아래의 Reactor Documentation을 통해 다양한 Operator와 해당 Operator의 마블 다이어그램을 확인해 볼 수 있습니다. 

### Reactor 3 Reference

[Reactor 3 Reference Guide](https://projectreactor.io/docs/core/release/reference/index.html#about-doc)

### Operator

[Flux(reactor-core 3.6.3)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#concat-java.lang.Iterable-)

#### 참고

"스프링으로 시작하는 리액티브 프로그래밍"
