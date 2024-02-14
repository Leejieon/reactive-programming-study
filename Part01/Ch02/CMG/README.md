# Reactive Programming CH1 ~ CH3

## 1. Reactive Programming

#### Reactive System

**빠른응답성**을 위한 시스템 디자인 : 클라이언트의 요청에 즉각적으로 응답함으로써 지연시간을 최소화 하기위한 백엔드 디자인 + 유지보수 및 확장 용이



#### Reactive Programming

리액티브 시스템 구축에 필요한 프로그래밍 모델



## 2. Reactive Streams

#### 1) Reactive Streams 

Reactive Streams 란 Reactive코드를 작성하기위한 표준사양을 정해놓은 라이브러리.

이를 구현하기 위한 구현체로 Spring의 경우 Reactor를 사용



#### 2) 구성요소

1) **Publisher** : 데이터를 생성하고 통지
2) **Subscriber** : 구독한 Publisher로부터 통지된 데이터를 전달받아 처리
3) **Subscription** : Publisher에 요청할 데이터의 개수를 지정하고 데이터의 구독을 취소
4) **Processor** : Publisher와 Subscriber의 기능을 모두 가짐



```java
/* publisher interface */
public interface Publisher<t>{
    public void subscribe(Subscriber<? super T> s);
}
```



- Publisher 가 데이터를 생성하고 통지 ->  ? 일종의 핸들러 등록과정인것 같음



```java
/* subscriber interface */
public interface Subscriber<t>{
    public void onSubscribe(Subscription s);
    public void onNext(T t);
    public void onError(Throwable t);
	public void onComplete();    
}
```



- **onSubscribe** : 구독시작시점에 요청할 데이터 개수지정 혹은 구독 해지등의 처리
- **onNext** : Publisher가 통지한 데이터 처리
- **onError** : 데이터 통지를 위한 처리과정에서 에러처리
- **onComplete** : 데이터 통지 완료 통지
- 위 메서드들은 일종의 *Signal*로 간주됨



```java
/* subscription interface */
public interface Subscriber<t>{
	public void request(long n);
    public void cancel();
}
```

- Subscription 인터페이스에는 실질적인 데이터 요청 및 취소 메서드에 대한 구현부가 존재한다



```java
/* Processor interface */
public interface Processor<T, R> extends Subscriber<T>, Publisher<R>{
}
```

프로세서의 경우 Subscriber와 Publisher를 모두 상속받음



#### 3) 리액티브 스트림즈의 구현 규칙

- 구현 규칙 메모

![image](https://github.com/bjpublic/Spring-Reactive/assets/62167266/b247f3ef-60d5-464b-9926-8e1793b0a23c)

![image](https://github.com/bjpublic/Spring-Reactive/assets/62167266/ea08e45b-5824-4670-a3d7-d1760f2c9134)



![image](https://github.com/bjpublic/Spring-Reactive/assets/62167266/d34ce047-c1f9-4604-9e38-40078dd68302)







## Blocking I/O 와 Non-Blocking I/O



웹 어플리케이션 측면에서  I/O란 파일읽기를 위한 DB I/O, 다른 웹 어플리케이션과의 통신을 위한 네트워크 I/O등이 있음

(아파치 톰캣 서블릿 컨테이너 동적인 데이터 처리 <- 뭔말?)



웹에서 Blocking I/O란 위와같은 I/O 처리에 있어 하나의 요청에 대해 하나의 쓰레드가 처리할때까지 커널이 점유 -> User thread block.

멀티쓰레드 기반의 스케줄링 방식은 이러한 Blocking I/O를 해결하기위한 방식.

하지만 자바 기반 웹어플리케이션에서 request당 쓰레드 하나를 할당할 경우 매우 많은 메모리가 요구되거나, 자바의 쓰레드풀 기반의 시스템에서 유휴 쓰레드가 없을 경우 응답 지연이 발생한다.



Non Blocking방식의 경우 하나의 쓰레드로 많은 요청을 처리할 수 있다.

CPU소모가 많은 어플리케이션의 경우에는 좋지 않을 수도 있음





서비스들간의 많은 I/O가 발생하거나, 스트리밍, 또는 실시간 시스템의 경우 끊임없이 들어오는 데이터에 대해 효율적으로 처리하기 위해  Non Blocking 방식을 지원하는 반응형 시스템인 Spring WebFlux기반의 어플리케이션이 필요한 이유이다.

