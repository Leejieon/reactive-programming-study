# Scheduler란?

Reactor에서 Scheduler는 

> 비동기 프로그래밍을 위해 사용되는 **스레드를 관리해 주는 역할**

을 합니다. 다시 말하면, Scheduler를 사용하여 **어떤 스레드에서 무엇을 처리할지 제어**합니다. 

# Scheduler를 위한 전용 Operator

Reactor에서 Scheduler는 **Scheduler 전용 Operator**를 통해 사용할 수 있습니다. 앞서 사용했던 `subscribeOn()` 와 `publishOn()` Operator가 바로 Scheduler 전용 Operator입니다. 

## subscribeOn( )

`subscribeOn()` Operator는 

> 구독이 발생한 직후 실행될 스레드를 지정하는 Operator

입니다.

구독이 발생하면 원본 Publisher가 데이터를 최초로 emit하게 되는데, `subscribeOn()` Operator는 구독 시점 직후에 실행되기 때문에 원본 Publisher의 동작을 수행하기 위한 스레드라고 볼 수 있습니다. 

```java
@Slf4j
public class DemoApplication {
    public static void main(String[] args) throws InterruptedException {
        Flux.fromArray(new Integer[]{1, 3, 5, 7})
            .subscribeOn(Schedulers.boundedElastic())
            .doOnNext(data -> log.info("# doOnNext: {}", data))
            .doOnSubscribe(subscription -> log.info("# doOnSubscribe"))
            .subscribe(data -> log.info("# onNext: {}", data));

    Thread.sleep(500L);
    }
}
```

위 코드에 대해 살펴봅시다.

-   `subscribeOn()` Operator를 추가했기 때문에 **구독이 발생한 직후에 원본 Publisher의 동작을 처리하기 위한 스레드**를 할당합니다. 
-   `doOnNext()` Operator를 사용해 **원본 Flux에서 emit되는 데이터**를 로그로 출력합니다. 
-   `doOnSubscribe()` Operator를 사용해 구독이 발생한 시점에 실행되는 스레드가 무엇인지 확인합니다. 

실제로 `subscribeOn()` Operator를 살펴보면 다음과 같습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcnW2Yu%2FbtsFiPSXwX6%2FNiDkYAnB5gPgekVGiLANo1%2Fimg.png)

Operator의 파라미터로 Scheduler가 들어가는 것을 확인할 수 있습니다. 

코드의 실행 결과는 다음과 같습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcBidM2%2FbtsFm6TuzB8%2FR1QYzEULaQo8KIs9dlOugk%2Fimg.png)

`doOnSubscribe()` 에서의 동작은 main 스레드에서 실행되는 것을 볼 수 있는데, 이 코드의 최초 실행 스레드가 main 스레드이기 때문입니다. 

`subscribeOn()`을 추가하지 않았다면 원본 Flux 처리 동작은 여전히 main 스레드에서 실행되겠지만, Scheduler를 지정했기 때문에 **구독이 발생한 직후부터는 스레드가 바뀌게** 됩니다. 

따라서, `doOnNext()`에서의 동작은 boundedElastic-1 스레드에서 실행되고, 이후도 특별히 다른 Scheduler를 지정하지 않았기 때문에 계속 해당 스레드에서 진행됩니다. 

## publishOn( )

우선, Publisher는 Reactor Sequence에서 발생하는 Signal을 Downstream으로 전송하는 주체입니다. 이러한 관점에서 `publishOn()` Operator는

> Downstream으로 Signal을 전송할 때 실행되는 스레드를 제어하는 역할을 하는 Operator

라고 할 수 있습니다. 위와 마찬가지로 파라미터로 Scheduler를 지정할 수 있습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FV1mTd%2FbtsFlQjlrjj%2FKYJBkmoysWaKHjzG5cRKGK%2Fimg.png)

```java
@Slf4j
public class DemoApplication {
    public static void main(String[] args) throws InterruptedException {
        Flux.fromArray(new Integer[]{1, 3, 5, 7})
            .doOnNext(data -> log.info("# doOnNext: {}", data))
            .doOnSubscribe(subscription -> log.info("# doOnSubscribe"))
            .publishOn(Schedulers.parallel())
            .subscribe(data -> log.info("# onNext: {}", data));

    Thread.sleep(500L);
    }
}
```

위 코드에서 `publishOn()` Operator를 사용했기 때문에 **Downstream으로 데이터를 emit하는 스레드를 변경**합니다. 

코드의 실행 결과는 다음과 같습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbBqK6m%2FbtsFhe6NLaq%2Fru4P3zP0Tgljd7iKjkoFBK%2Fimg.png)

앞선 코드와 마찬가지로 최초 실행 스레드 역시 main 스레드이기 때문에 `doOnSubscribe()`는 main 스레드에서 실행되었습니다. 

`doOnNext()`의 경우, `subscribeOn()` Operator를 사용하지 않았기 때문에 여전히 main 스레드에서 실행됩니다. 

하지만, `onNext()`의 경우, `publishOn()` Operator를 추가했기 때문에 `publishOn()`을 기준으로 **Downstream의 실행 스레드가 변경**되어 parallel-1 스레드에서 실행되는 것을 확인할 수 있습니다. 

> publishOn() Operator는 해당 publishOn() 을 기준으로 Downstream의 실행 스레드를 변경합니다.

## parallel( )

앞서 말한 `subscribeOn()`과 `publishOn()` Operator의 경우, 동시성을 가지는 논리적인 스레드에 해당됩니다. 하지만, `parallel()`의 경우, 병렬성을 가지는 물리적인(하드웨어) 스레드에 해당됩니다.

`parallel()`의 경우,

> Round-Robin 방식으로 CPU 코어 개수만큼의 스레드를 병렬로 실행

합니다.

```java
@Slf4j
public class DemoApplication {
    public static void main(String[] args) throws InterruptedException {
        Flux.fromArray(new Integer[]{1, 3, 5, 7, 9, 11, 13, 15, 17, 19})
            .parallel()
            .runOn(Schedulers.parallel())
            .subscribe(data -> log.info("# onNext: {}", data));

    Thread.sleep(100L);
    }
}
```

위 코드는 원본 Flux가 총 10개의 숫자를 emit하는데 `parallel()` Operator를 추가함으로써 이 10개의 숫자를 **병렬로 처리**합니다.

하지만, `parallel()` Operator는 emit되는 데이터를 CPU의 논리적인 코어(물리적 스레드) 수에 맞게 **사전에 골고루 분배하는 역할만** 하며, 실제로 **병렬 작업을 수행할 스레드의 할당**은 **`runOn()`** Operator가 담당합니다.

실행 결과는 다음과 같습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FmXZ9U%2FbtsFoJwJqKG%2FCsoscS2QorpWDQtkOhECA1%2Fimg.png)

제 컴퓨터의 CPU 스레드가 10개가 넘어가기 때문에 각 숫자가 스레드 별로 병렬 처리되는 것을 확인할 수 있습니다. 

그런데 어떤 작업을 처리하기 위해 물리적인 스레드 전부를 사용할 필요가 없는 경우에는 사용하고자 하는 스레드의 개수를 지정해 줄 수 있습니다.

```java
@Slf4j
public class DemoApplication {
    public static void main(String[] args) throws InterruptedException {
        Flux.fromArray(new Integer[]{1, 3, 5, 7, 9, 11, 13, 15, 17, 19})
            .parallel(4)
            .runOn(Schedulers.parallel())
            .subscribe(data -> log.info("# onNext: {}", data));

    Thread.sleep(100L);
    }
}
```

위 코드의 실행 결과는 다음과 같습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbFcQfE%2FbtsFlPLu60X%2Fxx05VlkVwptrPeEeVBk0O0%2Fimg.png) 

결과를 보면 총 4개의 스레드가 병렬로 실행되는 것을 확인할 수 있습니다.

# publishOn( )과 subscribeOn( )의 동작 이해

**원본 Publisher의 동작과 나머지 동작을 역할에 맞게 분리하기 위해** `subscribeOn()`과 `publishOn()` Operator를 함께 사용하는 경우가 흔히 있습니다.

이 두 개의 Operator를 함께 사용할 때의 실행 스레드의 동작에 대해 살펴봅시다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FIK1b2%2FbtsFn9JdZ4i%2FysJRts171Rkk3wUBVsSVUK%2Fimg.png)

우선 위의 그림은 둘 다 사용하지 않을 경우 Operator 체인에서 실행되는 스레드의 동작 과정입니다.

보다시피 별도의 Scheduler를 추가하지 않았기 때문에 Operator 체인상의 최초 스레드는 main 스레드가 되며, 모든 과정이 main 스레드에서 실행됩니다.

```java
@Slf4j
public class DemoApplication {
    public static void main(String[] args) throws InterruptedException {
        Flux.fromArray(new Integer[]{1, 3, 5, 7})
            .doOnNext(data -> log.info("# doOnNext fromArray: {}", data))
            .filter(data -> data > 3)
            .doOnNext(data -> log.info("# doOnNext filter: {}", data))
            .map(data -> data * 10)
            .doOnNext(data -> log.info("# doOnNext map: {}", data))
            .subscribe(data -> log.info("# onNext: {}", data));
    }
}
```

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fbw4yWy%2FbtsFkHUsUqb%2FFp5YAQgZAft8CuQgyGNvlK%2Fimg.png)

Operator 체인의 각 단계별로 실행되는 스레드를 확인하기 위해 세 개의 `doOnNext()` Operator를 사용했습니다. 실행 결과를 보면 모두 main 스레드에서 실행된 것을 확인할 수 있습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FBpRgO%2FbtsFimpJ6Of%2FPIunlIFq3GRS1KGGtiWZU1%2Fimg.png)

위 그림은 `publishOn()`을 하나만 사용할 경우 Operator 체인에서 실행되는 스레드의 동작 과정입니다.

Operator 체인에 `publishOn()`을 추가하면 `publishOn()`에서 지정한 해당 Scheduler 유형의 스레드가 실행됩니다. 그림을 보면 `publishOn()` 이후의 실행 스레드는 모두 A 스레드입니다. 

```java
@Slf4j
public class DemoApplication {
    public static void main(String[] args) throws InterruptedException {
        Flux.fromArray(new Integer[]{1, 3, 5, 7})
            .doOnNext(data -> log.info("# doOnNext fromArray: {}", data))
            .publishOn(Schedulers.parallel())
            .filter(data -> data > 3)
            .doOnNext(data -> log.info("# doOnNext filter: {}", data))
            .map(data -> data * 10)
            .doOnNext(data -> log.info("# doOnNext map: {}", data))
            .subscribe(data -> log.info("# onNext: {}", data));
    }
}
```

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbJKdeM%2FbtsFoaBlJnW%2F6qH6D1lh3J5UKNOpDrvs21%2Fimg.png)

실행 결과를 보면 `publishOn()` 이후에 추가된 Operator 체인은 모두 parallel-1 스레드에서 실행된 것을 확인할 수 있습니다. 

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FxH5uF%2FbtsFm5G2jSt%2FFarZBu70ghMz3fbIfzXJck%2Fimg.png)

위 그림은 `publishOn()`을 두 번 사용할 경우 Operator 체인에서 실행되는 스레드의 동작 과정입니다. `publishOn()`은 Operator 체인상에서 한 개 이상을 사용할 수 있습니다.

첫 번째 `publishOn()`을 추가했을 때 `filter()`는 A 스레드에서 실행되고, 두 번째 `publishOn()`을 추가하면 이후의 Operator 체인은 B 스레드에서 실행되는 것을 볼 수 있습니다. 

```java
@Slf4j
public class DemoApplication {
    public static void main(String[] args) throws InterruptedException {
        Flux.fromArray(new Integer[]{1, 3, 5, 7})
            .doOnNext(data -> log.info("# doOnNext fromArray: {}", data))
            .publishOn(Schedulers.parallel())
            .filter(data -> data > 3)
            .doOnNext(data -> log.info("# doOnNext filter: {}", data))
            .publishOn(Schedulers.parallel())
            .map(data -> data * 10)
            .doOnNext(data -> log.info("# doOnNext map: {}", data))
            .subscribe(data -> log.info("# onNext: {}", data));
    }
}
```

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbbbhjX%2FbtsFlWYasUx%2FyRTyHdgwKZTNZjCq4UQkzK%2Fimg.png)

실행 결과를 보면, 첫 번째 `publishOn()`을 추가함으로써 `filter()`는 parallel-2 스레드에서 실행되었습니다. 그리고 두 번째 `publishOn()`을 추가함으로써 `map()`부터는 parallel-1 스레드에서 실행된 것을 확인할 수 있습니다. 

이처럼 Operator 체인상에서 한 개 이상의 `publishOn()` Operator를 사용해 실행 스레드를 목적에 맞게 적절하게 분리할 수 있습니다. 

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbBoT3S%2FbtsFmhgGpRS%2F85RlRXiIu8rtm9UyyJOfrk%2Fimg.png)

위 그림은 `subscribeOn()`과 `publishOn()`을 함께 사용할 경우 Operator 체인에서 실행되는 스레드의 동작 과정입니다. 

`subscribeOn()`에 의해 구독이 발생한 직후에 실행될 스레드를 지정해 `fromArray()`는 A 스레드에서 실행됩니다. 그리고 별도의 `publishOn()`이 추가되지 않아 `filter()`는 여전히 A 스레드에서 실행됩니다. 마지막으로 `publishOn()`이 추가된 이후의 Operator 체인은 B 스레드에서 실행됩니다. 

```java
@Slf4j
public class DemoApplication {
    public static void main(String[] args) throws InterruptedException {
        Flux.fromArray(new Integer[]{1, 3, 5, 7})
            .subscribeOn(Schedulers.boundedElastic())
            .doOnNext(data -> log.info("# doOnNext fromArray: {}", data))
            .filter(data -> data > 3)
            .doOnNext(data -> log.info("# doOnNext filter: {}", data))
            .publishOn(Schedulers.parallel())
            .map(data -> data * 10)
            .doOnNext(data -> log.info("# doOnNext map: {}", data))
            .subscribe(data -> log.info("# onNext: {}", data));
    }
}
```

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcuroM5%2FbtsFhRXSvXy%2F40Qe0m7gSBanK2cn6S3bKK%2Fimg.png)

실행 결과를 보면 `publishOn()` 이전까지의 Operator 체인은 `subscribeOn()`에서 지정한 boundedElastic-1 스레드에서 실행되고, `publishOn()` 이후의 Operator 체인은 parallel-1 스레드에서 실행됩니다. 

이처럼 `subscribeOn()`과 `publishOn()`를 함께 사용하면 원본 Publisher에서 데이터를 emit하는 스레드와 emit된 데이터를 가공 처리하는 스레드를 적절하게 분리할 수 있습니다. 

# Scheduler의 종류

## Scheduler.immediate( )

`Scheduler.immediate()`은

> 별도의 스레드를 추가적으로 생성하지 않고, 현재 스레드에서 작업을 처리하고자 할 때 사용

할 수 있습니다.

```java
@Slf4j
public class DemoApplication {
    public static void main(String[] args) throws InterruptedException {
        Flux.fromArray(new Integer[]{1, 3, 5, 7})
            .subscribeOn(Schedulers.parallel())
            .doOnNext(data -> log.info("# doOnNext fromArray: {}", data))
            .filter(data -> data > 3)
            .doOnNext(data -> log.info("# doOnNext filter: {}", data))
            .publishOn(Schedulers.immediate())
            .map(data -> data * 10)
            .doOnNext(data -> log.info("# doOnNext map: {}", data))
            .subscribe(data -> log.info("# onNext: {}", data));
    }
}
```

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcSumEg%2FbtsFiounjbh%2FyR28Ps7UIWucZV34lnyu6k%2Fimg.png)

실행 결과를 보면, 추가 스레드를 생성하지 않고 현재 스레드를 그래도 사용하고 있는 것을 확인할 수 있습니다. 처음 지정했던 parallel-1 스레드를 그래도 사용하고 있는 것이죠.

## Scheduler.single( )

`Scheduler.single()`은

> 스레드 하나만 생성해서 Scheduler가 제거되기 전까지 재사용하는 방식

입니다.

```java
@Slf4j
public class DemoApplication {
    public static void main(String[] args) throws InterruptedException {
        doTask("task1")
            .subscribe(data -> log.info("# onNext: {}", data));
        doTask("task2")
            .subscribe(data -> log.info("# onNext: {}", data));
            
        Thread.sleep(500L);
	}
    
    private static Flux<Integer> doTask(String taskName) {
        return Flux.fromArray(new Integer[]{1, 3, 5, 7})
                    .publishOn(Schedulers.single())
                    .filter(data -> data > 3)
                    .doOnNext(data -> log.info("# doOnNext filter: {}", taskName, data))
                    .map(data -> data * 10)
                    .doOnNext(data -> log.info("# doOnNext map: {}", taskName, data));
    }
}
```

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcTzjUz%2FbtsFjJyf9IH%2FxuSKZR3kQkIVmTGYwI9xD0%2Fimg.png)

실행 결과를 보면 `doTask()`가 두 번 호출되었지만 single-1이라는 하나의 스레드에서 처리되었습니다. 

이처럼 `Scheduler.single()`을 통해 **하나의 스레드를 재사용하면서 다수의 작업을 처리**할 수 있는데, 하나의 스레드로 다수의 작업을 처리해야 되므로 **지연 시간이 짧은 작업을 처리하는 것이 효과적**입니다. 

## Scheduler.newSingle( )

`Scheduler.newSingle()`은

> 호출할 때마다 매번 새로운 스레드 하나를 생성

합니다. 

```java
@Slf4j
public class DemoApplication {
    public static void main(String[] args) throws InterruptedException {
        doTask("task1")
            .subscribe(data -> log.info("# onNext: {}", data));
        doTask("task2")
            .subscribe(data -> log.info("# onNext: {}", data));
            
        Thread.sleep(500L);
	}
    
    private static Flux<Integer> doTask(String taskName) {
        return Flux.fromArray(new Integer[]{1, 3, 5, 7})
                    .publishOn(Schedulers.newSingle("new-single", true))
                    .filter(data -> data > 3)
                    .doOnNext(data -> log.info("# doOnNext filter: {}", taskName, data))
                    .map(data -> data * 10)
                    .doOnNext(data -> log.info("# doOnNext map: {}", taskName, data));
    }
}
```

`Scheduler.newSingle()` 메서드의 첫 번째 파라미터에는 **생성할 스레드의 이름을 지정**합니다. 두 번째 파라미터에는 이 스레드를 데몬(Daemon) 스레드로 동작하게 할지 여부를 설정합니다.

> 데몬 스레드는 보조 스레드로, 주 스레드가 종료되면 자동으로 종료되는 특성을 가진 스레드입니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fv59dx%2FbtsFkW49GK5%2FtMPKa5XIXM5hnUS6TIenWk%2Fimg.png)

실행 결과를 보면, `doTask()` 메서드를 호출할 때마다 새로운 스레드 하나를 생성해서 각각의 작업을 처리하는 것을 확인할 수 있습니다. 

## Scheduler.boundedElastic( )

`Scheduler.boundedElastic()`은

> ExecutorService 기반의 스레드 풀을 생성한 후, 그 안에서 정해진 수만큼의 스레드를 사용하여 작업을 처리하고 작업이 종료된 스레드는 반납하여 재사용하는 방식

입니다.

기본적으로 $CPU 코어 수 * 10$만큼의 스레드를 생성하고, 풀에 있는 모든 스레드가 작업을 처리하고 있다면 최대 100,000개의 작업이 큐에서 대기할 수 있습니다. 

이제껏 살펴본 대부분의 코드는 `fromArray()` 같은 Operator의 데이터 소스로 적은 수의 데이터를 수동으로 전달했습니다. 하지만, 실제는 데이터베이스를 통한 질의나 HTTP 요청 같은 Blocking I/O 작업을 통해 전달받은 데이터를 데이터 소스로 사용하는 경우가 많습니다. 이러한 Blocking I/O 작업을 효과적으로 처리하기 위한 방식입니다. 

실행 시간이 긴 Blocking I/O 작업이 포함되면, 다른 Non-Blocking 처리에 영향을 주지 않도록 전용 스레드를 할당해 Blocking I/O 작업을 처리하기 때문에 처리 시간을 효율적으로 사용할 수 있습니다. 

## Scheduler.parallel( ) 

`Scheduler.parallel()`은

> Non-Blocking I/O에 최적화되어 있는 Scheduler로서 CPU 코어 수만큼의 스레드를 생성합니다.

`Scheduler.boundedElastic()`는 Blocking I/O 작업에 최적화되어 있습니다.

## Scheduler.fromExecutorService( )

`Scheduler.fromExecutorService()`는

> 기존에 이미 사용하고 있는 ExecutorService가 있다면 이 ExecutorService로부터 Scheduler를 생성하는 방식

입니다.

## Scheduler.newXXXX( )

스레드 이름, 생성 가능한 디폴트 스레드의 개수, 스레드의 유효 시간, 데몬 스레드로의 동작 여부 등을 필요에 따라 직접 지정해서 커스텀 스레드 풀을 새로 생성할 수 있습니다. 

### 참고

"스프링으로 시작하는 리액티브 프로그래밍"
