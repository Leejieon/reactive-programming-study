# Backpressure

- 정의
    
    Publisher가 끊임없이 emit하는 무수히 많은 데이터를 적절히 제어하여 데이터 처리에 과부하가 걸리지 않도록 제어하는 역할입니다.
    
- Upstream / Downstream 차이

|  | Upstream Publisher | Downstream Publisher |
| --- | --- | --- |
| 역할 및 위치 | 데이터를 생성하고 발생시키는 스트림의 시작 부분으로, 외부 소스나 이벤트에서 데이터를 생성 | 스트림에서 종단 부분에 위치하며, 생성된 데이터를 받아와서 처리하고 소비하는 역할 |
| 흐름 방향 | Up → Down ( Publisher는 데이터를 생성하고 전달) | Down → Up (Subscriber는 Pubisher에서 데이터를 받아와 처리) |
| 컨트롤 | 데이터의 발행을 제어 → 데이터를 생성하여 downstream에 전달 | 데이터의 구독을 제어   → Subscriber는 Publisher로부터 데이터를 요청하고 받아옴 |
| 즉, | 생성 | 처리 |

- Reactor 에서의 Backpresure 처리 방식
    
    Reactor에는 Backpresure를 처리하는 다양한 유형이 있습니다.
    
    - 데이터 개수 제어
        
        Subscriber가 적절히 처리할 수 있는 수준의 데이터 개수를 Publisher에게 요청하여 데이터 갯수를 제어하는 방식입니다.
        
        ```java
        public class Example {
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
        
        [main] INFO - # doOnRequest: 1
        [main] INFO - # hookOnNext: 1
        [main] INFO - # doOnRequest: 1
        [main] INFO - # hookOnNext: 2
        [main] INFO - # doOnRequest: 1
        [main] INFO - # hookOnNext: 3
        [main] INFO - # doOnRequest: 1
        [main] INFO - # hookOnNext: 4
        [main] INFO - # doOnRequest: 1
        [main] INFO - # hookOnNext: 5
        [main] INFO - # doOnRequest: 1
        ```
        
        위의 예제는 Reactor에서 Subscriber 인터페이스의 구현 클래스인 BaseSubscriber를 사용하여 데이터 요청 개수를 직접 제어합니다.
        
        숫자 1부터 다섯개의 데이터를 emit하도록 정의하여 BaseSubscriber를 통해 데이터를 1개씩 보내주기를 Publisher에게 요청합니다.
        
        1. subscribe() 메서드의 파라미터로 람다 표현식 대신 구현 객체를 전달합니다.
        2. hookOnSubscribe() 메서드는 Subscriber 인터페이스에 정의된 onSubscribe() 메서드를 대신해 구독 시점에 request()메서드를 호출하여 최초 데이터 요청 개수를 제어합니다.
        3. hookOnNext() 메서드는 onNext() 메서드를 대신해 Publisher가 emit한 데이터를 전달받아 처리한 후, 다시 데이터를 요청하는 역할을 합니다.
        
    - Backpressure 전략사용
        
        Reactor에는 Backpressure를 위한 다양한 전략을 제공합니다.
        
        | 종류 | 설명 |
        | --- | --- |
        | IGNORE 전략 | Backpressure를 적용하지 않는다. |
        | ERROR 전략 | Downstream으로 전달할 데이터가 버퍼에 가득 찰 경우, Exception을 발생시키는 전략 |
        | DROP 전략 | Downstream으로 전달할 데이터가 버버페 가득 찰 경우 버퍼 밖에서 대기하는 먼저 emit된 데이터부터 Drop시키는 전략 |
        | LATEST 전략 | Downstream으로 전달할 데이터가 버버페 가득 찰 경우 버퍼 밖에서 대기하는 가장 최근 emit된 데이터부터 버퍼에 채우는 전략 |
        | BUFFER 전략 | Downstream으로 전달할 데이터가 버버페 가득 찰 경우 버퍼 안에 있는 데이터부터 Drop시키는 전략 |
    - DROP 전략
        
        ![drop.png](https://file.notion.so/f/f/9e132be8-6c00-4f39-b489-0bf5bacbff36/d74cdc7d-76ae-4155-a5b7-761d2fa52c7b/drop.png?id=fc2f0aaa-0f64-4d25-bf15-d2e9002e8d93&table=block&spaceId=9e132be8-6c00-4f39-b489-0bf5bacbff36&expirationTimestamp=1708646400000&signature=xyGgMz0_qG9QPX7n6lnzeJ9AWIdFu7AL7PA-X3_Cfeo&downloadName=drop.png)
        
        1. Step 1에서 Publisher가 emit한 데이터가 버퍼에 채워집니다.
        2. Step 2에서 버퍼가 가득찹니다
        3. 가득차 있는 와 중 데이터가 계속 emit되어, 버퍼 밖에서 데이터들이 대기하는 상황이 발생합니다.
        4. Step 4에서 Downstream에서 데이터 처리가 아직 끝나지 않아 버퍼가 비어 있지 않은 상태이기에 밖에서 대기 중인 먼저 emit된 11, 12,13이 Drop되고 있습니다.
        5. Step 5에서 데이터 처리 이후 버퍼가 비어 있는 상태이기에 14부터 다시 버퍼에 채워집니다.
        6. 11,12,13은 버려지며 14부터 버퍼에 채워집니다.
        
    - DROP 코드
        
        ```java
        @Slf4j
        public class Example {
            public static void main(String[] args) throws InterruptedException {
                Flux
                    .interval(Duration.ofMillis(1L))
                    .onBackpressureDrop(dropped -> log.info("# dropped: {}", dropped))
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
        
        [parallel-1] INFO - # onNext: 0
        [parallel-1] INFO - # onNext: 1
        [parallel-1] INFO - # onNext: 2
        ...
        [parallel-1] INFO - # onNext: 40
        [parallel-1] INFO - # onNext: 41
        [parallel-1] INFO - # onNext: 42
        [parallel-1] INFO - # dropped: 256
        [parallel-1] INFO - # dropped: 257
        [parallel-1] INFO - # dropped: 258
        [parallel-1] INFO - # onNext: 43
        ...
        [parallel-1] INFO - # onNext: 1026
        ```
        
        이 구간에서 Drop이 시작되는 데이터는 256이고 Drop이 끝나는 데이터는 1025입니다.  이 구간 동안은 버퍼가 가득차 있는 상태임을 알 수 있습니다. 이처럼, Drop 전략을 적용하면 버퍼가 가득 찬 상태에서 버퍼가 비워질 때까지 데이터를 DROP 합니다.
        
    - LATEST 전략
        
        ![latest.png](https://file.notion.so/f/f/9e132be8-6c00-4f39-b489-0bf5bacbff36/055bd907-add7-45fc-8250-2a25410ef654/latest.png?id=8bb3f09c-984c-47cc-9aa7-4606b2ad7a58&table=block&spaceId=9e132be8-6c00-4f39-b489-0bf5bacbff36&expirationTimestamp=1708646400000&signature=UtTpyLukqmN5oJ5_u9BZhAzUWa0jqmPJXiv-oKVgZxg&downloadName=latest.png)
        
        LATEST 전략은 전달할 데이터가 버퍼에 가득찰 경우, 버퍼 밖에서 대기 중인 데이터 중에서 가장 최근 emit된 데이터부터 버퍼에 채우는 전략입니다.
        
        DROP과의 차이점은, 버퍼에 가득 찰 경우 버퍼 밖에서 대기중인 데이터가 emit되면 그 즉시 폐기되는 반면, LATEST 전략은 새로운 데이터가 들어온 시점에서 가장 최근의 데이만 남겨두고 전부 데이터를 폐기합니다.
        
        1. Publisher가 emit한 데이터가 버퍼에 채워집니다.
        2. Step3에서 버퍼가 가득 찬 상태에서 데이터가 계속 emit되어 버퍼 밖에 대기합니다.
        3. Step4에서 Downstream에서 데이터가 처리가 끝나서 버퍼를 비운상태 입니다. 버퍼가 비었기에 가장 최근에(나중에) emit된 숫자 17부터 버퍼에 채워지고 나머지 앞서 들어온 데이터들은 폐기됩니다.
        4. 이 때, 나머지 숫자들은 한번에 폐기되는 것이 아닌, 실제론 데이터가 들어올 때마다 이전에 유지하고 있던 데이터가 폐기되는 것입니다.
    
    - LATEST 코드
        
        ```java
        @Slf4j
        public class Example {
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
        
        [parallel-1] INFO - # onNext: 0
        [parallel-1] INFO - # onNext: 1
        [parallel-1] INFO - # onNext: 2
        ...
        [parallel-1] INFO - # onNext: 243
        [parallel-1] INFO - # onNext: 244
        [parallel-1] INFO - # onNext: 245
        [parallel-1] INFO - # onNext: 246
        [parallel-1] INFO - # onNext: 247
        [parallel-1] INFO - # onNext: 248
        [parallel-1] INFO - # onNext: 249
        [parallel-1] INFO - # onNext: 250
        [parallel-1] INFO - # onNext: 251
        [parallel-1] INFO - # onNext: 252
        [parallel-1] INFO - # onNext: 253
        [parallel-1] INFO - # onNext: 254
        [parallel-1] INFO - # onNext: 255
        [parallel-1] INFO - # onNext: 1037
        ```
        
        실행 결과를 보면, 255를 출력하고 바로 1037을 출력합니다 이 말은 즉슨, 버퍼가 비워지는 동안 emit된 데이터 중 가장 최근인 1037이 저장되는 것이고, 그 안에 데이터들은 폐기되는 것입니다.
        
    - BUFFER 전략
        
        컴퓨터 시스템에서 사용되는 버퍼의 일반적 기능은 입출력을 수행하는 장치간 속도 차이를 조절하기 위해 입출력 장치 중간에 위치하여 데이터를 쌓아두었다가 전송하는 것입니다.(버퍼링)
        
        Bakcpressure BUFFER 전략도 이와 비슷합니다. 버퍼의 데이터를 폐기하지 않고 버퍼링을 하는 전략도 지원하며, 버퍼가 가득 차면 버퍼 내의 데이터를 폐기하는 전략, 버퍼가 가득차면 에러를 발생시키는 전략도 지원합니다.
        
        BUFFER - DROP/LATEST 전략은 앞선 전략과 다르게 emit한 대기하는 데이터를 폐기하는 것이 아닌 BUFFER 안쪽에 있는 데이터를 폐기하는 데에 차이가 있습니다.
        
    - BUFFER DROP_LATEST 전략
        
        ![buffer.png](https://file.notion.so/f/f/9e132be8-6c00-4f39-b489-0bf5bacbff36/c829118b-c886-45e7-89c2-1072a01e7fcc/buffer.png?id=b7a599e3-c580-48bd-90bc-22abfd533aaa&table=block&spaceId=9e132be8-6c00-4f39-b489-0bf5bacbff36&expirationTimestamp=1708646400000&signature=w1F7S7w60I1hJJbU09HKCR_LC1wQ5weZTOVRrQrR7wE&downloadName=buffer.png)
        
        이 전략은, 버퍼가 가득 찰 경우 가장 나중에 채워진 데이터를 DROP하여 폐기한 후, 확보된 공간에 emit된 데이터를 채우는 전략입니다.
        
        1. 데이터가 버퍼에 채워지며 버퍼가 가득 찹니다.
        2. 버퍼의 크기가 10이라 가정하면, 11이 들어왔을 떄 버퍼 오버플로가 발생합니다.
        3. 따라서 오버플로를 일으킨 숫자 11이 Drop되어 폐기됩니다.
    
    - BUFFER DROP_LATEST 코드
        
        ```java
        @Slf4j
        public class Example {
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
        
        [main] DEBUG- Using Slf4j logging framework
        [parallel-2] INFO - # emitted by original Flux: 0
        [parallel-2] INFO - [ # emitted by Buffer: 0 ]
        [parallel-2] INFO - # emitted by original Flux: 1
        [parallel-2] INFO - # emitted by original Flux: 2
        [parallel-2] INFO - # emitted by original Flux: 3
        [parallel-2] INFO - ** Overflow & Dropped: 3 **
        [parallel-1] INFO - # onNext: 0
        [parallel-1] INFO - [ # emitted by Buffer: 1 ]
        [parallel-2] INFO - # emitted by original Flux: 4
        [parallel-2] INFO - # emitted by original Flux: 5
        [parallel-2] INFO - ** Overflow & Dropped: 5 **
        [parallel-2] INFO - # emitted by original Flux: 6
        [parallel-2] INFO - ** Overflow & Dropped: 6 **
        [parallel-1] INFO - # onNext: 1
        [parallel-1] INFO - [ # emitted by Buffer: 2 ]
        [parallel-2] INFO - # emitted by original Flux: 7
        ```
        
        onBackpressureBuffer() Operator를 사용하여 BUFFER 전략을 적용했는데, 첫 번째 파라미터는 버퍼의 최대 용량을 나타냅니다. 현재 최대 용량은 2입니다.
        
        두 번째 파라미터는 오버플로가 발생했을 때 Drop되는 데이터를 전달받아 처리할 수 있는 부분입니다.
        
        마지막 세 번째 파라미터는 적용할 Backpressure 전략을 나타냅니다.
        
        0.3초마다 emit되는 데이터가 들어옵니다. 0이 들어와서, 0이 emit 하며 그 처리속도가 1초걸립니다. 그 동안 버퍼는 비어있는 상태인데, 1와 2와 3이 들어옵니다. 하지만 현재 버퍼 상태가 2이기 때문에 3은 오버플로되어 폐기됩니다.
        
        그 동안, 0이 처리되고 1이 emit 되어, 버퍼에 2가 남게 됩니다. 이 때 4가 들어와 꽉차게 되며, 5가 들어오지만 폐기되고, 6이 들어오지만 이 역시 폐기됩니다. 이 후 2가 emit 되어 버퍼에 4, 7이 남게 됩니다.
        
    - BUFFER DROP_OLDEST 전략
        
        ![drop_oldest.png](https://file.notion.so/f/f/9e132be8-6c00-4f39-b489-0bf5bacbff36/f8d3adba-a002-4e9e-8adb-71825a2a1951/drop_oldest.png?id=761ef461-841d-45c8-a9df-7fadcf61db91&table=block&spaceId=9e132be8-6c00-4f39-b489-0bf5bacbff36&expirationTimestamp=1708646400000&signature=pN7Pu95kt50s-bNY2AcWsUOq0o0FTeXZz13G2Haznc8&downloadName=drop_oldest.png)
        
        이 전략은 위의 전략과 반대로 버퍼 안에 채워진 데이터 중에서 가장 오래된 데이터를 Drop하여 폐기한 후, 확보된 공간에 emit된 데이터를 채우는 전략입니다.
        
        1. emit한 데이터가 버퍼에 채워지고 버퍼가 가득차게 됩니다.
        2. 숫자 11이 emit이 되어 버퍼에 채워지지만 오버플로가 발생합니다.
        3. 이 때 11이 지워지는 것이 아닌, 버펑의 가장 앞쪽 숫자인 1이 DROP됩니다.
        
    - BUFFER DROP_OLDEST 코드
        
        ```java
        @Slf4j
        public class Example {
            public static void main(String[] args) throws InterruptedException {
                Flux
                    .interval(Duration.ofMillis(300L))
                    .doOnNext(data -> log.info("# emitted by original Flux: {}", data))
                    .onBackpressureBuffer(2,
                            dropped -> log.info("** Overflow & Dropped: {} **", dropped),
                            BufferOverflowStrategy.DROP_OLDEST)
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
        
        위의 코드와 유사합니다.
        
        하지만 버퍼에서 드랍되는 것이 최근에 들어온 것이 아닌 가장 오래된 데이터가 폐기됩니다. 
        
        Subscriber가 숫자 0을 처리하는 1초 동안 원본 Flux에서 0.3초에 한번 숫자 1, 2를 emit합니다. 그리고 버퍼에 1,2가 채워집니다.
        
        이 후 숫자 3이 emit되는데 3이 버퍼에 채워지는 순간 오버플로가 발생하며 가장 오래된 1이 폐기됩니다. 이 시점에서 버퍼는 2, 3이 남게 됩니다. 
        
        이 후 숫자 2가 emit되고 버퍼 상태는 3이 됩니다. 그리고 버퍼에 4가 채워 3, 4가 됩니다. 하지만 5가 채워지는 순간 버퍼가 꽉 차 3이 버려져 버퍼가 4,5가 됩니다 그리고 6을 emit하여 가장 오래된 4가 폐기되어 5, 6이 남게됩니다. 그리고 5가 emit되면 버퍼에는 6이 남으며 7을 emit하여 버퍼에 채워져 버퍼에 6, 7이 남게 됩니다.
        
    
- 각 전략을 구체적으로 사용하는 사례
    - DROP 전략
        
        실시간 비디오 스트리밍 서비스 : 사용자가 실시간으로 비디오를 시청하는 경우, 네트워크 상태가 좋지 않거나 서버가 과부화 상태인 경우 drop 전략을 사용하여 일부 프레임을 버리고 끊김 없이 최신 데이터를 전달합니다. 이로써 사용자는 약간의 화질 저하를 겪더라도 지연없이 스트리밍을 이용합니다.
        
    - LATEST 전략
        
        주식 시장 데이터 스트리밍 : 주식 시장에서 실시간으로 발생하는 거래 데이터가 중요합니다. 하지만 일부 데이터 손실은 큰 문제가 됩니다. 이를 해결하기 위해 최신 거래 정보만 유지하고 이전 거래 정보를 무시함으로써, 네트워크 지연이나 처리 지연으로 인한 데이터 손실을 최소화 합니다.
        
    - BUFFER 전략
        
        대규모 데이터 처리 시스템 : 대규모 데이터 처리 시스템에서 데이터 처리속도와 데이터 입력 속도가 일치하지 않는 경우가 발생합니다. 이때 Buffer 전략을 사용하여 데이터를 일시적으로 저장하고, 데이터 소비자가 처리할 준비가 될 때까지 대기할 수 있습니다. 그 예로, 대규모 로그 처리 시스템에서 로그 메시지를 버퍼에 저장하여 처리할 수 있는 리소스가 사용가능할 때까지 기다립니다.