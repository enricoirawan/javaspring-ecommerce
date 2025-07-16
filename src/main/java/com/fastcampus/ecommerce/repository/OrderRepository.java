package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.Order;
import com.fastcampus.ecommerce.model.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    @Query(value = """
      SELECT * FROM orders
      WHERE user_id = :userId
      """, nativeQuery = true)
    Page<Order> findByUserIdByPageable(Long userId, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    @Query(value = """
      SELECT * FROM orders
      WHERE user_id = :userId
      AND order_date BETWEEN :startDate AND :endDate
      """, nativeQuery = true)
    List<Order> findByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<Order> findByXenditInvoiceId(String xenditInvoiceId);

    List<Order> findByStatusAndOrderDateBefore(OrderStatus orderStatus, LocalDateTime dateTime);
}