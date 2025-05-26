package com.ecommerce.ecomapi.scheduller;

import com.ecommerce.ecomapi.entity.Order;
import com.ecommerce.ecomapi.enums.OrderStatus;
import com.ecommerce.ecomapi.repository.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderExpirySchedular {
    @Autowired
    private OrderRepo orderRepo;

//    @Scheduled(fixedRate = 600 * 60 * 1000) // runs every hour
    public void expirePendingOrders() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(1); // example: expire after 2 hours
//        .minusHours(2);

        List<Order> expiredOrders = orderRepo.findByPaymentStatusAndCreatedAtBefore("PENDING", cutoffTime);

        for (Order order : expiredOrders) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            order.setCancelledReason("Payment timeout");
            orderRepo.save(order);

            // (Optional) Add logic to restore product quantities if needed
        }

        System.out.println("Expired " + expiredOrders.size() + " pending orders.");
    }

}
