# Debugging

동기적 또는 명령형 프로그래밍과 다르게 비동기적인 작업들은 선언형 방식이므로 디버깅이 쉽지 않습니다.

이를 해결하기 위한 몇 가지 방법이 있습니다.

1. **Debug Mode를 사용한 디버깅**
    
    ```java
    import lombok.extern.slf4j.Slf4j;
    import reactor.core.publisher.Flux;
    import reactor.core.publisher.Hooks;
    import reactor.core.scheduler.Schedulers;
    
    import java.util.HashMap;
    import java.util.Map;
    
    @Slf4j
    public class Example12_1 {
        public static Map<String, String> fruits = new HashMap<>();
    
        static {
            fruits.put("banana", "바나나");
            fruits.put("apple", "사과");
            fruits.put("pear", "배");
            fruits.put("grape", "포도");
        }
    
        public static void main(String[] args) throws InterruptedException {
            Hooks.onOperatorDebug();
    
            Flux
                    .fromArray(new String[]{"BANANAS", "APPLES", "PEARS", "MELONS"})
                    .subscribeOn(Schedulers.boundedElastic())
                    .publishOn(Schedulers.parallel())
                    .map(String::toLowerCase)
                    .map(fruit -> fruit.substring(0, fruit.length() - 1))
                    .map(fruits::get)
                    .map(translated -> "맛있는 " + translated)
                    .subscribe(
                            log::info,
                            error -> log.error("# onError:", error));
    
            Thread.sleep(100L);
        }
    }
    
    //
    23:58:42.948 [parallel-1] INFO com.example.reactive.debug.Example12_1 -- 맛있는 바나나
    23:58:42.950 [parallel-1] INFO com.example.reactive.debug.Example12_1 -- 맛있는 사과
    23:58:42.950 [parallel-1] INFO com.example.reactive.debug.Example12_1 -- 맛있는 배
    23:58:42.955 [parallel-1] ERROR com.example.reactive.debug.Example12_1 -- # onError:
    java.lang.NullPointerException: The mapper [com.example.reactive.debug.Example12_1$$Lambda$32/0x0000000800c9cae0] returned a null value.
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:115)
    	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
    Assembly trace from producer [reactor.core.publisher.FluxMapFuseable] :
    	reactor.core.publisher.Flux.map(Flux.java:6517)
    	com.example.reactive.debug.Example12_1.main(Example12_1.java:31)
    Error has been observed at the following site(s):
    	*__Flux.map ⇢ at com.example.reactive.debug.Example12_1.main(Example12_1.java:31)
    	|_ Flux.map ⇢ at com.example.reactive.debug.Example12_1.main(Example12_1.java:32)
    Original Stack Trace:
    		at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:115)
    		at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:129)
    		at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:129)
    		at reactor.core.publisher.FluxPublishOn$PublishOnSubscriber.runAsync(FluxPublishOn.java:446)
    		at reactor.core.publisher.FluxPublishOn$PublishOnSubscriber.run(FluxPublishOn.java:533)
    		at reactor.core.scheduler.WorkerTask.call(WorkerTask.java:84)
    		at reactor.core.scheduler.WorkerTask.call(WorkerTask.java:37)
    		at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
    		at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:304)
    		at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
    		at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
    		at java.base/java.lang.Thread.run(Thread.java:833)
    
    ```
    
    위의 코드는, 디버그 모드를 활성화를 이용한 디버깅 예제 코드입니다.
    
    먼저 fruits 맵에 4 개의 과일 정보를 저장하고 있으며, sequenced에서 이 맵의 과일 정보를 읽어 영문으로 된 과일명에 해당하는 한글명을 출력합니다. 실행을 하면 다음과 같은 에러가 발생함을 확인할 수 있습니다.
    
    해당 오류는 map(fruits::get)에서 발생하는데, 이 메서드는 각 과일 이름에 대한 번역을 가져와야 합니다. 그러나 입력 스트림에 MELONS가 포함되어 있으므로, fruits.get(”melons”)가 실행되어 null을 반환합니다. 그리고 map(translated → “맛있는 “ + translated)에서 null 값은 문자열 결합 과정에서 NullPointerException을 발생하기에 문제가 생깁니다.
    
    이처럼, Hooks.onOperatorDebug() 메소드를 사용하면 디버그 모드를 활성화하여 에러가 발생한 지점을 명확하게 찾을 수 있습니다. 그러나 Operator 체인이 시작되기 전부터 디버그 모드를 활성하면 모든 스택트레이스를 캡쳐하기에 비용이 많이드는 방식입니다.
    
2. **checkpoint() Operator를 사용한 디버깅**
    
    디버그 모드를 활성하면 애플리케이션 내에 모든 스택트레이스를 캡쳐하는 반면, checkpoint()를 사용하면 Operator 체인 내의 특정 스택트레이스만 캡쳐합니다.
    
    checkpoint() 오퍼레이터는 세 가지 방법으로 이용할 수 있습니다.
    
    **1) Traceback을 출력하는 방뻐**
    
    checkpoint()를 사용하여 실제 에러가 발생한 지점 또는 에러가 전파된 지점의 traceback이 추가됩니다.
    
    ```java
    import lombok.extern.slf4j.Slf4j;
    import reactor.core.publisher.Flux;
    
    @Slf4j
    public class Example12_2 {
        public static void main(String[] args) {
            Flux
                    .just(2, 4, 6, 8)
                    .zipWith(Flux.just(1, 2, 3, 0), (x, y) -> x/y)
                    .map(num -> num + 2)
                    .checkpoint()
                    .subscribe(
                            data -> log.info("# onNext: {}", data),
                            error -> log.error("# onError:", error)
                    );
        }
    }
    
    //
    0:19:33.304 [main] INFO com.example.reactive.debug.Example12_2 -- # onNext: 4
    00:19:33.306 [main] INFO com.example.reactive.debug.Example12_2 -- # onNext: 4
    00:19:33.306 [main] INFO com.example.reactive.debug.Example12_2 -- # onNext: 4
    00:19:33.311 [main] ERROR com.example.reactive.debug.Example12_2 -- # onError:
    java.lang.ArithmeticException: / by zero
    	at com.example.reactive.debug.Example12_2.lambda$main$0(Example12_2.java:15)
    	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
    Assembly trace from producer [reactor.core.publisher.FluxMap] :
    	reactor.core.publisher.Flux.checkpoint(Flux.java:3559)
    	com.example.reactive.debug.Example12_2.main(Example12_2.java:17)
    Error has been observed at the following site(s):
    	*__checkpoint() ⇢ at com.example.reactive.debug.Example12_2.main(Example12_2.java:17)
    Original Stack Trace:
    		at com.example.reactive.debug.Example12_2.lambda$main$0(Example12_2.java:15)
    		at reactor.core.publisher.FluxZip$PairwiseZipper.apply(FluxZip.java:1190)
    		at reactor.core.publisher.FluxZip$PairwiseZipper.apply(FluxZip.java:1179)
    		at reactor.core.publisher.FluxZip$ZipCoordinator.drain(FluxZip.java:926)
    		at reactor.core.publisher.FluxZip$ZipInner.onSubscribe(FluxZip.java:1094)
    		at reactor.core.publisher.FluxArray.subscribe(FluxArray.java:53)
    		at reactor.core.publisher.FluxArray.subscribe(FluxArray.java:59)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8777)
    		at reactor.core.publisher.FluxZip$ZipCoordinator.subscribe(FluxZip.java:731)
    		at reactor.core.publisher.FluxZip.handleBoth(FluxZip.java:318)
    		at reactor.core.publisher.FluxZip.handleArrayMode(FluxZip.java:273)
    		at reactor.core.publisher.FluxZip.subscribe(FluxZip.java:137)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8777)
    		at reactor.core.publisher.Flux.subscribeWith(Flux.java:8898)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8742)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8666)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8636)
    		at com.example.reactive.debug.Example12_2.main(Example12_2.java:18)
    
    ```
    
    위의 코드 실행 결과, java.lang.ArithmeticException: / by zero를 통해 ArithmeticException이 발생한 것을 알 수 있지만, 어느 지점에서 발생한 것인지 확인되지 않았습니다. 아직은 에러가 직접적으로 발생한 지점인지 or 전파된 지점인지 알 수 없지만, 
    *__checkpoint() ⇢ at com.example.reactive.debug.Example12_2.main(Example12_2.java:17)
    Original Stack Trace: 
    
    를 통해 map() 다음에 추가한 checkpoint() 지점까지는 에러가 전파되었다는 것을 예상할 수 있습니다.
    
    ```java
    import lombok.extern.slf4j.Slf4j;
    import reactor.core.publisher.Flux;
    
    @Slf4j
    public class Example12_3 {
        public static void main(String[] args) {
            Flux
                    .just(2, 4, 6, 8)
                    .zipWith(Flux.just(1, 2, 3, 0), (x, y) -> x/y)
                    .checkpoint()
                    .map(num -> num + 2)
                    .checkpoint()
                    .subscribe(
                            data -> log.info("# onNext: {}", data),
                            error -> log.error("# onError:", error)
                    );
        }
    }
    
    //
    00:25:40.067 [main] INFO com.example.reactive.debug.Example12_3 -- # onNext: 4
    00:25:40.070 [main] INFO com.example.reactive.debug.Example12_3 -- # onNext: 4
    00:25:40.070 [main] INFO com.example.reactive.debug.Example12_3 -- # onNext: 4
    00:25:40.076 [main] ERROR com.example.reactive.debug.Example12_3 -- # onError:
    java.lang.ArithmeticException: / by zero
    	at com.example.reactive.debug.Example12_3.lambda$main$0(Example12_3.java:11)
    	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
    Assembly trace from producer [reactor.core.publisher.FluxZip] :
    	reactor.core.publisher.Flux.checkpoint(Flux.java:3559)
    	com.example.reactive.debug.Example12_3.main(Example12_3.java:12)
    Error has been observed at the following site(s):
    	*__checkpoint() ⇢ at com.example.reactive.debug.Example12_3.main(Example12_3.java:12)
    	|_ checkpoint() ⇢ at com.example.reactive.debug.Example12_3.main(Example12_3.java:14)
    Original Stack Trace:
    		at com.example.reactive.debug.Example12_3.lambda$main$0(Example12_3.java:11)
    		at reactor.core.publisher.FluxZip$PairwiseZipper.apply(FluxZip.java:1190)
    		at reactor.core.publisher.FluxZip$PairwiseZipper.apply(FluxZip.java:1179)
    		at reactor.core.publisher.FluxZip$ZipCoordinator.drain(FluxZip.java:926)
    		at reactor.core.publisher.FluxZip$ZipInner.onSubscribe(FluxZip.java:1094)
    		at reactor.core.publisher.FluxArray.subscribe(FluxArray.java:53)
    		at reactor.core.publisher.FluxArray.subscribe(FluxArray.java:59)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8777)
    		at reactor.core.publisher.FluxZip$ZipCoordinator.subscribe(FluxZip.java:731)
    		at reactor.core.publisher.FluxZip.handleBoth(FluxZip.java:318)
    		at reactor.core.publisher.FluxZip.handleArrayMode(FluxZip.java:273)
    		at reactor.core.publisher.FluxZip.subscribe(FluxZip.java:137)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8777)
    		at reactor.core.publisher.Flux.subscribeWith(Flux.java:8898)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8742)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8666)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8636)
    		at com.example.reactive.debug.Example12_3.main(Example12_3.java:15)
    ```
    
    위의 코드는 전의 코드와 달리 checkpoint()를 하나 더 추가하였습니다. 실행 결과 두 지점 모두 에러와 관련이 있음을 알 수 있습니다.
    
    두 개 모두 Traceback이 출력되었으므로, 두 번쨰 checkpoint()는 에러가 전파되었음을, 첫 번째 checkpont()는 zipWith()에서 직접적으로 에러가 발생했음을 알 수 있습니다.
    
    원본 데이터를 emit하는 just()에는 특별한 처리로직이 없기에 에러가 발생할 가능성이 없기에 zipWith()에서 에러가 발생하였음을 확신할 수 있습니다.
    
    **2) Traceback 없이 식별자를 포함한 Description을 출력해서 에러 발생 지점을 예상하는 방법**
    
    checkpoint(Description)을 사용하면 에러 발생 시 Traceback을 생략하고 Description을 통해 에러 발생 지점을 예상할 수 있습니다.
    
    ```java
    import lombok.extern.slf4j.Slf4j;
    import reactor.core.publisher.Flux;
    
    @Slf4j
    public class Example12_4 {
        public static void main(String[] args) {
            Flux
                .just(2, 4, 6, 8)
                .zipWith(Flux.just(1, 2, 3, 0), (x, y) -> x/y)
                .checkpoint("Example12_4.zipWith.checkpoint")
                .map(num -> num + 2)
                .checkpoint("Example12_4.map.checkpoint")
                .subscribe(
                        data -> log.info("# onNext: {}", data),
                        error -> log.error("# onError:", error)
                );
        }
    }
    
    //
    00:32:08.464 [main] INFO com.example.reactive.debug.Example12_4 -- # onNext: 4
    00:32:08.466 [main] INFO com.example.reactive.debug.Example12_4 -- # onNext: 4
    00:32:08.466 [main] INFO com.example.reactive.debug.Example12_4 -- # onNext: 4
    00:32:08.472 [main] ERROR com.example.reactive.debug.Example12_4 -- # onError:
    java.lang.ArithmeticException: / by zero
    	at com.example.reactive.debug.Example12_4.lambda$main$0(Example12_4.java:11)
    	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
    Error has been observed at the following site(s):
    	*__checkpoint ⇢ Example12_4.zipWith.checkpoint
    	|_ checkpoint ⇢ Example12_4.map.checkpoint
    Original Stack Trace:
    		at com.example.reactive.debug.Example12_4.lambda$main$0(Example12_4.java:11)
    		at reactor.core.publisher.FluxZip$PairwiseZipper.apply(FluxZip.java:1190)
    		at reactor.core.publisher.FluxZip$PairwiseZipper.apply(FluxZip.java:1179)
    		at reactor.core.publisher.FluxZip$ZipCoordinator.drain(FluxZip.java:926)
    		at reactor.core.publisher.FluxZip$ZipInner.onSubscribe(FluxZip.java:1094)
    		at reactor.core.publisher.FluxArray.subscribe(FluxArray.java:53)
    		at reactor.core.publisher.FluxArray.subscribe(FluxArray.java:59)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8777)
    		at reactor.core.publisher.FluxZip$ZipCoordinator.subscribe(FluxZip.java:731)
    		at reactor.core.publisher.FluxZip.handleBoth(FluxZip.java:318)
    		at reactor.core.publisher.FluxZip.handleArrayMode(FluxZip.java:273)
    		at reactor.core.publisher.FluxZip.subscribe(FluxZip.java:137)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8777)
    		at reactor.core.publisher.Flux.subscribeWith(Flux.java:8898)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8742)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8666)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8636)
    		at com.example.reactive.debug.Example12_4.main(Example12_4.java:15)
    ```
    
    위의 checkpoint()와 다르게 매개변수로 description을 추가해 Traceback 대신 description을 출력하도록 코드를 작성하였습니다. 실행결과를 보면 description이 출력됨을 확인할 수 있습니다.
    
    **3) Traceback과 Description을 모두 출력하는 방법**
    
    checkpoint(description, forceStackTrace)를 사용하면 description과 Traceback을 모두 출력할 수 있습니다.
    
    ```java
    import lombok.extern.slf4j.Slf4j;
    import reactor.core.publisher.Flux;
    
    @Slf4j
    public class Example12_5 {
        public static void main(String[] args) {
            Flux
                .just(2, 4, 6, 8)
                .zipWith(Flux.just(1, 2, 3, 0), (x, y) -> x/y)
                .checkpoint("Example12_4.zipWith.checkpoint", true)
                .map(num -> num + 2)
                .checkpoint("Example12_4.map.checkpoint", true)
                .subscribe(
                        data -> log.info("# onNext: {}", data),
                        error -> log.error("# onError:", error)
                );
        }
    }
    
    00:34:53.977 [main] INFO com.example.reactive.debug.Example12_5 -- # onNext: 4
    00:34:53.979 [main] INFO com.example.reactive.debug.Example12_5 -- # onNext: 4
    00:34:53.979 [main] INFO com.example.reactive.debug.Example12_5 -- # onNext: 4
    00:34:53.984 [main] ERROR com.example.reactive.debug.Example12_5 -- # onError:
    java.lang.ArithmeticException: / by zero
    	at com.example.reactive.debug.Example12_5.lambda$main$0(Example12_5.java:16)
    	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
    Assembly trace from producer [reactor.core.publisher.FluxZip], described as [Example12_4.zipWith.checkpoint] :
    	reactor.core.publisher.Flux.checkpoint(Flux.java:3624)
    	com.example.reactive.debug.Example12_5.main(Example12_5.java:17)
    Error has been observed at the following site(s):
    	*__checkpoint(Example12_4.zipWith.checkpoint) ⇢ at com.example.reactive.debug.Example12_5.main(Example12_5.java:17)
    	|_     checkpoint(Example12_4.map.checkpoint) ⇢ at com.example.reactive.debug.Example12_5.main(Example12_5.java:19)
    Original Stack Trace:
    		at com.example.reactive.debug.Example12_5.lambda$main$0(Example12_5.java:16)
    		at reactor.core.publisher.FluxZip$PairwiseZipper.apply(FluxZip.java:1190)
    		at reactor.core.publisher.FluxZip$PairwiseZipper.apply(FluxZip.java:1179)
    		at reactor.core.publisher.FluxZip$ZipCoordinator.drain(FluxZip.java:926)
    		at reactor.core.publisher.FluxZip$ZipInner.onSubscribe(FluxZip.java:1094)
    		at reactor.core.publisher.FluxArray.subscribe(FluxArray.java:53)
    		at reactor.core.publisher.FluxArray.subscribe(FluxArray.java:59)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8777)
    		at reactor.core.publisher.FluxZip$ZipCoordinator.subscribe(FluxZip.java:731)
    		at reactor.core.publisher.FluxZip.handleBoth(FluxZip.java:318)
    		at reactor.core.publisher.FluxZip.handleArrayMode(FluxZip.java:273)
    		at reactor.core.publisher.FluxZip.subscribe(FluxZip.java:137)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8777)
    		at reactor.core.publisher.Flux.subscribeWith(Flux.java:8898)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8742)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8666)
    		at reactor.core.publisher.Flux.subscribe(Flux.java:8636)
    		at com.example.reactive.debug.Example12_5.main(Example12_5.java:20)
    ```
    
    .checkpoint("Example12_4.zipWith.checkpoint", true)에서 두 번째 파라미터의 값을 true로 설정하면 에러 발생 시 description과 Traceback을 모두 출력할 수 있습니다. 
    
    하지만 위의 코드와 같이 단순한 Operator 체인이라면 checkpoint()를 추가하지 않더라도 에러가 발생한 지점을 육안으로 직접 찾을 수 있습니다. 하지만 체인이 복잡해지면 에러 발생 지점을 찾기 위해 checkpoint()를 많은 고ㅗㅅ에 두어야 하기에 에러 발생 지점을 찾는 것이 쉽지 않을 수 있습니다.
    
3. log() Operator를 사용한 디버깅
    
    log()는 Reactor Sequence의 동작 로그를 출력하는데, 이 로그를 통해 디버깅이 가능합니다.
    
    ```java
    import lombok.extern.slf4j.Slf4j;
    import reactor.core.publisher.Flux;
    import java.util.HashMap;
    import java.util.Map;
    
    @Slf4j
    public class Example12_7 {
        public static Map<String, String> fruits = new HashMap<>();
    
        static {
            fruits.put("banana", "바나나");
            fruits.put("apple", "사과");
            fruits.put("pear", "배");
            fruits.put("grape", "포도");
        }
    
        public static void main(String[] args) {
            Flux.fromArray(new String[]{"BANANAS", "APPLES", "PEARS", "MELONS"})
                    .map(String::toLowerCase)
                    .map(fruit -> fruit.substring(0, fruit.length() - 1))
                    .log()
    //                .log("Fruit.Substring", Level.FINE)
                    .map(fruits::get)
                    .subscribe(
                            log::info,
                            error -> log.error("# onError:", error));
        }
    }
    
    //
    00:42:16.572 [main] INFO reactor.Flux.MapFuseable.1 -- | onSubscribe([Fuseable] FluxMapFuseable.MapFuseableSubscriber)
    00:42:16.574 [main] INFO reactor.Flux.MapFuseable.1 -- | request(unbounded)
    00:42:16.574 [main] INFO reactor.Flux.MapFuseable.1 -- | onNext(banana)
    00:42:16.574 [main] INFO com.example.reactive.debug.Example12_7 -- 바나나
    00:42:16.574 [main] INFO reactor.Flux.MapFuseable.1 -- | onNext(apple)
    00:42:16.575 [main] INFO com.example.reactive.debug.Example12_7 -- 사과
    00:42:16.575 [main] INFO reactor.Flux.MapFuseable.1 -- | onNext(pear)
    00:42:16.575 [main] INFO com.example.reactive.debug.Example12_7 -- 배
    00:42:16.575 [main] INFO reactor.Flux.MapFuseable.1 -- | onNext(melon)
    00:42:16.576 [main] INFO reactor.Flux.MapFuseable.1 -- | cancel()
    00:42:16.577 [main] ERROR com.example.reactive.debug.Example12_7 -- # onError:
    java.lang.NullPointerException: The mapper [com.example.reactive.debug.Example12_7$$Lambda$15/0x0000000800c3f060] returned a null value.
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:115)
    	at reactor.core.publisher.FluxPeekFuseable$PeekFuseableSubscriber.onNext(FluxPeekFuseable.java:210)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:129)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:129)
    	at reactor.core.publisher.FluxArray$ArraySubscription.fastPath(FluxArray.java:171)
    	at reactor.core.publisher.FluxArray$ArraySubscription.request(FluxArray.java:96)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.request(FluxMapFuseable.java:171)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.request(FluxMapFuseable.java:171)
    	at reactor.core.publisher.FluxPeekFuseable$PeekFuseableSubscriber.request(FluxPeekFuseable.java:144)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.request(FluxMapFuseable.java:171)
    	at reactor.core.publisher.LambdaSubscriber.onSubscribe(LambdaSubscriber.java:119)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onSubscribe(FluxMapFuseable.java:96)
    	at reactor.core.publisher.FluxPeekFuseable$PeekFuseableSubscriber.onSubscribe(FluxPeekFuseable.java:178)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onSubscribe(FluxMapFuseable.java:96)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onSubscribe(FluxMapFuseable.java:96)
    	at reactor.core.publisher.FluxArray.subscribe(FluxArray.java:53)
    	at reactor.core.publisher.FluxArray.subscribe(FluxArray.java:59)
    	at reactor.core.publisher.Flux.subscribe(Flux.java:8777)
    	at reactor.core.publisher.Flux.subscribeWith(Flux.java:8898)
    	at reactor.core.publisher.Flux.subscribe(Flux.java:8742)
    	at reactor.core.publisher.Flux.subscribe(Flux.java:8666)
    	at reactor.core.publisher.Flux.subscribe(Flux.java:8636)
    	at com.example.reactive.debug.Example12_7.main(Example12_7.java:30)
    ```
    
    코드 실행결과 다음과 같이 오류가 발생함을 확인할 수 있습니다. 그리고 어느 지점에서 에러가 발생했는지 찾기 위해 우선 두 번째 map() 다음 log() Operator를 추가하였습니다.
    
    log()를 추가한 결과, Subscriber에 전달된 결과 이외에 onSubscribe(), request(), onNext()와 같은 Signal이 출력되었습니다. 이 Signal은 두 번째 map에서 발생한 것들입니다.
    
    실행 결과 00:42:16.576 [main] INFO reactor.Flux.MapFuseable.1 -- | cancel()
    
    가 출력됨을 확인할 수 있습니다. 이는 두 번째 map()이 melon을 emit했지만 두 번째 map() 이후 어떤 지점에서 melon 문자열을 추가하는 과정에서 문제가 발생했음을 의미합니다.
    
    다행히 두 번째 map() 이후 다른 map() Operator가 하나밖에 없기 때문에 여기서 어떤 문제가 발생했음을 예상할 수 있습니다. 
    
    로그를 좀 더 자세히 분석하기 위해 log를 다음과 같이 바꿔 실행해보도록 하겠습니다.
    
    ```java
    .log("Fruit.Substring", Level.FINE)
    
    //실행 결과
    00:49:31.447 [main] INFO com.example.reactive.debug.Example12_7 -- 바나나
    00:49:31.449 [main] INFO com.example.reactive.debug.Example12_7 -- 사과
    00:49:31.449 [main] INFO com.example.reactive.debug.Example12_7 -- 배
    00:49:31.452 [main] ERROR com.example.reactive.debug.Example12_7 -- # onError:
    java.lang.NullPointerException: The mapper [com.example.reactive.debug.Example12_7$$Lambda$15/0x0000000800c84000] returned a null value.
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:115)
    	at reactor.core.publisher.FluxPeekFuseable$PeekFuseableSubscriber.onNext(FluxPeekFuseable.java:210)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:129)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:129)
    	at reactor.core.publisher.FluxArray$ArraySubscription.fastPath(FluxArray.java:171)
    	at reactor.core.publisher.FluxArray$ArraySubscription.request(FluxArray.java:96)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.request(FluxMapFuseable.java:171)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.request(FluxMapFuseable.java:171)
    	at reactor.core.publisher.FluxPeekFuseable$PeekFuseableSubscriber.request(FluxPeekFuseable.java:144)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.request(FluxMapFuseable.java:171)
    	at reactor.core.publisher.LambdaSubscriber.onSubscribe(LambdaSubscriber.java:119)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onSubscribe(FluxMapFuseable.java:96)
    	at reactor.core.publisher.FluxPeekFuseable$PeekFuseableSubscriber.onSubscribe(FluxPeekFuseable.java:178)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onSubscribe(FluxMapFuseable.java:96)
    	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onSubscribe(FluxMapFuseable.java:96)
    	at reactor.core.publisher.FluxArray.subscribe(FluxArray.java:53)
    	at reactor.core.publisher.FluxArray.subscribe(FluxArray.java:59)
    	at reactor.core.publisher.Flux.subscribe(Flux.java:8777)
    	at reactor.core.publisher.Flux.subscribeWith(Flux.java:8898)
    	at reactor.core.publisher.Flux.subscribe(Flux.java:8742)
    	at reactor.core.publisher.Flux.subscribe(Flux.java:8666)
    	at reactor.core.publisher.Flux.subscribe(Flux.java:8636)
    	at com.example.reactive.debug.Example12_7.main(Example12_7.java:31)
    ```
    
    출력 결과 로그 레벨이 모두 DEBUG로 바뀌었고, 이 로그들이 두 번쨰 map()에서 발생한 Signal이라는 것을 쉽게 구분할 수 있도록 Fruit.Substring이라는 카테고리까지 표시해 줍니다.
    
    이처럼, log를 사용하면 에러가 발생한 지점에서 단계적으로 접근할 수 있고, 사용 개수에 제한이 없기 때문에 다른 Operator 뒤에 추가하여 Sequence 내부 동작을 좀 더 상세하게 분석하면서 디버깅 할 수 있습니다.