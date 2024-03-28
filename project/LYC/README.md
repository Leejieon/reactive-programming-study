# WebFlux MVC 성능비교

- **환경설정**
    - 장비  : macbook pro 14,
    - 테스트 도구 : Apache JMeter
    - MVC : SpringBoot 3.24, mysql 8.0, Spring Web
    - WebFlux : SpringBoot 3.24, Spring Data R2DBC , Spring Reacitve Web
    - DB 데이터 : USER 더미 데이터 10,000건
    - REQUEST : [localhost/users](http://localhost/users)  를 통한 전체 유저 데이터 요청
    - 동시접속 : 5, 200, 1000명일 때로 각각 테스트 진행
    - 들어오는 요청은 10초 간격, 반복 횟수는 2회 설정

- MVC 테스트 결과
    - 동시 접속자 5명일 때
        
        ![mvc-5.png](https://file.notion.so/f/f/9e132be8-6c00-4f39-b489-0bf5bacbff36/a6e6784f-99b4-4730-930c-ca4a2354573c/mvc-5.png?id=6a928c62-7eef-4133-9159-35e72793b3aa&table=block&spaceId=9e132be8-6c00-4f39-b489-0bf5bacbff36&expirationTimestamp=1711706400000&signature=O6wYGLYXJ215DJuFz6o9PcFeaaRPrhcjLDdzTYWebpc&downloadName=mvc-5.png)
        
    - 동시 접속자 200명일 때
        
        ![mvc-200.png](https://file.notion.so/f/f/9e132be8-6c00-4f39-b489-0bf5bacbff36/b2170e42-0bf1-4aeb-9440-61968bd9b879/mvc-200.png?id=54003514-9055-42ca-b280-65ba00e20454&table=block&spaceId=9e132be8-6c00-4f39-b489-0bf5bacbff36&expirationTimestamp=1711706400000&signature=6llR7vRLOxtccv3dsip7XqGXKSHyxNzkNnu_eRn1hOM&downloadName=mvc-200.png)
        
    - 동시 접속자 1000명일 때
        
        ![mvc-1000.png](https://file.notion.so/f/f/9e132be8-6c00-4f39-b489-0bf5bacbff36/3ab12cbe-3afa-44b4-b9d2-949fad5f3367/mvc-1000.png?id=b693c1e3-a7a4-4311-8421-371105d2e4b2&table=block&spaceId=9e132be8-6c00-4f39-b489-0bf5bacbff36&expirationTimestamp=1711706400000&signature=zAAZ_-EE8ufvp96nHzRLTFFaAUyX-6xWsPKvK0qcFNw&downloadName=mvc-1000.png)
        
- WebFlux 테스트 결과
    - 동시 접속자 5명일 때
        
        ![스크린샷 2024-03-27 오후 11.21.13.png](https://file.notion.so/f/f/9e132be8-6c00-4f39-b489-0bf5bacbff36/fa09cf0d-966f-46ec-8bb3-c23196be1fee/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7_2024-03-27_%EC%98%A4%ED%9B%84_11.21.13.png?id=25d7b091-4220-4d64-a1fa-f5fe6218fe57&table=block&spaceId=9e132be8-6c00-4f39-b489-0bf5bacbff36&expirationTimestamp=1711706400000&signature=j9V40bLbp-Y_Uuhh9xatHhQWb6Ph3kRuuAywN8WhIP8&downloadName=%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7+2024-03-27+%EC%98%A4%ED%9B%84+11.21.13.png)
        
    - 동시 접속자 200명일 때
        
        ![flux-200.png](https://file.notion.so/f/f/9e132be8-6c00-4f39-b489-0bf5bacbff36/53efbf52-fa0b-47cd-91ff-66ca5b3b6ff8/flux-200.png?id=c9e38dec-5558-4117-b038-b954f6f2ccb8&table=block&spaceId=9e132be8-6c00-4f39-b489-0bf5bacbff36&expirationTimestamp=1711706400000&signature=kmmqVrqifQpC3TElx-s99KNw6JGEoBX5afYfMeUAabs&downloadName=flux-200.png)
        
    - 동시 접속자 1000명일 때
        
        ![flux-1000.png](https://file.notion.so/f/f/9e132be8-6c00-4f39-b489-0bf5bacbff36/8ff42261-ac4e-4cf8-8a16-f701d5105115/flux-1000.png?id=2b17130f-b8dd-46c7-840b-c0363089e7b5&table=block&spaceId=9e132be8-6c00-4f39-b489-0bf5bacbff36&expirationTimestamp=1711706400000&signature=TLjAwNZdNwxZd9PMw7w0dn4o_iwyO7KBS5nRWUoGRw4&downloadName=flux-1000.png)
        

**스케일 (부하에 따른 시스템 반응):**

- **Flux 애플리케이션**: 사용자 5명에서는 평균 응답 시간이 189ms였고, 사용자 200명에서는 3621ms, 사용자 1000명에서는 35021ms로 증가한다. 부하가 증가함에 따라 응답 시간이  증가하는 경향을 보인다.
    
    
- **MVC 애플리케이션**: 사용자 5명에서는 평균 응답 시간이 72ms였고, 사용자 200명에서는 1464ms, 사용자 1000명에서는 34048ms로 증가한다다. 여기서도 부하 증가에 따라 응답 시간이 크게 증가하지만, Flux 애플리케이션에 비해 상대적으로 덜 급격한 증가를 보인다.

**안정성 (높은 사용자 수에서의 오류율):**

- **Flux 애플리케이션**: 사용자 5, 200에서 0%, 1000명에서 오류율이 1%입니다.
- **MVC 애플리케이션**: 사용자 5명에서는 오류율이 0%이지만, 사용자 1000명에서는 오류율이 약 24.67%로 급격히 상승한다. 이는 부하가 증가함에 따라 MVC 애플리케이션의 안정성이 감소한다는 것을 의미한다.

**자원 사용 (효율적인 자원 사용 여부):**

- **Flux 애플리케이션**: 사용자가 증가함에 따라 throughput이 감소하고, 응답 시간이 증가하는 경향이 보인다. 사용자 1000명에서의 처리량은 20.8/sec로, 부하가 높아짐에 따라 상대적으로 처리량이 줄어들고 있다.
- **MVC 애플리케이션**: Flux와 유사하게, 처리량이 사용자 1000명에서 5.9/sec로 감소합니다. 하지만, Flux에 비해 오류율이 상승하는 점에서 자원 사용 측면에서 정확한 결과를 예측하기 어렵고, 오히려 비효율적인 모습을 보인다.

**종합적인 평가:**

- 스케일 측면에서는 MVC가 더 나은 성능을 보인다. Flux는 응답 시간이 사용자 수 증가에 따라 더 가파르게 증가했다.
- 안정성 측면에서는 Flux가 명확히 우수하다. 사용자 수 증가에도 불구하고 0%의 오류율을 유지하는 반면, MVC는 사용자 수가 많을 때 오류율이 크게 증가한다.
- 자원 사용 측면에서는 Flux가 오류 없이 처리량을 유지하는 것으로 보아 더 나은 성능을 보이는 것으로 확인할 수 있다.

이러한 결과를 종합해 볼 때, **부하가 높은 환경**에서는 **Flux 패턴**이 더 안정적이며 자원을 효율적으로 사용하는 것으로 보다. 다만, 오류율이 증가하는 MVC 패턴의 경우, 부하가 매우 높아질 때 (예를 들어 사용자 수가 1000명일 때) 성능 저하와 더불어 오류율이 크게 증가하는 것으로 나타난다. 이는 시스템의 한계에 도달했거나, MVC 패턴이 그러한 수준의 동시성을 처리하는 데 어려움이 있음을 확인할 수 있다. 반면, Flux 패턴은 동일한 부하 조건에서도 오류율을 1%로 유지하면서 처리량을 관리하는 것을 보여준다.

부하가 낮거나 중간 정도일 때 (사용자 수가 5명과 200명일 때), MVC 패턴은 Flux에 비해 더 낮은 평균 응답 시간을 가지고 있어, 부하가 가볍거나 중간 정도일 때는 더 빠른 처리가 가능할 수 있습니다. 그러나 부하가 증가함에 따라 이러한 이점은 감소한다.

결론적으로, 부하가 매우 높은 상황에서는 Flux가 더 나은 성능과 안정성을 제공하는 반면, 부하가 낮거나 중간일 때는 MVC가 더 빠른 반응을 보일 수 있다. 그러나 시스템의 규모가 확장될 가능성이 있다면, Flux가 더 견고하고 확장 가능한 패턴임을 확인할 수 있다. 

다만, 이 테스트에서 간과해서는 안되는 부분이 있다.

먼저 실제 서버로 작동하지 않고, [localhost](http://localhost) 에서 성능분석을 진행했다는 점.