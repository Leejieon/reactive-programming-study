# 10. Scheduler

### Reactor의 스케줄러

스케줄러란, 비동기 프로그래밍을 위해 사용되는 스레드를 관리해주는 역할.

멀티스레드 환경에서 스레드간 경쟁조건등의 이유로 코드의 복잡도 및 위험성이 증가한다.

Reactor에서는 스케줄러가 스레드제어를 직접 해주기때문에 개발자가 직접 스레드를 제어해야 하는 부담이 적다.



- **SubscribeOn()** : 구독이 발생한 직후 실행될 스레드를 지정한다.

- **PublishOn()** : PublishOn을 기준으로 아래쪽인 DownStream의 실행 쓰레드를 변경한다. 

![image](https://github.com/Leejieon/reactive-programming-study/assets/62167266/f4edb6ff-2191-477d-b751-cce57eff64e7)

기존의 방식과 마찬가지로 스레드를 사용하지 않는다. (main) 에서 실행



![image](https://github.com/bjpublic/Spring-Reactive/assets/87458171/02ab08f9-ea4d-42ce-8db5-99c2e27d14a1)

PubishOn을 두번사용할 경우 동작과정이다. 서로 각각 다른스레드에서 실행된다.

![image](https://github.com/bjpublic/Spring-Reactive/assets/87458171/b5020b0d-6aca-4a5f-ad0e-cb94114a743f)

SubscribeOn() 과 PublishOn()을 함께 사용할 경우 동작이다.

 SubscribeOn은 구독이 발생한 직후부터 실행쓰레드를 지정하기때문에 

이때 FromArray () Operator까지는 A쓰레드에서 실행된다.

하지만 PublishOn부터는 B쓰레드에서 실행된다.



### Scheduler의 종류

- **Schedulers.immediate()**

  현재 쓰레드에서 작업을 처리한다.

- **Schedulers.single()**

​	doTask를 여러번 호출하더라도 하나의 스레드에서만 처리한다.

- **Schedulers.newSingle()**

​	하나의 스레드를 재사용하는 반면 매번 새로운 스레드를 생성한다.

- **Schedulers.boundedElastic()**

​	스레드 풀을 생성 후 그안에서 정해진 수만큼의 스레드를 사용해 작업을 처리하고 작업이 종료된 스레드는 반	납하여 재사용한다. Blocking I/O에 최적화되어있다.

- **Scheduleres.parallel()**

​	CPU코어수만큼의 스레드를 생성한다. Non blocking I/O에 최적화되어있다.

- **Schedulers.fromExecutorService()**

​	기존에 이미 사용하고 있는 ExecutorService가 있다면 이것으로부터 Scheduler를 생성한다.

- Schedulers.newXXXX() : 커스텀 스케줄러