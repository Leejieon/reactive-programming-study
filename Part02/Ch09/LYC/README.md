# Sinks

- Sinks 란?
    
    리액티브 스트림즈에서 Processor는 Publisher와 Subscriber의 기능을 모두 지니고 있어, 다른 Publisher를 구독하거나 다른 Subscriber가 구독할 수 있는 것을 의미합니다.
    
    Sinks는 Reactor 3.4.0 버전부터 지원한 Processor의 기능을 개선한 것으로 리액티브 스트림즈의 Signal을 프로그래밍 방식으로 푸시할 수 있는 구조이며 Flux 또는 Mono의 의미 체계를 가진다고 설명합니다.
    
- 예제
    
    ```java
    @Slf4j
    public class Example {
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
    
    위의 코드는, create() Operator를 사용해 프로그래밍 방식으로 Signal을 보내는 코드입니다. 총 5개의 작업을 수행한 후 이를 Subscriber에게 전달합니다.
    
    1. 먼저 create() operator가 처리해야 할 작업의 개수만큼 doTask() 메서드를 호출하여 작업을 처리한 후 결과를 리턴받습니다.
    2. 이 처리 결과를 map() Operator를 사용해서 추가적으로 가공처리한 후 최종적으로 Subscriber에게 전달합니다. 이 때 작업을 처리하는 단계, 처리 결과를 가공하는 단계, 가공된 결과를 전달하는 단계를 모두 각각 다른 스레드에서 실행하도록 작성하였습니다.
    3. 작업을 처리하는 스레드는 subscribeOn()에서 지정하고,
        
        처리 결과를 가공하는 스레드는 publishOn() Operator에서 지정하며, 가공된 결과를 두 번째 publishOn()에서 지정합니다.
        
    
    이처럼 Reactor Sequence에서 단계적으로 나누어 여러 개의 스레드를 처리할 수 있습니다. 하지만 작업을 처리한 후 그 결과를 반환하는 doTask()메서드가 싱글 스레드가 아닌 여러 개의 스레드를 각각의 전혀 다른 작업을 처리한 후, 처리 결과를 반환하는 상황이 생길기는데, 이를  적절하게 사용하는 것이 Sinks입니다.
    
    Sinks 방식을 사용하면 멀티 스레드 환경에서 스레드 안정성을 보장받을 수 있습니다.
    
    스레드 안정성이란 함수나 변수 같은 공유 자원에 동시 접근할 경우 프로그램 실행에 문제가 없음을 의미합니다. 공유 변수에 동시에 접근해서 올바르지 않은 값이 할상되거나, 교착 상태에 빠지게 되면 스레드 안정성이 깨지게 됩니다. Sinks 같은 경우 동시 접근을 감지하고, 동시 접근하는 스레드 중 하나가 빠르게 실패함으로써 스레드 안정성을 보장합니다.
    
- SInks 종류 및 특징
    
    Reactor에서 Sinks를 사용하여 Signal을 보내는 방법은 크게 두 가지입니다. 첫째는 Sinks.One을 사용하는 것이고 둘째는 Sinks.many를 사용하는 것입니다.
    
    - Sinks.One
        
        한 건의 데이터를 전송하는 방법을 정의해 둔 기능 명세라고 말할 수 있습니다.
        
        ```java
        public final class Sinks {
        
        	public static <T> Sinks.One<T> one() {
        						return SinksSpecs.DEFAULT_ROOT_SPEC.one();
        	}
        }
        ```
        
        다음 코드는 Sinks.one() 메서드 내부 코드 구성입니다. 한 건의 데이터를 프로그래밍 방식으로 emit하는 역할을 하기도 하고, Mono 방식으로 Subscriber가 데이터를 소비할 수 있도록 해주는 Sinks 클래스 내부에 정의된 것 입니다.
        
        즉, 한 건의 데이터를 프로그래밍 방식으로 emit 하는 기능을 사용하고 싶으니 거기에 맞는 적당한 기능을 달라고 요청하는 것과 같습니다.
        
        ```java
        @Slf4j
        public class Example {
            public static void main(String[] args) throws InterruptedException {
        		    Sinks.One<String> sinkOne = Sinks.one();
        				Mono<String> mono = sinkOne.asMono();
        
        				sinkOne.emitValue("Hello Reactor", FAIL_FAST);  
        				sinkOne.emitValue("HI Reactor", FAIL_FAST);   
        	 
        				mono.subscribe(data->log.info("# Subscriber1 ", data));
        				mono.subscribe(data->log.info("# Subscriber2 ", data));
        
            }
        }
        
        ```
        
        위의 코드는 Sinks.one() 메소드를 사용한 예제입니다. 이를 호출하면 [Sinks.One](http://Sinks.One) 이라는 기능 명세를 리턴하며 객체로 데이터를 emit할 수 있습니다. 실행하면 “HI Reacotr”는 실행이 되지 않습니다. 그 이유는 Sinks.One으로 아무리 많은 수의 데이터를 emit 한다 하더라도 처음 emit한 데이터는 정상적으로 emit 되지만 나머지는 데이터들은 Drop된다는 사실을 알 수 있습니다.
        
    - Sinks.Many
        
        Sinks.many() 메서드를 사용하여 여러 건의 데이터를 여러 방법으로 전송하는 기능을 정의해둔 명세라고 볼 수 있습니다.
        
        ```java
        public final class Sinks {
        		
        		public static ManySpec many() {
        				return SinksSpecs.DEFAULT_ROOT_SPEC.many();
        		}
        }
        ```
        
        many() 메서드의 경우 Sinks.Many를 리턴하지 않고 ManySpec이라는 인터페이스를 리턴합니다. 그 이유는 데이터를 emit을 위한 여러 가지 기능이 존재하기에 ManySpec을 리턴합니다.
        
        ManySpec은 총 가 가지 기능을 정의합니다.
        
        - UnicastSpec : 한 명에게 여러가지 전송
        - MulticastSpec : 여러명에게 여러가지 전송
        - MulticastReplaySpec : 여러명에게 전송하는데, limit을 걸어서 전에 구독하기전 limit 갯수만큼 나중에 emit된 것만 전송