package com.lime.productservice.util;

import com.lime.productservice.dto.ProductDto;
import com.lime.productservice.entity.Product;
import org.springframework.beans.BeanUtils;

public class EntityDtoUtil {
    public static ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setDescription(product.getDescription());
        BeanUtils.copyProperties(product, dto);

                //이런거 제공함 ㅇㅇ
        //copy from entity to dto

        return dto;
    }

    public static Product toEntity(ProductDto dto) {
        Product product = new Product();
        product.setDescription(dto.getDescription());
        BeanUtils.copyProperties(dto, product);

        //이런거 제공함 ㅇㅇ
        //copy from entity to dto

        return product;
    }
}
