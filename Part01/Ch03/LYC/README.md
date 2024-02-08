# Block I/O and Non Block I/O

- Blcok IO 와 Non-Block IO
    - **Blocking I/O**
    
    ![image.png](Block%20I%20O%20and%20Non%20Block%20I%20O%20206e652a156e444bbbe611037db9c511/image.png)
    
    클라이언트와 메인서버1과 서브서버 2,3이 있습니다. 클라이언트에서 메인서버1에 데이터를 요청하면, 메인서버1은 서브서버 2와3에 요청을 보냅니다.
    
    이 때 서버 1에서 2로 요청을 보내는 시점에서 실행되는 스레드는 차단되어 서버 2의 스레드가 끝나 응답을 반환하기 전까지 대기합니다. 여기서 차단되었다는 말은 일반적으로 스레드가 어떤 이유로 작업을 계속 진행할 수 없게 되는 상황을 의미합니다. 주로, 위와 같은 상황인 특정 작업을 완료하기 위해 대기하거나 다른 작업을 기다리는 동안 발생합니다.
    
    서버 2의 응답을 반환받은 서버1은 다시 차단된 스레드를 실행하여 서버 3에게 데이터를 요청하고 다시 서버 1의 스레드는 차단이 됩니다.
    
    이 처럼, 스레드가 다른 특정 작업을 대기하며, 차단되어 있는 것을 Bolck I/O라고 합니다.
    
    이를 해결하기 위해, 멀티스레딩 기법을 통해, 추가 스레드를 할당하는 방식이 있지만, 이 역시 문맥교환(context switching등의 문제로, 성능저하 문제가 발생할 수 있습니다.
    
    또한 많은 양의 트래픽이 주어지면, 그 만큼의 새로운 스레드가 실행되기 위해 스택 영역에 할당이 됩니다. 따라서 메모리 사용량 증가로 오버헤드가 발생할 수 있습니다.
    
    - **Non-Blocking I/O**
        
        ![image (1).png](Block%20I%20O%20and%20Non%20Block%20I%20O%20206e652a156e444bbbe611037db9c511/image_(1).png)
        
        위의 사진 처럼 Block I/O와 반대로 스레드가 차단되지 않습니다. 
        
        가장 중요한 점은 요청을 보내고 스레드가 차단되지 않는 점입니다.
        
        메인 서버1에서 서버2로 요청을 처리하는 동안 스레드가 차단되지 않아 바로 서버3으로 요청을 보낼 수 있습니다. 스레드가 차단되지 않기 때문에 멀티스레드와 달리 하나의 스레드로 많은 수의 요청을 처리할 수 있다는 장점을 가지고 있습니다.
        
    
    - Non - Blocking I/O 방식의 통신이 적합한 시스템
        1. 대량의 요청 트래픽이 발생하는 시스템
            
            대량의 요청 트래픽이 발생하면, Block I/O 방식에서 구현하는 데에 한계가 있습니다. 따라서 Spring WebFlux 기반 애플리케이션으로 전환을 할 수 있습니다.
            
            ex) 웹 서버, 클라우드 서비스, 게임 서버, 소셜 미디어 플랫폼
            
        2. 마이크로서비스 기반 시스템
            
            마이크로서비스 기반 시스템은 서비스들 간의 통신이 많기에, 많은 수의 I/O가 발생합니다. 따라서 Block 형식으로 통신할 경우 트래픽 초과로 다른 서비스에도 응답 지연시켜 전체 서비스에 영향을 끼칩니다. 이를 해결하기 위해 Non - Blocking I/O를 사용합니다.
            
            ex) 금융 서비스, 저자 상거래 플랫폼, 여행 및 호텔 플랫폼, IoT 플랫폼 등
            
            ⭐ MSA와 비동기 메시지 기반 통신이 잘 맞는 이유
            
            메시지 기반 통신은 서비스 간의 강력한 결합을 방지하고, 서비스 간의 상호작용을 느슨하게 만듭니다. 각 서비스는 메시지를 전송하기만 하면 되기에 의존성이 낮아져, 변경이나 확장이 용이해집니다.
            
            예를 들어,  주문과 배송 시스템을 살펴 볼 수 있습니다.
            
            ![무제.png](Block%20I%20O%20and%20Non%20Block%20I%20O%20206e652a156e444bbbe611037db9c511/%25E1%2584%2586%25E1%2585%25AE%25E1%2584%258C%25E1%2585%25A6.png)
            
            만약, 주문과 배송 시스템이 서로 밀접하게 연결되어 있으면, 한 시스템의 변경이 다른 시스템에 영향을 미칠 수 있습니다. 예를 들어, 주문 시스템이 바로 배송 시스템에 배송 정보를 전달하고, 배송이 완료 되면 주문 시스템이 이를 업데이트 하게 되는 경우를  들 수 있습니다.
            
            이 경우, 배송 시스템의 장애가 발생하면 주문 시스템에 주문 처리가 지연되거나 실패하게 되어 주문 시스템에 영향을 줄 수 있습니다. 또는 주문 시스템의 로직 변경으로 배송 시스템을 수정해야 하는 상황도 있을 수 있습니다.
            
            메시지 기반의 리액티브 시스템 같은 경우 비동기적이고 느슨하게 결합되어 이러한 결합도를 낮출 수 있습니다. 주문은 주문 처리에 집중하고, 배송은 배송에만 집중하여 서로 독립적인 관계를 유지할 수 있습니다. 이를 통해 다른 시스템에 미치는 영향을 최소화하게 됩니다.
            
        3. 스트리밍 또는 실시간 시스템
            
            스트리밍 또는 실시간 시스템은 실시간으로 대량의 데이터 들이 들어오기 때문에, 이를 효율적으로 처리하기 위해 비동기 통신을 이용한다.
            
    - Block I/O vs Non Block I/O
        
        ```java
        blocking/non-blocking은 호출되는 함수가 바로 리턴하느냐 마느냐가 관심사이다.
        blocking: 바로 리턴 하지 않는다.
        non-blocking: 바로 리턴 한다.
        ```
        
    - sync(동기) vs async(비동기)
        
        ```java
        sync/async는 호출되는 함수의 작업 완료 여부를 누가 신경쓰냐가 관심사
        동기(sync): 호출되는 함수의 작업 완료 여부를, 호출 하는 함수가 신경씀.
        비동기(async): 호출되는 함수의 작업 완료 여부를, 호출 되는 함수가 신경씀.
        #
        ```