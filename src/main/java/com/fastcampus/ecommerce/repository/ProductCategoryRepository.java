package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.Product;
import com.fastcampus.ecommerce.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategory.ProductCategoryId> {

}
