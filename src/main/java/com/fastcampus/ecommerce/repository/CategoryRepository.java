package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.Category;
import com.fastcampus.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query(value = """
            SELECT * FROM product
            WHERE lower("name") LIKE :name
            """, nativeQuery = true)
    List<Category> findByName(String name);
}
