# 7. Cold Sequence와 Hot Sequence



### 1) Cold와 Hot의 의미

Cold : 무언가를 새로 시작한다. 초기화 과정이 필요하다.

Hot : 무언가를 새로시작하지않다. 



### 2) Cold Sequence

Sequence란 Publisher가 emit하는 데이터의 연속적인 흐름을 정의해놓은 것으로 코드로 표현하면 operator 체인 형태이다.

![image](https://github.com/bjpublic/Spring-Reactive/assets/62167266/6bdd7616-3e50-4739-80e7-5b6f6b344883)



위의 경우 Subscriber A와 Subscriber B가 구독시점이 다를때, Cold Sequence에서는 구독시점이 달라도 구독할때마다 Publisher가 데이터를 emit하는 과정을 처음부터 다시 시작한다.

```java
/**
 * Cold Sequence 예제
 */
@Slf4j
public class Example7_1 {
    public static void main(String[] args) throws InterruptedException {

        Flux<String> coldFlux =
                Flux
                    .fromIterable(Arrays.asList("KOREA", "JAPAN", "CHINESE"))
                    .map(String::toLowerCase);

        coldFlux.subscribe(country -> log.info("# Subscriber1: {}", country));
        System.out.println("----------------------------------------------------------------------");
        Thread.sleep(2000L);
        coldFlux.subscribe(country -> log.info("# Subscriber2: {}", country));
    }
}
```

위 코드에서 subscriber2가 늦게 구독을 하여도 동일한 데이터를 얻을수 있음을 확인 할 수  있다,



#### 3) Hot Sequence

![image](https://github.com/bjpublic/Spring-Reactive/assets/62167266/5d80443d-700e-425c-818d-a8963f6c681d)

반대로 Hot Sequence의 경우 구독이 발생한 시점 이전에 Publisher로부터 emit된 데이터는 Subscriber가 전달받지 못하고 구독이 발생한 시점 이후에 emit된 데이터만 전달받을 수 있다.

```java
/**
 * Hot Sequence 예제
 */
@Slf4j
public class Example7_2 {
    public static void main(String[] args) throws InterruptedException {
        String[] singers = {"Singer A", "Singer B", "Singer C", "Singer D", "Singer E"};

        log.info("# Begin concert:");
        Flux<String> concertFlux =
                Flux
                    .fromArray(singers)
                    .delayElements(Duration.ofSeconds(1))
                    .share();

        concertFlux.subscribe(
                singer -> log.info("# Subscriber1 is watching {}'s song", singer)
        );

        Thread.sleep(2500);

        concertFlux.subscribe(
                singer -> log.info("# Subscriber2 is watching {}'s song", singer)
        );

        Thread.sleep(3000);
    }
}

```

반대로 `.share()`operator를 통한 Hot sequence의 동작은 원본 Flux를 다른 subscriber가 먼저 구독해 버린다면 이후에 Subscriber는 구독하는 시점에서 원본 Flux에서 emit된 데이터를 받을 수 없음을 의미한다.

해당코드를 실행하면 실제로 Sleep동안 일어난 A,B의 emit에 대해 subscriber2는 전달받지 못한다.





코드 7-4에서 cache()가 없다면 발생하는 상황은?