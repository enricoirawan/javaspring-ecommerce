package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.Category;
import com.fastcampus.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
