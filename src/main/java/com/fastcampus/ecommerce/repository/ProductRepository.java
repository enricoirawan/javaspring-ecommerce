package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(value = """
            SELECT * FROM product
            WHERE lower("name") LIKE :name
            """, nativeQuery = true)
    Page<Product> findByNamePageable(@Param("name") String name, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT p.* FROM product p
            JOIN product_category pc ON p.product_id = pc.product_id
            JOIN category c ON pc.category_id = c.category_id
            WHERE c.name = :categoryName
            """, nativeQuery = true)
    List<Product> findByCategory(@Param("categoryName") String categoryName);

    @Query(value = """
            SELECT * FROM product
            """, nativeQuery = true)
    Page<Product> findByPageable(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
      SELECT p FROM Product p WHERE p.id = :id
      """)
    Optional<Product> findByIdWithPessimisticLock(@Param("id") Long id);
}
