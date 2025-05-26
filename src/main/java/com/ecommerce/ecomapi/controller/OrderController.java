package com.ecommerce.ecomapi.controller;

import com.ecommerce.ecomapi.dto.*;
import com.ecommerce.ecomapi.entity.*;
import com.ecommerce.ecomapi.enums.OrderStatus;
import com.ecommerce.ecomapi.repository.AddressRepo;
import com.ecommerce.ecomapi.repository.CartRepo;
import com.ecommerce.ecomapi.repository.OrderRepo;
import com.ecommerce.ecomapi.repository.ProductRepo;
import com.ecommerce.ecomapi.service.OrderService;
import com.ecommerce.ecomapi.service.PaymentService;
import com.ecommerce.ecomapi.service.UserService;
import com.ecommerce.ecomapi.utility.SecurityUtil;
import com.mongodb.client.result.UpdateResult;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Tag(name = "07. Order Controller",
        description = "Handles all order-related operations such as placing, updating, deleting, and retrieving orders.")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private SecurityUtil securityUtil;

    /* Get all orders */
    @Operation(summary = "Get all orders (admin only)",
            description = "Retrieve all orders with pagination and sorting support. Only accessible by ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-all-orders")
    public ResponseEntity<?> getAllOrders(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(value = "sortBy", defaultValue = "discountedPrice") String sortBy,
                                          @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        Page<Order> orders = orderService.getAllOrders(page,size,sortBy,direction);
        if (orders != null && !orders.isEmpty()){
            return new ResponseEntity<>(orders, HttpStatus.OK);
        }
        return new ResponseEntity<>("Order are presently.",HttpStatus.NO_CONTENT);
    }

    /* Get all order of specific user */
    @Operation(summary = "Get orders of logged-in user",
            description = "Fetch all orders placed by the currently logged-in user, with pagination and sorting.")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/get-order-of-user")
    public ResponseEntity<?> getAllOrderOfUser(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(value = "sortBy", defaultValue = "discountedPrice") String sortBy,
                                               @RequestParam(value = "direction", defaultValue = "asc") String direction) {

        Page<Order> orders =  orderService.getOrdersByUserId(securityUtil.getCurrentUserId(),page,size,sortBy,direction);
        if (orders != null && !orders.isEmpty()){
            return new ResponseEntity<>(orders, HttpStatus.OK);
        }
        return new ResponseEntity<>("Order are presently.",HttpStatus.NO_CONTENT);
    }

    /* Get order by id */

    @Operation(summary = "Get order by ID",
            description = "Retrieve full order details (including items and address) for a specific order ID.")
    @GetMapping("/get-order/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {

        return new ResponseEntity<>(orderService.getOrderByIdWithDetails(id), HttpStatus.OK);
    }

    /*Delete order by user id */
    @Operation(summary = "Delete all orders of a specific user",
            description = "Delete all orders belonging to the specified user ID. Only accessible by USER role.")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/delete-order-byuserid/{id}")
    public ResponseEntity<?> deleteOrderByUserId(@PathVariable Long id) {
        orderService.deleteOrdersByUserId(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /* Get orders by seller id */
    @Operation(
            summary = "Get orders placed for the current seller",
            description = "Returns a list of orders where the current authenticated SELLER is associated."
    )
    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/get-order-bySellerId")
    public ResponseEntity<?> getOrdersBySeller(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(value = "sortBy", defaultValue = "discountedPrice") String sortBy,
                                                         @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        Long id = securityUtil.getCurrentUserId();
        Page<Order> orders = orderService.getOrdersBySellerId(String.valueOf(id),page,size,sortBy,direction);
        return ResponseEntity.ok(orders);
    }

    /* Get Seller items only */
    @Operation(
            summary = "Get seller's items from orders",
            description = "Fetches only the items/products that belong to the authenticated SELLER across all orders."
    )
    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/get-seller-product")
    public ResponseEntity<?> getOrdersBySellerIdItems(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(value = "sortBy", defaultValue = "discountedPrice") String sortBy,
                                                      @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        Long id = securityUtil.getCurrentUserId();
        Page<Order> orders = orderService.getOrdersBySellerIdItems(String.valueOf(id),page,size,sortBy,direction);
        return ResponseEntity.ok(orders);
    }

    /* Find confirm orders of seller */
    @Operation(
            summary = "Get confirmed orders for the seller",
            description = "Retrieves orders that have been confirmed and belong to the authenticated SELLER."
    )
    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/confirm-order-to-seller")
    public ResponseEntity<?> getConfirmOrder(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(value = "sortBy", defaultValue = "discountedPrice") String sortBy,
                                             @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        Long id = securityUtil.getCurrentUserId();
        Page<Order> orders = orderService.getConfirmedOrdersBySeller(String.valueOf(id),page,size,sortBy,direction);
        return ResponseEntity.ok(orders);
    }

    /*Create Order */
    @Operation(
            summary = "Create Order",
            description = "Creates a new order from product list with shipping and billing addresses. Only accessible to USER or SELLER roles.")
    @PreAuthorize("hasAnyRole('USER', 'SELLER')")
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody OrderCreateRequest request) {
        User user = userService.getUserById(securityUtil.getCurrentUserId());

        for (var p : request.getProducts()) {
            if (!ObjectId.isValid(p.getProductId())) {
                return ResponseEntity.badRequest().body("Invalid product ID format: " + p.getProductId());
            }
        }

        if (user != null) {
            Order creatdedOrder = orderService.createOrder(request, user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "hello order is created");
            response.put("data", creatdedOrder);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        return new ResponseEntity<>("Something went to wrong ", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /* Crate order using cart */
    @Operation(
            summary = "Create Order from Cart",
            description = "Creates an order based on the products available in the user's cart. Only accessible to USER or SELLER roles.")
    @PreAuthorize("hasAnyRole('USER', 'SELLER')")
    @PostMapping("/create-cart-order")
    public ResponseEntity<?> createOrderFromCart(@RequestBody CartOrderRequestDto request) {
        Long userId = securityUtil.getCurrentUserId();
        User user = userService.getUserById(Long.valueOf(userId));
        if (user != null) {
            Order creatdedOrder = orderService.createCartOrder(request, user);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "hello order is created");
            response.put("data", creatdedOrder);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        return new ResponseEntity<>("Something went to wrong ", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /*Delete Order id*/
    @Operation(
            summary = "Delete an order",
            description = "Allows an ADMIN to delete an order by its ID."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable("id") Long orderId) {

        if (orderService.deleteOrderById(orderId)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    /* Cancel order */
    @Operation(
            summary = "Cancel an order",
            description = "Allows a USER to cancel their order by providing the order ID and a reason for cancellation. Refund money ."
    )
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/cancel-order")
    public ResponseEntity<?> cancelOrder(@RequestBody CancelOrderDto dto) throws StripeException {
        boolean isCancelled = orderService.cancelOrder(dto.getOrderId(), dto.getReasonMessage());

        if (isCancelled) {
            return ResponseEntity.ok("Order cancelled successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong while cancelling the order.");
        }
    }

    @Operation(
            summary = "Process payment for an order",
            description = "Allows a USER to make a payment for an order using either a card (Stripe integration) or Cash on Delivery (COD)."
    )
    @PreAuthorize("hasRole('USER')")
    /*Do payment by Card or COD */
    @PutMapping("/do-payment")
    public ResponseEntity<?> doPayment( @RequestBody PaymentRequest paymentRequest) throws StripeException {
        Long userId = securityUtil.getCurrentUserId();
        User user = userService.getUserById(userId);
        if (user == null) {
            return new ResponseEntity<>("User not found or unauthorized.",HttpStatus.NOT_FOUND);
        }
        PaymentIntentResponseDto responseDto = orderService.doPayment(user.getId(),paymentRequest);
        if ("COD_PAYMENT".equals(responseDto.getPaymentIntentId())) {
            return ResponseEntity.ok(Map.of("Order placed successfully with COD.", responseDto.getClientSecret()));
        }
        return ResponseEntity.ok(responseDto);

    }

}
