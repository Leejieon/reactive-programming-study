package com.lime.productservice.service;

import com.lime.productservice.dto.ProductDto;
import com.lime.productservice.repository.ProductRepository;
import com.lime.productservice.util.EntityDtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public Flux<ProductDto> getAll() {
        return this.productRepository.findAll()
                .map(EntityDtoUtil::toDto);
    }

    public Mono<ProductDto> getProductById(String id) {
        return this.productRepository.findById(id)
                .map(EntityDtoUtil::toDto);
    }

    public Mono<ProductDto> insertProduct(Mono<ProductDto> productDtoMono) {
        //가정 -> get a productDto -> somebody will be posting a dto to us
        return productDtoMono
                .map(EntityDtoUtil::toEntity)
                .flatMap(this.productRepository::insert) //extract that : map vs flatmap
                .map(EntityDtoUtil::toDto);
    }

    public Mono<ProductDto> updateProduct(String id, Mono<ProductDto> productDtoMono) {
        return this.productRepository.findById(id)  //this is present?
                            .flatMap(p -> productDtoMono
                                                .map(EntityDtoUtil::toEntity)
                                                .doOnNext(e -> e.setId(id))) //id가 blank니까 채워줌
                            .flatMap(this.productRepository::save) //mono를 반환하기 때문에 flatmap을 사용 - updated
                            .map(EntityDtoUtil::toDto);

        //now from the dto we have converted to entity
        //having a mono within a mono -> flat map 사용

        //id가 존재하면 -> emitting an entity boject
    }

    public Mono<Void> deleteProduct(String id) {
//        this.productRepository.deleteById(id); //동작안함 -> mono를 반환하기 때문! subscribe할 때까지 work하지 않음 in the reactive programming

        return this.productRepository.deleteById(id);
        //has to be subscribed - whoever called the service ㅇㅇ
    }
}
