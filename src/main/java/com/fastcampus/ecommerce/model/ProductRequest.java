package com.fastcampus.ecommerce.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Nama produk tidak boleh kosong")
    @Size(min = 2, max = 100, message = "Nama produk harus antara 2 dan 100 karakter")
    private String name;

    @NotNull(message = "Harga tidak boleh kosong")
    @Positive(message = "Harga harus lebih besar dari 0")
    @Digits(integer = 10, fraction = 2, message = "Harga harus memiliki maksimal 10 digit dan 2 angka dibelakang koma")
    private BigDecimal price;

    @NotNull(message = "Deskripsi produk tidak boleh kosong")
    @Size(max = 1000, message = "Deskripsi produk tidak boleh lebih dari 100 karakter")
    private String description;

    @NotNull(message = "Stok tidak boleh kosong")
    @Min(value = 0, message = "Berat tidak boleh kurang dari 0")
    private Integer stockQuantity;

    @NotNull(message = "Berat tidak boleh kosong")
    @Min(value = 1000, message = "Berat tidak boleh kurang dari 1000 gram")
    private BigDecimal weight;

    @NotEmpty(message = "Harus ada satu kategori yang dipilih ")
    private List<Long> categoryIds;
}
