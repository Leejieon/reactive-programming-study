# 리액티브 스트림즈(Reactive Streams)

## 리액티브 스트림즈란?

리액티브한 코드 작성과 구성을 용이하게 해 주는 리액티브 라이브러리를 어떻게 구현할지 정의해 놓은 **표준 사양**을 리액티브 스트림즈라고 합니다. 

한마디로,

> 데이터 스트림을 Non-Blocking이면서 비동기적인 방식으로 처리하기 위한 리액티브 라이브러리의 표준 사양

이라고 표현할 수 있습니다. 

이를 구현한 구현체에는 RxJava, Reactor, Akka .. 등 다양한 것들이 있습니다.

## 리액티브 스트림즈 구성요소

리액티브 스트림즈를 통해 구현해야 하는 API 컴포넌트에는 Publisher, Subscriber, Subscription, Processor 가 있습니다. 

| 컴포넌트 | 설명 |
| --- | --- |
| Publisher | 데이터를 생성하고 통지(발행)하는 역할 |
| Subscriber | 구독한 Publisher로부터 통지(발행)된 데이터를 전달받아서 처리하는 역할 |
| Subscription | Publisher에 요청할 데이터의 개수를 지정하고, 데이터의 구독을 취소하는 역할 |
| Processor | Publisher와 Subscriber의 기능을 모두 가진다. Publisher로서 다른 Subscriber가 구독할 수 있고, Subscriber로서 다른 Publisher를 구독할 수 있다.  |

![리액티브 스트림즈](https://blog.kakaocdn.net/dn/bMwMIT/btsEn0OlOMO/KeHcKdufRVwv3HxT4zjNd0/img.png)

위 그림은 **Publisher**와 **Subscriber** 간에 데이터가 전달되는 동작 과정을 그림으로 표현한 것입니다.

1.  먼저 Subscriber는 전달받을 데이터를 구독합니다(`subscribe`).
2.  Publisher는 데이터를 발행(통지)할 준비가 되었음을 Subscriber에 알립니다(`onSubcribe`).
3.  이 알림을 받은 Subscriber는 전달받기 원하는 데이터의 개수를 Publisher에게 요청합니다.(`Subscription.request`).
4.  그 다음 Publisher는 Subscriber로부터 요청받은 만큼의 데이터를 발행합니다(`onNext`).
5.  위의 과정을 반복하다가 Publisher가 모든 데이터를 통지하게 되면 마지막으로 데이터 전송이 완료되었음을 Subscriber에게 알립니다(`onComplete`). 만약 Publisher가 데이터를 처리하는 과정에서 에러가 발생하면 Subscriber에게 에러가 발생했음을 알립니다(`onError`).

여기서 의문점은 왜 굳이 3번 과정을 통해 데이터의 요청 개수를 지정하는 것일까요?

위 그림에서 보이는 것과는 다르게 Publisher와 Subscriber는 각각 **다른 스레드에서 비동기적으로 상호작용**하는 경우가 대부분입니다. 이 경우, Publisher가 발행(통지)하는 속도가 Publisher로부터 발행된 데이터를 Subscriber가 처리하는 속도보다 더 빠르면 _처리를 기다리는 데이터가 쌓여 시스템 부하_ 를 불러올 수 있습니다. 이와 같은 문제를 해결하기 위해 데이터 개수를 제어하는 것입니다. 

## 리액티브 스트림즈 컴포넌트

리액티브 스트림즈의 컴포넌트는 실제 코드에서는 **인터페이스(Interface)** 형태로 정의되고, 이를 구현해서 해당 컴포넌트를 사용하는 방식입니다. 

### Publisher

```java
public interface Publisher<T> {
  public void subscribe(Subscriber<? super T> s);
}
```

위 코드는 Publisher 인터페이스 코드 입니다. 

`subscribe` 메서드는 파리미터로 전달받은 **Subscriber**를 등록하는 역할을 합니다. 

### Subscriber

```java
public interface Subscriber<T> {
  public void onSubscribe(Subscription s);
  public void onNext(T t);
  public void onError(Throwable t);
  public void onComplete();
}
```

위 코드는 Subscriber 인터페이스 코드로 각 메서드는 다음과 같습니다.

-   `onSubscribe` := Publisher에게 요청할 데이터의 개수를 지정 or 구독을 해지 → Subscription 객체를 통해 이루어집니다.
-   `onNext` := Publisher가 발행한 데이터를 처리하는 역할
-   `onError` := Publisher가 데이터 발행을 위한 처리 과정에서 에러가 발생했을 때, 해당 에러를 처리하는 역할
-   `onComplete` := Publisher가 데이터 발행을 완료했음을 알릴 때 호출되는 메서드. 데이터 발행이 정상적으로 완료될 경우에 특정 후처리를 해야 한다면 이 메서드에서 처리 코드를 작성하면 됩니다. 

### Subscription

```java
public interface Subscription {
  public void request(long n);
  public void cancel();
}
```

위 코드는 Subscription 인터페이스 코드입니다.

-   `request` := Publisher에게 데이터의 개수를 요청
-   `cancel` := 구독을 해지

위 그림의 동작 과정을 컴포넌트 코드 과정에서 다시 설명하면 다음과 같습니다.

1.  Publisher가 Subscriber 인터페이스 구현 객체를 `subscribe` 메서드의 파라미터로 전달
2.  전달 받은 Subscriber 구현 객체의 `onSubscribe` 메서드를 호출하면서 구독을 의미하는 Subscription 구현 객체를 Subscriber에게 전달
3.  호출된 `onSubscribe` 메서드에서 전달 받은 Subscription 객체를 통해 전달 받을 데이터의 개수를 Publisher에게 요청
4.  이 요청 개수만큼 데이터를 Publisher가 `onNext` 메서드를 호출해 Subscriber에게 전달
5.  Publisher는 통지할 데이터가 더 이상 없을 경우 `onComplete` 메서드를 호출해 Subscriber에게 데이터 처리 종료 알림

### Processor

```java
public interface Processor<T, R> extends Subscriber<T>, Publisher<R> {
}
```

Processor는 Subscriber와 Publisher 인터페이스를 상속하고 있고, 별도로 구현해야 하는 메서드가 없습니다. 이 둘의 기능을 모두 가지고 있기 때문입니다. 

## 리액티브 스트림즈 관련 용어

#### Signal

> Publisher와 Subscriber 간에 주고받는 상호작용

위에서 살펴본 `onSubscribe`, `onNext` ... 와 같은 메서드들을 리액티브 스트림즈에서는 **Signal**이라고 표현합니다. 

#### Demand

> Subscriber가 Publisher에게 요청하는 (아직 전달받지 않은) 데이터

#### Emit

> Publisher가 Subscriber에게 데이터를 통지(발행)하는 것

_데이터를 emit한다._ := _데이터를 전달하기 위한 `onNext` Signal_

#### Sequence

> Publisher가 emit하는 데이터의 연속적인 흐름

**Operator 체인** 형태로 정의됩니다. 즉, 다양한 Operator로 데이터의 연속적인 흐름을 정의한 것.

#### Operator

> just, filter, map 등과 같은 연산자

#### Source

> 최초에 가장 먼저 생성된 무언가 := 원본

---

#### 참고

"스프링으로 시작하는 리액티브 프로그래밍"
