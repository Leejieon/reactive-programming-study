# Blocking I/O와 Non-Blocking I/O

## I/O

I/O란 간단히 얘기하면

> 데이터의 입력(Input)과 출력(Output)

을 일컫는 말입니다. 단순히 파일 I/O를 넘어서, 데이터를 조회하거나 추가하는 DB I/O, 네트워크 통신에서 네트워크 I/O 등이 포함됩니다. 

I/O는 어플리케이션의 성능에 가장 영향을 많이 미칩니다. 따라서, 높은 성능을 보장해야 하는 어플리케이션 입장에서 I/O는 큰 장애물이 될 수 있습니다. 

이 I/O의 두 가지 방식인 Blocking I/O와 Non-Blocking I/O에 대해 알아봅시다.

## Blocking I/O

Blocking I/O란,

> I/O 작업이 진행되는 동아 유저 프로세스가 자신의 작업을 중단한채 I/O가 끝날 때까지 대기하는 방식

을 의미합니다. 즉, 하나의 스레드가 I/O에 의해서 차단되어 대기하는 것을 말합니다. 

![Blocking I/O](https://blog.kakaocdn.net/dn/59bq0/btsEx62a4v4/LqOwWtDUjkcrp1tK23YkF1/img.png)

위 그림은 Blocking I/O를 설명하기 위한 간단한 예시입니다.

한 대의 클라언트 PC와 도서 데이터를 제공하는 두 API 서버가 있습니다. 클라이언트에서 본사 API 서버에 도서 정보 요청을 보내면 본사 API 서버는 지점 API 서버에 추가적인 요청을 보내고 지점 API 서버에서 응답을 받아 클라이언트에 전달하게 됩니다. 이때, 본사 API 서버에서 지점 API 서버로 요청을 보내는 그 시점에 본사 API 서버에서 실행된 요청 스레드는 차단되어 지점 API 서버의 작업 스레드가 응답을 반환하기 전까지 대기하게 됩니다. 

물론, 이를 보완하기 위해 추가 스레드를 할당하는 멀티스레딩 기법을 사용할 수 있지만, 이 경우 **컨텍스트 스위칭(Context Switching)** 으로 인한 스레드 전환 비용이 발생합니다. 

## Non-Blocking I/O

Non-Blocking I/O는 Blocking I/O와 반대로 스레드가 차단되지 않습니다. 즉,A 함수가 I/O 작업을 호출했을 때 I/O 작업이 완료될 때까지 A 함수의 작업을 중단하지 않고 I/O 호출에 대해 즉시 리턴합니다. 이때, A 함수가 이어서 다른 일을 수행할 수 있도록 하는 방식입니다. 

![Non-Blocking I/O](https://blog.kakaocdn.net/dn/nor0I/btsEA2jXvxi/Py0sZ3f1n5JsuYoR4GkIck/img.png)

위 그림은 Non-Blocking I/O를 설명하기 위한 예시입니다. Blocking I/O 예시와 다른 점은 하나의 지점 API 서버가 아니라 두 개의 API 서버로 구성되어 있습니다. 만약 Blocking I/O 방식이라면 A 지점 API 서버로의 요청을 처리하는 동안 스레드가 차단되어 B 지점 API 서버로 요청을 보내지 못할 것입니다. 

 하지만, 위 그림의 경우 A 지점 API 서버로의 요청을 처리하는 동안 **스레드가 차단되지 않기 때문에** 대기 없이 B 지점 API 서버로 요청을 _즉시_ 보낼 수 있습니다. 

이처럼 Non-Blocking I/O 방식은

> 작업 스레드의 종료 여부와 관계없이 요청한 스레드는 차단되지 않는다.

라고 할 수 있습니다.
