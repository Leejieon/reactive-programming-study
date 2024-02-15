# 람다식

- 람다표현식
    
    ![스크린샷 2024-02-14 오전 7.36.59.png](%E1%84%85%E1%85%A1%E1%86%B7%E1%84%83%E1%85%A1%E1%84%89%E1%85%B5%E1%86%A8%207100cbb68887404095dae2f98a0a264b/%25E1%2584%2589%25E1%2585%25B3%25E1%2584%258F%25E1%2585%25B3%25E1%2584%2585%25E1%2585%25B5%25E1%2586%25AB%25E1%2584%2589%25E1%2585%25A3%25E1%2586%25BA_2024-02-14_%25E1%2584%258B%25E1%2585%25A9%25E1%2584%258C%25E1%2585%25A5%25E1%2586%25AB_7.36.59.png)
    
    - 람다 파라미터는 함수형 인터페이스에 정의된 추상 메서드의 파라미터입니다.
    - 람다 몸체는 이 추창 메서드에서 구현되는 메서드 몸체를 의미합니다. 즉 람다 몸체에서 람다 파라미터로 전달받은 값들을 사용하게 됩니다.
    - ⭐함수형 인터페이스의 추상 메서드를 람다 표현식으로 작성해서 메서더의 파라미터로 전달한다는 의미는 메서드 자체를 전달하는 것이 아닌 함수형 인터페이스를 구현한 클래스의 인스턴스를 람다 표현식으로 작성해서 전달하는 것 입니다.
    
    ```java
    public class Example {
    	public static void main(String[] args {
    	
    		List<CryptoCurrency> cryptoCurrencies = SampleData.cryptoCurrencies;
    		
    		Collections.sort(cryptoCurrentcies,
    				(cc1,cc2) -> cc1.getUnit().name().compareTo(cc2.getUnit().name()));
    	}
    }
    ```
    

- 람다 캡처링
    - 람다 캡처링이란 람다 표현식 외부에서 정의된 자유 변수를 람다 표현식에 사용하는 것입니다.
    - 주의할 점은, 람다표현식에서 사용되는 자유 변수는 final 또는 final과 같은 효력을 지녀야 합니다. 즉 중간에 변수가 바뀌면 실행에 오류가 생깁니다.
    
    ```java
    public class Example {
    	public static void main(String[] args {
    	
    		List<CryptoCurrency> cryptoCurrencies = SampleData.cryptoCurrencies;
    		
    		String korBTC = "비트코인";
    		//korBTC = "빗코인"; 이렇게 자유변수를 바꾸면 오류가 생김 
    		
    		cryptoCurrentcies.stream()
    					.filter(cc -> cc.getUnit()==CurrencyUnit.BTC)
    					.map(cc -> cc.getName() + "(" + korBTC + ")" )
    					.forEach(cc -> System.out.println(cc));
    	}
    }
    ```
    
- 메서드 레퍼런스
    
    메서드 래퍼런스란 특정한 클래스내의 메서드를 참조하는 것을 의미합니다. 이를 통해 메서드를 호출하거나 전달할 수 있습니다.
    
    ```java
    (Car car) -> car.getCarName() == Car::getCarNAame
    ```
    
    메서드 레퍼런스에는 네 가지 유형이 있습니다.
    
    1. ClassName :: static method
    
    가장 대표적인 유형으로 클래스의 static 메서드를 표현하는 방식입니다.
    
    ```java
    public class Example{
        public static void main(String[] args) {
            List<CryptoCurrency> cryptoCurrencies = SampleData.cryptoCurrencies;
    
            cryptoCurrencies.stream()
                    .map(cc -> cc.getName())
    //                .map(name -> StringUtils.upperCase(name))
                    .map(StringUtils::upperCase)
                    .forEach(name -> System.out.println(name));
        }
    }
    ```
    
    현재 StringUtils::upperCase 이 부분에서 메서드 레퍼런스가 사용되었습니다. StringUtils 클래스의 upperCase 메서드 파라미터는 람다 파라미터로 전달되는 값을 사용합니다. 컴파일러가 내부적으로 람다 표현식의 파라미터 형식을 알고 있기에 이 부분을 생각하고, 간결한 메서드 레퍼런스 형태로 축약할 수 있습니다.
    
    1. ClassName :: instance 유형
    
    두 번째 유형은 클래스에 정의된 인스턴스 메서드를 표현하는 방식입니다.
    
    ```java
    public class Example {
        public static void main(String[] args) {
            List<CryptoCurrency> cryptoCurrencies = SampleData.cryptoCurrencies;
    
            cryptoCurrencies.stream()
                    .map(cc -> cc.getName())
    //                .map(name -> name.toUpperCase())
                    .map(String::toUpperCase)
                    .forEach(name -> System.out.println(name));
        }
    }
    ```
    
    위의 코드에서 String 클래스의 인스턴스 메서드인 toUpperCase()를 사용하여 레퍼런스를 표현하였습니다.
    
    즉, 클래스의 static 메서드와 instance 메소드  두 경우 모두 메서드 레퍼런스로 표현할 수 있음을 알 수 있습니다.
    
    1. Object :: instance method 유형
    
    위의 2번 유형과 다르게 클래스의 이름이 아닌 클래스의 객체인 Object 형태로 표현하는 방식입니다. 이와 같은 유형은 람다 표현식 외부에서 정의된 객체의 메서드를 호출할 때 사용합니다.
    
    ```java
    public class Example {
        public static void main(String[] args) {
            List<CryptoCurrency> cryptoCurrencies = SampleData.cryptoCurrencies;
            int btcPrice = cryptoCurrencies.stream()
                    .filter(cc -> cc.getUnit() == CurrencyUnit.BTC)
                    .findFirst()
                    .get()
                    .getPrice();
    
            int amount = 2;
    
            PaymentCalculator calculator = new PaymentCalculator();
            cryptoCurrencies.stream()
                    .filter(cc -> cc.getUnit() == CurrencyUnit.BTC)
                    .map(cc -> new ImmutablePair(cc.getPrice(), amount))
    //                .map(pair -> calculator.getTotalPayment(pair))
                    .map(calculator::getTotalPayment)
                    .forEach(System.out::println);
        }
    }
    ```
    
    여기서 ImmutablePair를 사용합니다.
    
    ![스크린샷 2024-02-14 오후 8.01.40.png](%E1%84%85%E1%85%A1%E1%86%B7%E1%84%83%E1%85%A1%E1%84%89%E1%85%B5%E1%86%A8%207100cbb68887404095dae2f98a0a264b/%25E1%2584%2589%25E1%2585%25B3%25E1%2584%258F%25E1%2585%25B3%25E1%2584%2585%25E1%2585%25B5%25E1%2586%25AB%25E1%2584%2589%25E1%2585%25A3%25E1%2586%25BA_2024-02-14_%25E1%2584%258B%25E1%2585%25A9%25E1%2584%2592%25E1%2585%25AE_8.01.40.png)
    
    ImmutablePair는 Pair의 구현 클래스로, 두 객체를 하나의 단위로 묶어서 관리하는데 사용합니다. 현재 위 코드에서 BTC의 가격과 수량을 설정합니다.
    
    이 후 외부에 정의된 PaymentCalculator의 객체인 calculator를 메서드 레퍼런스를 사용하여 표현한 코드입니다. 외부에서 정의된 객체의 메서드를 호출하는 방식이기에 object :: instance method 유형인 것을 볼 수 있습니다.
    
    1. ClassName::new 유형
    
    마지막으로 생성자입니다. 람다 표현식 내부에서 어떤 클래스의 생성자를 사용할 경우 메서드 레퍼런스가 가능합니다. 그리고 이를 ClassName :: new와 같은 방식으로 사용할 수 있습니다.
    
    ```java
    public class Example {
        public static void main(String[] args) {
            List<CryptoCurrency> cryptoCurrencies = SampleData.cryptoCurrencies;
    
            int amount = 2;
    
            Optional<PaymentCalculator> optional =
                    cryptoCurrencies.stream()
                                    .filter(cc -> cc.getUnit() == CurrencyUnit.BTC)
                                    .map(cc -> new ImmutablePair(cc.getPrice(), amount))
    //                                            .map(pair -> new PaymentCalculator(pair))
                                    .map(PaymentCalculator::new)
                                    .findFirst();
    
            System.out.println(optional.get().getTotalPayment());
        }
    }
    ```
    
    위의 3번예제와 다르게, 람다 표현식 내부에서 PaymentCalculator 객체를 생성한 후에 최종 값으로 리턴 반환합니다. 이를 통해 코드의 가독성과 간결성을 높일 수 있습니다.