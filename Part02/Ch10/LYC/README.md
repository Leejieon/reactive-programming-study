# Scheduler

- Reactor에서 사용되는 Scheduler는 Sequence에서 사용되는 Thread를 관리해주는 관리자 역할을 합니다.

- Thread 개념과 이해
    - Physical Thread
        
        ![물리적스레드.PNG](https://file.notion.so/f/f/9e132be8-6c00-4f39-b489-0bf5bacbff36/092542d1-e78d-4706-811c-a57112602c7f/%EB%AC%BC%EB%A6%AC%EC%A0%81%EC%8A%A4%EB%A0%88%EB%93%9C.png?id=1adb9cec-120b-46e0-a6ab-b190af46e00d&table=block&spaceId=9e132be8-6c00-4f39-b489-0bf5bacbff36&expirationTimestamp=1709287200000&signature=cKNt3YtU4xey5rH0GLIKcmDYrj7UsOR35E62awny1uM&downloadName=%EB%AC%BC%EB%A6%AC%EC%A0%81%EC%8A%A4%EB%A0%88%EB%93%9C.PNG.png)
        
        위의 그림을 보면, 한 개의 코어는 두 개의 Thread를 포함하고 있는데 이 두 개의 Thread는 물리적인 코어를 논리적으로 나눈 것을 의미하며, 이렇게 물리적인 코어를 논리적으로 나눈 코어를 Physical Thread 라고 합니다. 하드웨어와 관련된 Thread는 Physical Thread라고 생각할 수 있습니다
        
    - Logical Thread
        
        ![논리적스레드.PNG](https://file.notion.so/f/f/9e132be8-6c00-4f39-b489-0bf5bacbff36/58b2b5c5-1c72-4d7b-aae7-ae8b00895e6c/%EB%85%BC%EB%A6%AC%EC%A0%81%EC%8A%A4%EB%A0%88%EB%93%9C.png?id=3dfb9a4c-ec3f-498e-8150-869d781cb427&table=block&spaceId=9e132be8-6c00-4f39-b489-0bf5bacbff36&expirationTimestamp=1709287200000&signature=xx0N68J7uq3yO3-kc3D1nTbteR-tUX1T03PLI8iYSHw&downloadName=%EB%85%BC%EB%A6%AC%EC%A0%81%EC%8A%A4%EB%A0%88%EB%93%9C.PNG.png)
        
        논리적 스레드란, 소프트웨어적으로 생성되는 Thread를 의미하며, Java에서 사용되는 Thread는 Logical Thread입니다.
        
        Logical Thread는 메모리가 허용하는 범위 내에서 얼마든지 만들 수 있지만 Physical Thread의 가용 범위 내에서 실행될 수 있습니다.
        
    - 물리적 스레드는 **병렬성 /** 논리적 스레드는 **동시성**
        
        병렬성은 여러 작업을 동시에 처리함을 의미합니다. 동시성은 용어 자체의 의미 때문에 동시에 실행 된다고 생각할 수 있지만 동시에 실행되는 것처럼 보이는 것을 의미합니다.
        
        위의 사진과 같이, 무수히 많은 논리적 스레드가 네 개의 물리적 스레드를 아주 빠른 속도로 번갈아 가며 사용하면서 마치 동시에 실행되는 것처럼 보이는 동시성을 지니게 됩니다.
        
        즉, 동시성을 지닌 논리적 스레드는 물리적 스레드의 갯수 내에서 실행이 됩니다.
        
- Scheduler란?
    
    운영체제에서 Scheduler는 실행되는 프로그램인 프로세스를 선택하고 실행하는 등 프로세스의 라이프 사이클을 관리해 주는 관리자 역할을 합니다.
    
    Java 프로그래밍에서 멀티스레드를 제어하는것은 어렵기에 Reactor에서는 Scheduler를 사용하여 스레드의 제어를 위탁할 수 있습니다.
    
    - subscribeOn()
        
        이 Operator는 그 이름처럼 구독이 발생한 직후 실행될 스레드를 저장하는 Operator입니다. 즉 구독 시점 직후 원본 Publisher의 동작을 수행하기 위한 스레드라고 볼 수 있습니다.
        
        ```jsx
        
        public class Example10_1 {
            static Logger log = LoggerFactory.getLogger(Example10_1.class);
        
            public static void main(String[] args) throws InterruptedException {
        
                Flux.fromArray(new Integer[] {1,3,5,7})
                        .subscribeOn(Schedulers.boundedElastic())
                        .doOnNext(data -> log.info("# doOnNext : {}", data))
                        .doOnSubscribe(subscription -> log.info("# doOnSubsribe"))
                        .subscribe(data -> log.info("# onNext: {}", data));
        
                Thread.sleep(500L);
            }
        }
        
        ///
        11:25:53.129 [main] INFO reactive.Example10_1 -- # doOnSubsribe
        11:25:53.132 [boundedElastic-1] INFO reactive.Example10_1 -- # doOnnext : 1
        11:25:53.133 [boundedElastic-1] INFO reactive.Example10_1 -- # on next: 1
        11:25:53.133 [boundedElastic-1] INFO reactive.Example10_1 -- # doOnnext : 3
        11:25:53.133 [boundedElastic-1] INFO reactive.Example10_1 -- # on next: 3
        11:25:53.133 [boundedElastic-1] INFO reactive.Example10_1 -- # doOnnext : 5
        11:25:53.133 [boundedElastic-1] INFO reactive.Example10_1 -- # on next: 5
        11:25:53.133 [boundedElastic-1] INFO reactive.Example10_1 -- # doOnnext : 7
        11:25:53.133 [boundedElastic-1] INFO reactive.Example10_1 -- # on next: 7
        ```
        
        1. subscribeOn() Operator를 추가했기 때문에 구독이 발생한 직후에 원본 Publisher의 동작을 처리하기 위한 스레드를 할당합니다. subcribeOn()의 파라미터를 통해 Scheduler를 지정할 수 있는데 현재 boundedElastic이라는 유형의 Scheduler를 사용하였습니다.
        2. doOnNext() Operator를 사용해 원본 Flux에서 emit되는 데이터(정수형 배열)을 로그로 출력합니다.
        3. doOnSubscribe() Operator를 사용해 구독이 발생한 시점에 추가적인 어떤 처리가 필요할 경우 해당 처리 동작을 추가할 수 있습니다. 현재 구독이 발생한 시점에 실행되는 스레드가 무엇인지 학인합니다.
        - 실행결과를 보면 첫 doOnSubscribe() 동작은 main 스레드에서 실행되는데, 그 이유는 최초의 실행 스레드가 main 스레드이기 때문입니다. 이 후 subscribeOn() 연산자를 통해 boundedElastic 스레드를 지정했기 때문에 이 후 동작에서 boundedElastic 스레드에서 실행됩니다.
        
    - publishOn()
        
        Operator는 Downstream으로 Signal을 전송할 떄 실행되는 스레드를 제어하는 역할을 하는 Operator라고 할 수 있습니다.  코드 상에서 publishOn()을 기준으로 아래쪽인 Downstream의 실행 스레드를 변경합니다. 그리고 파라미터로 Scheduler를 지정함으로써 해당 Scheduler의 특성을 가진 스레드로 변경할 수 있습니다.
        
        ```jsx
        public class Example10_2 {
            static Logger log = LoggerFactory.getLogger(Example10_2.class);
            
            public static void main(String[] args) throws InterruptedException {
                Flux.fromArray(new Integer[]{1, 3, 5, 7})
                        .doOnNext(data -> log.info("# doOnNext : {}", data))
                        .doOnSubscribe(subscription -> log.info("# doOnSubscribe"))
                        .publishOn(Schedulers.parallel())
                        .subscribe(data -> log.info("# onNext: {}", data));
        
                Thread.sleep(500L);
            }
        }
        
        ///
        12:02:29.311 [main] INFO reactive.Example10_2 -- # doOnSubscribe
        12:02:29.317 [main] INFO reactive.Example10_2 -- # doOnNext : 1
        12:02:29.319 [main] INFO reactive.Example10_2 -- # doOnNext : 3
        12:02:29.319 [parallel-1] INFO reactive.Example10_2 -- # onNext: 1
        12:02:29.319 [parallel-1] INFO reactive.Example10_2 -- # onNext: 3
        12:02:29.319 [main] INFO reactive.Example10_2 -- # doOnNext : 5
        12:02:29.319 [main] INFO reactive.Example10_2 -- # doOnNext : 7
        12:02:29.319 [parallel-1] INFO reactive.Example10_2 -- # onNext: 5
        12:02:29.319 [parallel-1] INFO reactive.Example10_2 -- # onNext: 7
        ```
        
        doOnNext()의 경우 subsriveOn() 을 사용하지 않았기에 main 스레드에서 실행됩니다. 그런데 onNext()의 경우 publishOn()을 추가했기 때문에 이 연산자를 기준으로 Downstream의 실행 스레드가 변경되어 parallel 스레드에서 실행됩니다.
        
    - parallel()
        
        subscribeOn()와 publishOn()의 경우, 동시성을 가지는 논리적 스레드에 해당되지만 parallel()은 병렬성을 가지는 물리적 스레드에 해당됩니다.
        
        ```jsx
        public class Example10_3 {
            static Logger log = LoggerFactory.getLogger(Example10_3.class);
        
            public static void main(String[] args) throws InterruptedException {
        
                Flux.fromArray(new Integer[] {1,3,5,7,9,11,13,15,17,19})
                        .parallel()
                        .runOn(Schedulers.parallel())
                        .subscribe(data -> log.info("# on next: {}", data));
                Thread.sleep(100L);
            }
        }
        
        //
        08:57:34.773 [parallel-2] INFO reactive.Example10_3 -- # on next: 3
        08:57:34.773 [parallel-5] INFO reactive.Example10_3 -- # on next: 9
        08:57:34.773 [parallel-7] INFO reactive.Example10_3 -- # on next: 13
        08:57:34.773 [parallel-1] INFO reactive.Example10_3 -- # on next: 1
        08:57:34.773 [parallel-6] INFO reactive.Example10_3 -- # on next: 11
        08:57:34.776 [parallel-1] INFO reactive.Example10_3 -- # on next: 17
        08:57:34.776 [parallel-2] INFO reactive.Example10_3 -- # on next: 19
        08:57:34.773 [parallel-8] INFO reactive.Example10_3 -- # on next: 15
        08:57:34.773 [parallel-4] INFO reactive.Example10_3 -- # on next: 7
        08:57:34.773 [parallel-3] INFO reactive.Example10_3 -- # on next: 5
        ```
        
        parallel() Operator는 emit되는 데이터를 CPU의 논리적인 코어 수에 맞게 사전에 골고루 분배하는 역할만 하며, 실제로 병렬 작업을 수행할 스레드의 할당은 runOn() Operator가 담당합니다.
        
        ```jsx
        public class Example10_4 {
            static Logger log = LoggerFactory.getLogger(Example10_3.class);
        
            public static void main(String[] args) throws InterruptedException {
        
                Flux.fromArray(new Integer[] {1,3,5,7,9,11,13,15,17,19})
                        .parallel(4)
                        .runOn(Schedulers.parallel())
                        .subscribe(data -> log.info("# on next: {}", data));
                Thread.sleep(100L);
            }
        }
        
        09:00:36.736 [parallel-4] INFO reactive.Example10_3 -- # on next: 7
        09:00:36.737 [parallel-2] INFO reactive.Example10_3 -- # on next: 3
        09:00:36.736 [parallel-3] INFO reactive.Example10_3 -- # on next: 5
        09:00:36.739 [parallel-3] INFO reactive.Example10_3 -- # on next: 13
        09:00:36.737 [parallel-1] INFO reactive.Example10_3 -- # on next: 1
        09:00:36.739 [parallel-1] INFO reactive.Example10_3 -- # on next: 9
        09:00:36.739 [parallel-4] INFO reactive.Example10_3 -- # on next: 15
        09:00:36.739 [parallel-2] INFO reactive.Example10_3 -- # on next: 11
        09:00:36.739 [parallel-1] INFO reactive.Example10_3 -- # on next: 17
        09:00:36.739 [parallel-2] INFO reactive.Example10_3 -- # on next: 19
        ```
        
        위의 예제와 달리 병렬 작업을 처리하기 위해 4개의 스레드만 지정하였습니다. 그 결과 실행되는 스레드는 4개임을 볼 수 있습니다.
        
- Scheduler의 종류
    - Scheduler.immediate()
        
        ```java
        public class Example10_9 {
            static Logger log = LoggerFactory.getLogger(Example10_9.class);
        
            public static void main(String[] args) throws InterruptedException {
        
                Flux
                        .fromArray(new Integer[] {1,3,5,7})
                        .publishOn(Schedulers.parallel())
                        .filter(data -> data > 3)
                        .doOnNext(data -> log.info("# doOnNext filter {}", data))
                        .publishOn(Schedulers.immediate())
                        .map(data-> data*10)
                        .doOnNext(data -> log.info("# doOnNext map: {}", data))
                        .subscribe(data -> log.info("# onNext: {}", data));
        
                Thread.sleep(100L);
            }
        }
        
        ///
        11:09:35.210 [parallel-1] INFO reactive.Example10_9 -- # doOnNext filter 5
        11:09:35.212 [parallel-1] INFO reactive.Example10_9 -- # doOnNext map: 50
        11:09:35.212 [parallel-1] INFO reactive.Example10_9 -- # onNext: 50
        11:09:35.212 [parallel-1] INFO reactive.Example10_9 -- # doOnNext filter 7
        11:09:35.212 [parallel-1] INFO reactive.Example10_9 -- # doOnNext map: 70
        11:09:35.212 [parallel-1] INFO reactive.Example10_9 -- # onNext: 70
        ```
        
        immedate()를 사용하면 추가 스레드를 생성하지 않고, 현재 스레드를 그대로 사용하여 작업을 처리하기 때문에, parallel-1이 현재 스레드가 됩니다.
        
    - Scheduler.single()
        
        스레드 하나만 생성해서 Scheduler가 제거되기 전까지 재사용하는 방식입니다.
        
        ```java
        public class Example10_10 {
            static Logger log = LoggerFactory.getLogger(Example10_10.class);
        
            public static void main(String[] args) throws InterruptedException {
        
                doTask("task1")
                        .subscribe(data -> log.info("# onNext: {}", data));
        
                doTask("task2")
                        .subscribe(data -> log.info("# onNext: {}", data));
        
                Thread.sleep(200L);
            }
        
            private static Flux<Integer> doTask(String taskName) {
                return Flux.fromArray(new Integer[] {1,3,5,7})
                        .publishOn(Schedulers.single())
                        .filter(data -> data > 3)
                        .doOnNext(data -> log.info("# {} doOnNext filter: {}", taskName, data))
                        .map(data -> data * 10)
                        .doOnNext(data -> log.info("# {} doOnNext map: {}", taskName, data));
            }
        }
        
        ///
        11:28:12.862 [single-1] INFO reactive.Example10_10 -- # task1 doOnNext filter: 5
        11:28:12.864 [single-1] INFO reactive.Example10_10 -- # task1 doOnNext map: 50
        11:28:12.864 [single-1] INFO reactive.Example10_10 -- # onNext: 50
        11:28:12.864 [single-1] INFO reactive.Example10_10 -- # task1 doOnNext filter: 7
        11:28:12.864 [single-1] INFO reactive.Example10_10 -- # task1 doOnNext map: 70
        11:28:12.864 [single-1] INFO reactive.Example10_10 -- # onNext: 70
        11:28:12.865 [single-1] INFO reactive.Example10_10 -- # task2 doOnNext filter: 5
        11:28:12.865 [single-1] INFO reactive.Example10_10 -- # task2 doOnNext map: 50
        11:28:12.865 [single-1] INFO reactive.Example10_10 -- # onNext: 50
        11:28:12.865 [single-1] INFO reactive.Example10_10 -- # task2 doOnNext filter: 7
        11:28:12.865 [single-1] INFO reactive.Example10_10 -- # task2 doOnNext map: 70
        11:28:12.865 [single-1] INFO reactive.Example10_10 -- # onNext: 70
        
        ```
        
        위의 코드와 같이,
        
        doTask() 라는 메서드를 다른 작업으로 두 번 호출하였습니다. 하지만 single() 을 사용하였기에 스레드를 재사용하여, 같은 스레드로 작업을 처리합니다.
        
    - Scheduler.newSingle()
        
        Scheduler.single()이 하나의 스레드를 재사용하는 반면에, Schedulers.newSingle()은 호출할 때마다 매번 새로운 스레드 하나를 생성합니다.
        
        ```java
        public class Example10_11 {
            static Logger log = LoggerFactory.getLogger(Example10_10.class);
        
            public static void main(String[] args) throws InterruptedException {
        
                doTask("task1")
                        .subscribe(data -> log.info("# onNext: {}", data));
        
                doTask("task2")
                        .subscribe(data -> log.info("# onNext: {}", data));
        
                Thread.sleep(200L);
            }
        
            private static Flux<Integer> doTask(String taskName) {
                return Flux.fromArray(new Integer[] {1,3,5,7})
                        .publishOn(Schedulers.newSingle("new-single"), true)
                        .filter(data -> data > 3)
                        .doOnNext(data -> log.info("# {} doOnNext filter: {}", taskName, data))
                        .map(data -> data * 10)
                        .doOnNext(data -> log.info("# {} doOnNext map: {}", taskName, data));
            }
        }
        
        ///
        15:49:04.513 [new-single-2] INFO reactive.Example10_11 -- # task2 doOnNext filter: 5
        15:49:04.513 [new-single-1] INFO reactive.Example10_11 -- # task1 doOnNext filter: 5
        15:49:04.515 [new-single-2] INFO reactive.Example10_11 -- # task2 doOnNext map: 50
        15:49:04.515 [new-single-1] INFO reactive.Example10_11 -- # task1 doOnNext map: 50
        15:49:04.515 [new-single-2] INFO reactive.Example10_11 -- # onNext: 50
        15:49:04.515 [new-single-1] INFO reactive.Example10_11 -- # onNext: 50
        15:49:04.515 [new-single-2] INFO reactive.Example10_11 -- # task2 doOnNext filter: 7
        15:49:04.515 [new-single-1] INFO reactive.Example10_11 -- # task1 doOnNext filter: 7
        15:49:04.515 [new-single-2] INFO reactive.Example10_11 -- # task2 doOnNext map: 70
        15:49:04.515 [new-single-1] INFO reactive.Example10_11 -- # task1 doOnNext map: 70
        15:49:04.515 [new-single-2] INFO reactive.Example10_11 -- # onNext: 70
        15:49:04.515 [new-single-1] INFO reactive.Example10_11 -- # onNext: 70
        ```
        
    - Scheduler.boundedElastic()
        
        ExecutorService 기반의 스레드 풀을 생성한 후 그 안에서 정해진 수만큼의 스레드를 사용하여 작업을 처리하고 작업이 종료된 스레드는 반납하여 재사용하는 방식입니다.
        
        기본적으로 CPU 코어 수 X 10 만큼의 스레드를 생성하여 모든 스레드가 작업을 처리하고 있다면 이용 가능한 스레드가 생길 때 까지 최대 100,000개의 작업이 큐에서 대기할 수 있습니다.
        
        실제 데이터베이스를 통한 질의나 HTTP 요청 같은 작업을 통해 전달 받은 데이터를 데이터 소스로 사용하는 경우가 많습니다. 이러한 작업을 효과처리하기 위한 방식으로, 실행 기간이 긴 작업이 포함된 경우, 다른 Non-Blocking 처리에 영향을 주지 않도록 전용 스레드를 할당하여 작업을 처리하기 때문입니다.