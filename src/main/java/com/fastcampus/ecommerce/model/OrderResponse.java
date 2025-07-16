package com.fastcampus.ecommerce.model;

import com.fastcampus.ecommerce.entity.Order;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
public class OrderResponse implements Serializable {

    private Long orderId;
    private Long userId;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal taxFee;
    private BigDecimal totalAmount;
    private OrderStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime orderDate;
    private String xenditInvoiceId;
    private String xenditPaymentStatus;
    private String xenditPaymentMethod;
    private String paymentUrl;

    public static OrderResponse fromOrder(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .taxFee(order.getTaxFee())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .xenditInvoiceId(order.getXenditInvoiceId())
                .xenditPaymentStatus(order.getXenditPaymentStatus())
                .xenditPaymentMethod(order.getXenditPaymentMethod())
                .build();
    }
}