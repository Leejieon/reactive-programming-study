# Cold Sequence와 Hot Sequence

# Cold와 Hot의 의미

간단히 얘기하자면, 컴퓨터 시스템에서 Cold와 Hot의 의미는 다음과 같습니다.

> Cold는 무언가를 새로 시작하고, Hot은 무언가를 새로 시작하지 않는다.

이렇게만 보면 어떤 의미인지 잘 와닿지 않을 수 있습니다. 따라서, 예시를 몇 가지 들어보겠습니다.

### Hot Swap

컴퓨터 시스템에서 **Hot Swap**이란, 전원을 끄거나 시스템을 중지시키는 행위 _없이_ 장치를 교체해서 사용이 가능한 기능을 말합니다. 즉, 전원이나 시스템을 껐다 키는 행위를 새로 시작한다는 의미로 볼 수 있고, 이 행위를 하지 않는다는 것입니다. 위에서 말한 "무언가를 새로 시작하지 않는다."와 대응되는 의미라고 볼 수 있습니다.

반대로,  **Cold Swap**의 경우는 컴퓨터에 연결하거나 제거하기 위해 완전히 컴퓨터를 _종료_ 해야 하는 것들입니다. 

### Hot Deploy

Hot Deploy는 서버의 재시작 없이 응용 프로그램의 동적 변경을 바로 적용 시키는 기능을 말합니다. 이 또한, "새로 시작하지 않는다."는 의미를 내포하고 있습니다. 

---

# Cold Sequence

우선, **Sequence**란

> Publisher가 emit 하는 데이터의 연속적인 흐름을 정의해 놓은 것

입니다. 이를 코드로 표현하면 **Operator 체인** 형태로 정의되는 것입니다.

위에서 얘기한 Cold와 함께 대략적인 의미를 생각해본다면, Cold Sequence란 아마 Sequence가 새로 시작한다 정도로 생각해 볼 수 있습니다. 

**Cold Sequence**는 

> Subscriber가 구독할 때마다 데이터 흐름이 처음부터 다시 시작되는 Sequence 

입니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2F0MovG%2FbtsE9FH4wAl%2FCtKiUgdlcI3eU6K0sXDf2k%2Fimg.png)

위 그림은 Cold Sequence의 의미를 마블 다이어그램으로 나타낸 것입니다. 

여기에는 총 두 개의 **데이터 처리 흐름**(Sequence)을 나타내는 타임라인이 있습니다. 우선, Subscriber A가 구독을 하면 Publisher는 네 개의 데이터(1, 3, 5, 7)를 `emit` 합니다. 그리고 아래쪽의 Subscriber B가 구독을 해도 역시 네 개의 데이터를 `emit` 하는 것을 확인 할 수 있습니다. 위 그림에서 볼 수 있드시 Subscriber A와 B의 **구독 시점이 달라도** A, B 모두 **동일한 데이터를 전달**받는 것을 확인할 수 있습니다. 

이처럼, Subscriber의 **구독 시점이 달라도** 구독할 때마다 Publisher가 데이터를 `emit` 하는 과정을 **처음부터 다시 시작**하는 데이터의 흐름을 **Cold Sequence**라고 합니다. 결과적으로 Sequence 타임라인이 구독을 할 때마다 하나씩 더 생기게 됩니다. 

간단한 예시로, 월간 잡지를 구독할 때, 구독자가 5월부터 구독을 시작해도 이전 1월달 잡지부터 모두 보내 주는 경우 Cold Sequence의 느낌이라고 볼 수 있습니다. 

Cold Sequence의 예시 코드를 살펴 봅시다. 

```java
@Slf4j
public class DemoApplication {
	static final Logger log = LoggerFactory.getLogger(DemoApplication.class);
	public static void main(String[] args) throws InterruptedException {
		Flux<String> coldFlux = Flux
				.fromIterable(Arrays.asList("KOREA", "JAPAN", "CHINESE"))
				.map(String::toLowerCase);

		coldFlux.subscribe(country -> log.info("# Subscriber A: {}", country));
		System.out.println("-------------------------------");

		Thread.sleep(2000L);
		coldFlux.subscribe(country -> log.info("# Subscriber B: {}", country));
	}
}
```

우선, 코드에 대해 설명해보면 `fromIterable()` Operator를 사용해 List로 전달받은 데이터 소스를 `emit` 합니다. 그리고 Subscriber A와 B가 2초의 텀을 두고 구독을 진행합니다. 위 코드의 실행 결과는 다음과 같습니다. 

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fk99ti%2FbtsE0J6GEeR%2FNZbDTILxuhIw3O1r9Uwjp1%2Fimg.png)

코드 실행 결과를 보면 **구독이 발생할 때마다** `emit`된 **데이터를 처음부터 다시 전달받고 있음**을 확인할 수 있습니다. 

# Hot Sequence

**Hot Sequence**는 Cold Sequence와 반대로

> 구독이 발생한 시점 이전에 Publisher로부터 emit 된 데이터는 Subscriber가 전달받지 못하고, 구독이 발생한 시점 이후에 emit 된 데이터만 전달받을 수 있다.

고 얘기할 수 있습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FmO3YF%2FbtsE6I6Z3aB%2F6WvGZbdsBXJuyOXpVbprHK%2Fimg.png)

Cold Sequence와 달리, 세 번의 구독이 발생했지만 **타임라인은 하나**밖에 생성되지 않았습니다. 즉, Hot Sequence의 경우 구독이 아무리 많이 발생해도 Publisher가 **데이터를 처음부터 `emit` 하지 않는다는 것**을 의미합니다. 

위 그림 내용을 정리해 각 구독자가 받는 데이터는 다음과 같습니다

-   Subscriber **A** := 1, 3, 5, 7
-   Subscriber **B** := 5, 7
-   Subscriber **C** := 7

이처럼, Publisher가 데이터를 `emit` 하는 과정이 한 번만 일어나고 Subscriber가 각각의 구독 시점 이후에 `emit` 된 데이터만 전달받는 데이터의 흐름을 **Hot Sequence**라고 합니다. 

똑같이, 구독자가 5월부터 잡지를 구독했을 때, 5월달 잡지부터 구독자에게 보내 주는 것과 같다고 볼 수 있습니다.

Hot Sequence의 예시 코드를 살펴 봅시다.

```java
@Slf4j
public class DemoApplication {
	static final Logger log = LoggerFactory.getLogger(DemoApplication.class);
	public static void main(String[] args) throws InterruptedException {
		String[] singers = {"Singer A", "Singer B", "Singer C", "Singer D", "Singer E"};

		log.info("# Begin concert:");

		Flux<String> concertFlux = Flux
				.fromArray(singers)
				.delayElements(Duration.ofSeconds(1))
				.share();

		concertFlux.subscribe(singer -> log.info("# Subscriber A is watching {}'s song", singer));
		Thread.sleep(2500);
		concertFlux.subscribe(singer -> log.info("# Subscriber B is watching {}'s song", singer));
		Thread.sleep(3000);
	}
}
```

위 코드는 뮤직 콘서트에 입장하는 관객의 콘서트 관람 상황을 시뮬레이션했습니다. 콘서트는 총 5명의 가수가 1분에 한 명씩 출연해 노래를 부르고, A 관객은 시작하기 이전에 이미 입장해 있는 상태이고, B 관객은 콘서트가 이미 시작되고 일정 시간이 지났을 때 입장했습니다.

### delayElements

코드에서 사용된 `delayElements()` Operator는 데이터 소스로 입력된 각 **데이터의 `emit`을 일정시간 동안 지연**시키는 Operator 입니다. 위 코드에서는 데이터의 `emit`이 1초씩 지연될 수 있도록 파라미터로 `Duration` 객체를 전달했습니다.

참고로, `delayElements()`의 마블 다이어그램은 아래와 같습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fckppxa%2FbtsE8CrrYIH%2F7KBMgDpKsv8kjbMrtIAtlk%2Fimg.png)

### share

`share()` Operator는 Cold Sequence를 **Hot Sequence로 동작**하게 해 주는 Operator 입니다.

Reactor의 Documentation에 다음과 같이 설명되어 있습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FwwfpQ%2FbtsE92iRrnq%2Fkkr2Bb4vGwrNffH10FKGmk%2Fimg.png)

첫 줄의 내용을 보면 _"원본 Flux를 공유하는 새로운 Flux를 리턴한다."_ 고 해석할 수 있습니다. 원본 Flux는 Operator의 가공이 이루어지지 않은 원본 데이터 소스를 처음으로 `emit` 하는 Flux를 의미합니다.

위 코드를 보면 `fromArray()` 에서 처음으로 Flux를 리턴하고, 이어서 체이닝 형태로 각각 새로운 Flux들(다른 참조 값을 가지는)을 리턴합니다. 이렇게 리턴되는 Flux 중에서 `fromArray()` 에서 **처음으로 리턴하는 Flux**가 바로 **원본 Flux**입니다.

이 원본 Flux를 공유한다는 의미는 **여러 Subscriber가 하나의 원본 Flux를 공유**한다는 의미입니다.

하나의 원본 Flux를 공유해서 다 같이 사용하기 때문에 어떤 Subscriber가 이 원본 Flux를 "먼저" 구독해 버리면 데이터 `emit`을 시작하게 돼, 다른 Subscriber가 구독하는 시점에는 원본 Flux에서 이미 `emit` 된 데이터를 전달 받을 수 없습니다.

결국, 위 코드의 실행 결과는 다음과 같습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fw4MgY%2FbtsE5Eqjwsv%2FNcz0iN7jClb9ApwV8sPvR0%2Fimg.png)

결과를 보면, 늦게 구독을 시작한 Subscriber B의 경우, 앞에서 `emit` 된 Singer A와 B의 노래를 들을 수 없습니다. 2.5초 뒤에 구독이 발생했기 때문에 2.5초의 지연 시간동안 원본 Flux가 이미 Singer A, B 데이터를 `emit` 했기 때문입니다. 

참고로, main 스레드와 parallel-1, 2, 3, 4, 5 스레드가 실행된 것을 볼 수 있는데, 이는 `delayElements()` Operator의 디폴트 스레드 스케줄러가 **parallel**이기 때문입니다. 

### 참고

"스프링으로 시작하는 리액티브 프로그래밍"
