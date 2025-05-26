package com.ecommerce.ecomapi.repository;

import com.ecommerce.ecomapi.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
    List<Order> findByPaymentStatusAndCreatedAtBefore(String paymentStatus, LocalDateTime beforeTime);

//    @EntityGraph(attributePaths = {"orderItems", "billingAddress", "shippingAddress"})
//    List<Order> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"orderItems", "billingAddress", "shippingAddress"})
    Optional<Order> findWithDetailsById(Long id);
    @EntityGraph(attributePaths = {"orderItems", "billingAddress", "shippingAddress", "user"})
    @Query("SELECT o FROM Order o")
    Page<Order> findAllWithDetails(Pageable pageable);


    @EntityGraph(attributePaths = {"orderItems", "billingAddress", "shippingAddress"})
    Page<Order> findByUser_Id(Long userId, Pageable pageable);

    void deleteByUser_Id(Long userId);

    @EntityGraph(attributePaths = {"orderItems", "billingAddress", "shippingAddress"})
    List<Order> findAllByIdIn(Collection<Long> orderIds);


}
