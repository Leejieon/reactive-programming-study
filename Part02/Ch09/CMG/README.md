# 9. Sinks



### 1) Sink란?



**Sink**란 Publisher와 Subscriber의 기능을 모두 가지는 마치 Processor의 역할을 한다.  



Sinks사용과 Operator사용의 전통적인 방식의 차이

Operator 는 싱글스레드 기반에서 SIgnal을 전송하는데 반면 Sinks는 멀티쓰레드 방식으로 Signal을 전송해도 안정성을 보장한다.

```java
/**
 * create() Operator를 사용하는 예제
 *  - 일반적으로 Publisher가 단일 쓰레드에서 데이터 생성한다.
 */
@Slf4j
public class Example9_1 {
    public static void main(String[] args) throws InterruptedException {
        int tasks = 6;
        Flux
            .create((FluxSink<String> sink) -> {
                IntStream
                        .range(1, tasks)
                        .forEach(n -> sink.next(doTask(n)));
            })
            .subscribeOn(Schedulers.boundedElastic())
            .doOnNext(n -> log.info("# create(): {}", n))
            .publishOn(Schedulers.parallel())
            .map(result -> result + " success!")
            .doOnNext(n -> log.info("# map(): {}", n))
            .publishOn(Schedulers.parallel())
            .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

    private static String doTask(int taskNumber) {
        // now tasking.
        // complete to task.
        return "task " + taskNumber + " result";
    }
}
```





```java
/**
 * Sinks를 사용하는 예제
 *  - Publisher의 데이터 생성을 멀티 쓰레드에서 진행해도 Thread safe 하다.
 */
@Slf4j
public class Example9_2 {
    public static void main(String[] args) throws InterruptedException {
        int tasks = 6;

        Sinks.Many<String> unicastSink = Sinks.many().unicast().onBackpressureBuffer();
        Flux<String> fluxView = unicastSink.asFlux();
        IntStream
                .range(1, tasks)   
                .forEach(n -> {
                    try {
                        new Thread(() -> {
                            unicastSink.emitNext(doTask(n), Sinks.EmitFailureHandler.FAIL_FAST);
                            log.info("# emitted: {}", n);
                        }).start();
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage());
                    }
                });

        fluxView
                .publishOn(Schedulers.parallel())
                .map(result -> result + " success!")
                .doOnNext(n -> log.info("# map(): {}", n))
                .publishOn(Schedulers.parallel())
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(200L);
    }

    private static String doTask(int taskNumber) {
        // now tasking.
        // complete to task.
        return "task " + taskNumber + " result";
    }
}
```



반대로 위는 Sink를 사용하는 예제인데, `doTask()`메서드는 루프를 돌 때마다 새로운 쓰레드를 생성한다.

그리고 그결과를 Downstream에 emit하게 된다. 따라서 5개의 쓰레드와 가공처리 parrallel 2 쓰레드, Subscriber에서 전달받은 데이터의 처리를  parallel 1 쓰레드에서 실행하게 되어 총 7개의  쓰레드를 사용하게 됨으로써 멀티쓰레드 환경에서 쓰레드 안전성을 보장 받을수 있다.







### 2) Sink의 종류

#### Sinks.One



![image](https://github.com/Leejieon/reactive-programming-study/assets/62167266/9f11c3db-996f-49cc-ae89-e56988c1e2b1)



#### Sinks.Many

Return으로 데이터 emit을 위한 여러가지 기능이 정의된 ManySpec을 리턴한다.

![image](https://github.com/bjpublic/Spring-Reactive/assets/62167266/c4a63ede-4ceb-4151-868f-de9bbfa93c28)

이때 UnicastSpec 1대1통신이기 때문에 Subscriber 하나에게만 데이터를 emit하게 되고, 

MulticastSpec의 경우 하나이상의 Subscriber에게 데이터를 emit하게된다,.