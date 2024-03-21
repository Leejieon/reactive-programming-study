package com.lime.productservice.dto;

import lombok.Data;

@Data
public class ProductDto {
    private String id;
    private String description;
    private Integer price;
}
