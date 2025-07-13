package com.fastcampus.ecommerce.controller;

import com.fastcampus.ecommerce.model.ProductRequest;
import com.fastcampus.ecommerce.model.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("products")
public class ProductController {

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findProductById(@PathVariable(value = "id") Long productId) {
        return ResponseEntity.ok(
                ProductResponse.builder()
                        .name("product " + productId)
                        .price(BigDecimal.ONE)
                        .name("deskripsi produk")
                        .build()
        );
    }

    @GetMapping("")
    public ResponseEntity<List<ProductResponse>> getAllProduct() {
        return ResponseEntity.ok(
                List.of(
                        ProductResponse.builder()
                                .name("product 1")
                                .price(BigDecimal.ONE)
                                .name("deskripsi produk")
                                .build(),
                        ProductResponse.builder()
                                .name("product 1")
                                .price(BigDecimal.ONE)
                                .name("deskripsi produk")
                                .build()
                )
        );
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest productRequest) {
        return ResponseEntity.ok(
                ProductResponse.builder()
                        .name(productRequest.getName())
                        .price(productRequest.getPrice())
                        .name(productRequest.getDescription())
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @RequestBody @Valid ProductRequest productRequest,
            @PathVariable(value = "id") Long productId
            ) {
        return ResponseEntity.ok(
                ProductResponse.builder()
                        .name(productRequest.getName() + " " + productId)
                        .price(productRequest.getPrice())
                        .name(productRequest.getDescription())
                        .build()
        );
    }
}
