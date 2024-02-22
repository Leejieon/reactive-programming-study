# 8.Backpressure



### 1) Backpressure란?

Publisher와 Subscriber간 처리속도 불균형으로 인해 처리되지않는 데이터가 지속적으로 쌓여 오버플로우가 발생하는 경우를 해결하기 위함이다.


```java
/*데이터 개수 제어 */
@Slf4j
public class Example8_1 {
    public static void main(String[] args) {
        Flux.range(1, 5)
            .doOnRequest(data -> log.info("# doOnRequest: {}", data))
            .subscribe(new BaseSubscriber<Integer>() {
                @Override
                protected void hookOnSubscribe(Subscription subscription) {
                    request(1);
                }

                @SneakyThrows
                @Override
                protected void hookOnNext(Integer value) {
                    Thread.sleep(2000L);
                    log.info("# hookOnNext: {}", value);
                    request(1);
                }
            });
    }
}

```

위 코드는 Subscriber가 명시적으로 request() 명령어를 통해 적절한 데이터 개수를 요청하는 방식이다.

이때 doOnRequest는 subscriber가 요청한 데이터의 개수를 출력한다.



### 2) BackPressure 처리방식



![image](https://github.com/bjpublic/Spring-Reactive/assets/62167266/32f2d76b-a90c-4737-825f-718c1abf1538)

아래 예제들에서 interval을 통해 짧은주기로 생성된 데이터들을 subscriber가 어떠한 방식으로 데이터 입력을 처리하는지 확인 할 수 있다.



- Error 전략 : Downstream의 데이터 처리속도가 느려  Upstream의 emit 속도를 따라가지 못할 경우 IllegalStateException을 발생시킨다.

```java
/**
 * Unbounded request 일 경우, Downstream 에 Backpressure Error 전략을 적용하는 예제
 *  - Downstream 으로 전달 할 데이터가 버퍼에 가득 찰 경우, Exception을 발생 시키는 전략
 */
@Slf4j
public class Example8_2 {
    public static void main(String[] args) throws InterruptedException {
        Flux
            .interval(Duration.ofMillis(1L))
            .onBackpressureError()
            .doOnNext(data -> log.info("# doOnNext: {}", data))
            .publishOn(Schedulers.parallel())
            .subscribe(data -> {
                        try {
                            Thread.sleep(5L);
                        } catch (InterruptedException e) {}
                        log.info("# onNext: {}", data);
                    },
                    error -> log.error("# onError", error));

        Thread.sleep(2000L);
    }
}
```

결과를 보면 doOnNext로 publish된 데이터의 속도를 onNext가 따라가지 못하고 255가 될경우 오버플로우가 발생함을 확인가능하다. (왜 255?)





- Drop전략 : Publisher가 Downstream으로 전달할 데이터가 버퍼에 가득 찰 경우 버퍼밖에서 대기중인 데이터 중 먼저 emit된 데이터부터Drop시키는 전략

  

```java
/**
 * Unbounded request 일 경우, Downstream 에 Backpressure Error 전략을 적용하는 예제
 *  - Downstream 으로 전달 할 데이터가 버퍼에 가득 찰 경우, Exception을 발생 시키는 전략
 */
@Slf4j
public class Example8_2 {
    public static void main(String[] args) throws InterruptedException {
        Flux
            .interval(Duration.ofMillis(1L))
            .onBackpressureError()
            .doOnNext(data -> log.info("# doOnNext: {}", data))
            .publishOn(Schedulers.parallel())
            .subscribe(data -> {
                        try {
                            Thread.sleep(5L);
                        } catch (InterruptedException e) {}
                        log.info("# onNext: {}", data);
                    },
                    error -> log.error("# onError", error));

        Thread.sleep(2000L);
    }
}
```



- Latest 전략 : 새로운 데이터가 들어오는 시점에 가장 최근의 데이터만 남겨두고 나머지 데이터를 폐기한다.

Drpo전략의 경우 버퍼가 가득 찰경우 대기중인 데이터를 하나씩 차례대로 drop한다. 타이밍적인 차이가 있다?

```java
/**
 * Unbounded request 일 경우, Downstream 에 Backpressure Latest 전략을 적용하는 예제
 *  - Downstream 으로 전달 할 데이터가 버퍼에 가득 찰 경우,
 *    버퍼 밖에서 대기하는 가장 나중에(최근에) emit 된 데이터부터 버퍼에 채우는 전략
 */
@Slf4j
public class Example8_4 {
    public static void main(String[] args) throws InterruptedException {
        Flux
            .interval(Duration.ofMillis(1L))
            .onBackpressureLatest()
            .publishOn(Schedulers.parallel())
            .subscribe(data -> {
                        try {
                            Thread.sleep(5L);
                        } catch (InterruptedException e) {}
                        log.info("# onNext: {}", data);
                    },
                    error -> log.error("# onError", error));

        Thread.sleep(2000L);
    }
}
```



- Buffer Drop전략

  앞선 Drop 및 Latest의 경우 버퍼가 가득찼을때 버퍼 밖에서 대기중인 데이터들을 폐기하는 것이었다면,  Buffer Drop의 경우 Buffer 내부의 데이터들을 폐기한다. 그 폐기 기준으로 LATEST와 OLDEST가 있다.

  

```java

/**
 * Unbounded request 일 경우, Downstream 에 Backpressure Buffer DROP_LATEST 전략을 적용하는 예제
 *  - Downstream 으로 전달 할 데이터가 버퍼에 가득 찰 경우,
 *    버퍼 안에 있는 데이터 중에서 가장 최근에(나중에) 버퍼로 들어온 데이터부터 Drop 시키는 전략
 */
@Slf4j
public class Example8_5 {
    public static void main(String[] args) throws InterruptedException {
        Flux
            .interval(Duration.ofMillis(300L))
            .doOnNext(data -> log.info("# emitted by original Flux: {}", data))
            .onBackpressureBuffer(2,
                    dropped -> log.info("** Overflow & Dropped: {} **", dropped),
                    BufferOverflowStrategy.DROP_LATEST)
            .doOnNext(data -> log.info("[ # emitted by Buffer: {} ]", data))
            .publishOn(Schedulers.parallel(), false, 1)
            .subscribe(data -> {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {}
                        log.info("# onNext: {}", data);
                    },
                    error -> log.error("# onError", error));

        Thread.sleep(2500L);
    }
}
```

위 코드는 버퍼의 크기를 2로 설정해 original flux가 buffer에 담기고,  overflow시 drop되는 데이터의 흐름을 파악할 수 있다.