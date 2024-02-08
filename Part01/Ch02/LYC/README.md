# 리액티브 스트림즈

- 리액티브 스트림즈
    - 구성요소
    
    | 컴포넌트 | 설명 |
    | --- | --- |
    | Publisher | 데이터를 생성하고 통지(발행, 게시, 방출)하는 역할을 한다. |
    | Subscriber | 구독한 Publisher로부터 통지된 데이터를 전달받아 처리한다. |
    | Subscription | Publisher와 Subscriber의 연결을 나타내며, Publisher에 요청할 데이터의 개수를 지정하고, 데이터의 구독을 취소하는 역할을 한다. |
    | Processor | Publisher, Subscriber의 기능을 모두 가지고 있다. 즉 Subscriber로서 다른 Publisher를 구독할 수 있고, Publisher로서 다른 Subscriber가  구독할 수 있다. 주로 첫 발행자와 최종 구독자 사이에서 로직을 처리하기 위해 사용된다. |

- Publisher 와 Subscriber
    
    리액티브 스트리에서 Publisher는 연속된 데이터들을 정보를 요청한 Subscriber에게 제공합니다.
    
    ⭐ Publisher와 Subscriber의 동작 관계
    
    ![다운로드.png](%E1%84%85%E1%85%B5%E1%84%8B%E1%85%A2%E1%86%A8%E1%84%90%E1%85%B5%E1%84%87%E1%85%B3%20%E1%84%89%E1%85%B3%E1%84%90%E1%85%B3%E1%84%85%E1%85%B5%E1%86%B7%E1%84%8C%E1%85%B3%20b34178ce8d7342ffa4a1c546ccafdb6e/%25E1%2584%2583%25E1%2585%25A1%25E1%2584%258B%25E1%2585%25AE%25E1%2586%25AB%25E1%2584%2585%25E1%2585%25A9%25E1%2584%2583%25E1%2585%25B3.png)
    
    1. **Publisher**(생산자)가 **Subscriber**(소비자)를 **subscribe**(등록)합니다.
    2. 동시에 **Subscriber**(소비자)가 **Subscription**(전달자)을 **onSubscribe**(등록)합니다.
    3. **Subscriber**(소비자)는 필요할 때 **Subscribe**(전달자).**request**(요청)을 통해 **Publisher**에게 데이터를 요청합니다.
    4. **Publisher**(생산자)는 요청을 받으면 **생성한 데이터를 보냅니다.**
    5. **Subscriber**는 `onNext`로 데이터를 받습니다.
    6. 모든 요청이 성공적으로 완료되면 `onComplete`을 호출하고 흐름을 종료합니다.
    7. 요청이 실패하면 `onError`를 호출하고 흐름을 종료합니다.
    
    ⭐여기서, Pubilsher는 왜 subscribe라는 메소드를 본인이 들고 있을까요?
    
     Publisher는 subscriber와 다르게, 구독을 한 subscriber가 누구인지 전부알고 있을 필요가 있습니다. 그 이유는, 데이터를 전파함에 있어 구독을 요청한 사람에게 제공을 해야하기 때문입니다. 따라서 Pubilsher는 subscribe라는 메소드를 통해 이벤트를 전파할 대상을 알 수 있습니다.
    
    쉽게 그 예를 코드로 살펴 보겠습니다.
    
    ```java
    import java.util.function.Consumer;
    
    public class Main {
        public static void main(String[] args) {
            // Publisher 객체 생성
            Publisher<Integer> publisher = new Publisher<>();
    
            // Subscriber 객체 생성
            Subscriber<Integer> subscriber1 = new Subscriber<>(data -> {
                if (data % 2 == 0) {
                    System.out.println("Even number received by Subscriber 1: " + data);
                }
            });
    
            Subscriber<Integer> subscriber2 = new Subscriber<>(data -> {
                if (data % 2 != 0) {
                    System.out.println("Odd number received by Subscriber 2: " + data);
                }
            });
    
            // Publisher에 Subscriber 등록
            publisher.subscribe(subscriber1);
            publisher.subscribe(subscriber2);
    
            // 이벤트 발행
            for (int i = 1; i <= 10; i++) {
                publisher.publish(i);
            }
        }
    
    //실행결과
    Odd number received by Subscriber 2: 1
    Even number received by Subscriber 1: 2
    Odd number received by Subscriber 2: 3
    Even number received by Subscriber 1: 4
    Odd number received by Subscriber 2: 5
    Even number received by Subscriber 1: 6
    Odd number received by Subscriber 2: 7
    Even number received by Subscriber 1: 8
    Odd number received by Subscriber 2: 9
    Even number received by Subscriber 1: 10
    ```
    
    위의 예제는, 간단한 숫자 스트림을 생성하고, 이를 두 개의 구독자에게 전달하는 과정을 보여줍니다.
    
    Publisher 클래스 : 객체를 생성한 뒤, subscribe라는 메소드를 통해 구독받을 Subscriber를 등록하고, publish라는 메소드를 통해 이벤트를 발생시킵니다.
    
    Subscriber 클래스 : 이벤트를 받은 데이터를 처리하는데, 이 때 각각 홀수인지 짝수인지에 따라 다르게 처리합니다. 이를 통해 Subscriber가 Publisher를 통해 받은 데이터를 각각 독립적으로 처리할 수 있음을 보여줍니다.