package com.ecommerce.ecomapi.serviceImpl;

import com.ecommerce.ecomapi.dto.*;
import com.ecommerce.ecomapi.entity.*;
import com.ecommerce.ecomapi.enums.OrderStatus;
import com.ecommerce.ecomapi.exception.OrderIsAlreadyPlacedException;
import com.ecommerce.ecomapi.exception.OrderNotFoundException;
import com.ecommerce.ecomapi.exception.ProductNotFoundException;
import com.ecommerce.ecomapi.repository.*;
import com.ecommerce.ecomapi.service.EmailService;
import com.ecommerce.ecomapi.service.OrderService;
import com.ecommerce.ecomapi.service.PaymentService;
import com.ecommerce.ecomapi.utility.ObjectIdUtils;
import com.ecommerce.ecomapi.utility.SecurityUtil;
import com.mongodb.client.result.UpdateResult;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private AddressRepo addressRepo;
    @Autowired
    private OrderRepo orderRepo;
    @Autowired
    private OrderItemRepo orderItemRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private EmailService emailService;

    /*
    * total 2*2
    * Address shippingAddress = processAddress(modelMapper.map(request.getShippingAddress(), Address.class), user);
        Address billingAddress = processAddress(modelMapper.map(request.getBillingAddress(), Address.class), user);
    *
    * */

    /*Create order using card info */
    @Transactional
    public Order createCartOrder(CartOrderRequestDto request , User user){
        Address shippingAddress = saveAddress(modelMapper.map(request.getShippingAddress(), Address.class), user);
        Address billingAddress = saveAddress(modelMapper.map(request.getBillingAddress(), Address.class), user);

        Cart cart = cartRepo.findById(request.getCartId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        final Order order = new Order();
        order.setUser(user);
        order.setBillingAddress(billingAddress);
        order.setShippingAddress(shippingAddress);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem>  orderItems = cart.getCartItems().stream()
                .map(p -> {
                    Product product = productRepo.findById(ObjectIdUtils.toObjectId(p.getProductId()))
                            .orElseThrow(() -> new RuntimeException("Product not found: " + p.getProductId()));

                    OrderItem item = new OrderItem();
                    item.setProductId(String.valueOf(product.getId()));
                    item.setName(product.getProductName());
                    item.setQuantity(p.getQuantity());
                    item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())));
                    item.setDiscountedPrice(product.getDiscountedPrice().multiply(BigDecimal.valueOf(p.getQuantity())));
                    item.setMainImage(product.getMainImageUrl());
                    item.setAdditionalImages(product.getAdditionalImageUrls());
                    item.setOrder(order);
                    return item;
                }).collect(Collectors.toList());

        order.setOrderItems(orderItems);

        BigDecimal totalAmount = order.getOrderItems().stream().map(orderItem -> orderItem.getTotalPrice()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal finalAmount = order.getOrderItems().stream().map(orderItem -> orderItem.getDiscountedPrice()).reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setDiscount(totalAmount.subtract(finalAmount));
        order.setTotalAmount(totalAmount);
        order.setFinalAmount(finalAmount);
        order.setDeliveryDate(Date.from(LocalDateTime.now().plusDays(5)
                .atZone(ZoneId.systemDefault()).toInstant()));

        Order creatdedOrder = orderRepo.save(order);

        return creatdedOrder;
    }

    /*Create order through all order details */
    @Transactional
    public Order createOrder(OrderCreateRequest request ,User user){

        Address shippingAddress = saveAddress(modelMapper.map(request.getShippingAddress(), Address.class), user);
        Address billingAddress = saveAddress(modelMapper.map(request.getBillingAddress(), Address.class), user);

        final Order order = new Order();
        order.setUser(user);
        order.setStatus("Not-Confirmed");
        order.setBillingAddress(billingAddress);
        order.setShippingAddress(shippingAddress);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem>  orderItems = request.getProducts().stream()
                .map(p -> {
                    Product product = productRepo.findById(ObjectIdUtils.toObjectId(p.getProductId()))
                            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + p.getProductId()));

                    OrderItem item = new OrderItem();
                    item.setProductId(String.valueOf(product.getId()));
                    item.setName(product.getProductName());
                    item.setQuantity(p.getQuantity());
                    item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())));
                    item.setDiscountedPrice(product.getDiscountedPrice().multiply(BigDecimal.valueOf(p.getQuantity())));
                    item.setMainImage(product.getMainImageUrl());
                    item.setAdditionalImages(product.getAdditionalImageUrls());
                    item.setOrder(order);
                    return item;
                }).collect(Collectors.toList());

        order.setOrderItems(orderItems);

        BigDecimal totalAmount = order.getOrderItems().stream().map(orderItem -> orderItem.getTotalPrice()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal finalAmount = order.getOrderItems().stream().map(orderItem -> orderItem.getDiscountedPrice()).reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setDiscount(totalAmount.subtract(finalAmount));
        order.setTotalAmount(totalAmount);
        order.setFinalAmount(finalAmount);
        order.setDeliveryDate(Date.from(LocalDateTime.now().plusDays(5)
                .atZone(ZoneId.systemDefault()).toInstant()));

        Order creatdedOrder = orderRepo.save(order);

        return creatdedOrder;
    }

    /*Get all Orders */
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return orderRepo.findAllWithDetails(pageable);

    }

    //    Find order using order-Id
    @Transactional(readOnly = true)
    public Order getOrderByIdWithDetails(Long orderId) {
        return orderRepo.findWithDetailsById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    /*Get All orders of specific user */
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByUserId(Long userId,int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return orderRepo.findByUser_Id(userId, pageable);
    }

    /*get all order by seller id */
    @Transactional(readOnly = true)
    public Page<Order> getOrdersBySellerId(String sellerId, int page, int size, String sortBy, String direction) {
        List<Product> products = productRepo.findBySellerId(sellerId);
        if (products.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }

        List<String> productIds = products.stream()
                .map(product -> product.getId().toString())
                .toList();

        List<OrderItem> orderItems = orderItemRepo.findByProductIdIn(productIds);
        if (orderItems.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }

        // Collect unique order IDs from the order items
        Set<Long> orderIds = orderItems.stream()
                .map(orderItem -> orderItem.getOrder().getId())
                .collect(Collectors.toSet());

        List<Order> allOrders = orderRepo.findAllByIdIn(orderIds);

        // Sort the orders
        Comparator<Order> comparator;
        if ("id".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Order::getId);
        } else {
            comparator = Comparator.comparing(Order::getCreatedAt); // default sort
        }
        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        List<Order> sortedOrders = allOrders.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        // Manual pagination
        int start = Math.min(page * size, sortedOrders.size());
        int end = Math.min(start + size, sortedOrders.size());
        List<Order> pagedOrders = sortedOrders.subList(start, end);

        return new PageImpl<>(pagedOrders, PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy)), sortedOrders.size());
    }

    /*get specific order items by seller id*/
    @Transactional(readOnly = true)
    public Page<Order> getOrdersBySellerIdItems(String sellerId, int page, int size, String sortBy, String direction) {
        // Step 1: Get all products by this seller
        List<Product> products = productRepo.findBySellerId(sellerId);
        if (products.isEmpty()) {
            return Page.empty();
        }

        // Step 2: Extract product IDs
        List<String> sellerProductIds = products.stream()
                .map(product -> product.getId().toString())
                .toList();

        // Step 3: Get order items that belong to the seller's products
        List<OrderItem> sellerOrderItems = orderItemRepo.findByProductIdIn(sellerProductIds);
        if (sellerOrderItems.isEmpty()) {
            return Page.empty();
        }

        // Step 4: Get distinct order IDs that contain seller's products
        Set<Long> relevantOrderIds = sellerOrderItems.stream()
                .map(orderItem -> orderItem.getOrder().getId())
                .collect(Collectors.toSet());

        // Step 5: Fetch full orders (with address and items)
        List<Order> allOrders = orderRepo.findAllByIdIn(relevantOrderIds);

        // Step 6: Filter order items to only include the seller’s own products
        for (Order order : allOrders) {
            List<OrderItem> filteredItems = order.getOrderItems().stream()
                    .filter(item -> sellerProductIds.contains(item.getProductId().toString()))
                    .toList();
            order.setOrderItems(filteredItems);
        }

        // Step 7: Sort the list manually
        Comparator<Order> comparator;

        if ("id".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Order::getId);
        } else {
            // Default to createdAt
            comparator = Comparator.comparing(Order::getCreatedAt);
        }

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }


        List<Order> sortedOrders = allOrders.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        // Step 8: Paginate manually
        int start = (int) Math.min((long) page * size, sortedOrders.size());
        int end = Math.min(start + size, sortedOrders.size());
        List<Order> pagedOrders = sortedOrders.subList(start, end);

        return new PageImpl<>(pagedOrders, PageRequest.of(page, size, Sort.Direction.fromString(direction), sortBy), sortedOrders.size());
    }

    /*get confirm orders by seller id*/
    @Transactional(readOnly = true)
    public Page<Order> getConfirmedOrdersBySeller(String sellerId, int page, int size, String sortBy, String direction) {
        List<Product> products = productRepo.findBySellerId(sellerId);
        if (products.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }

        // Step 2: Extract product IDs
        List<String> sellerProductIds = products.stream()
                .map(product -> product.getId().toString())
                .toList();

        // Step 3: Get order items that belong to the seller's products
        List<OrderItem> sellerOrderItems = orderItemRepo.findByProductIdIn(sellerProductIds);
        if (sellerOrderItems.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }

        // Step 4: Get distinct order IDs that contain seller's products
        Set<Long> relevantOrderIds = sellerOrderItems.stream()
                .map(orderItem -> orderItem.getOrder().getId())
                .collect(Collectors.toSet());

        // Step 5: Fetch full orders
        List<Order> allOrders = orderRepo.findAllByIdIn(relevantOrderIds);

        // Step 6: Filter order items to only include the seller’s products
        for (Order order : allOrders) {
            List<OrderItem> filteredItems = order.getOrderItems().stream()
                    .filter(item -> sellerProductIds.contains(item.getProductId().toString()))
                    .toList();
            order.setOrderItems(filteredItems);
        }

        // Step 7: Sort orders
        Comparator<Order> comparator;
        if ("id".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Order::getId);
        } else {
            comparator = Comparator.comparing(Order::getCreatedAt);
        }
        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }
        List<Order> sortedOrders = allOrders.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        // Step 8: Create sublist for pagination
        int start = Math.min(page * size, sortedOrders.size());
        int end = Math.min(start + size, sortedOrders.size());
        List<Order> pagedList = sortedOrders.subList(start, end);

        return new PageImpl<>(pagedList, PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy)), sortedOrders.size());
    }

    /*save address with the help of saveUnsavedNewAddress() method */
    public Address saveAddress(Address address, User user){
        if (address.getId() != null) {
            Address existing = addressRepo.findById(Long.valueOf(address.getId()))
                    .orElseThrow(() -> new RuntimeException("Address not found"));

            if (existing.isSaved()){
                return saveUnsavedNewAddress(existing, user);
            }

        }
            return saveUnsavedNewAddress(address, user);
    }

    /*Save unsaved address*/
    public Address saveUnsavedNewAddress(Address address,User user){
        System.out.println("saveUnsavedNewAddress is working");
        // Else create new
        Address newAddress = new Address();
        newAddress.setStreet(address.getStreet());
        newAddress.setCity(address.getCity());
        newAddress.setState(address.getState());
        newAddress.setZip(address.getZip());
        newAddress.setCountry(address.getCountry());
        newAddress.setUser(user);
        newAddress.setSaved(false); // Not saved unless user explicitly saves it
        return addressRepo.save(newAddress);
    }

    /*Delete order by order id*/
    @Override
    public boolean deleteOrderById(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order != null) {
            orderRepo.deleteById(order.getId());
            return true;
        }
        return false;
    }

    /*Delete all orders of specific user*/
    @Transactional
    public void deleteOrdersByUserId(Long userId) {
        orderRepo.deleteByUser_Id(userId); // Cascades to orderItems due to orphanRemoval
    }

    /* cancel order by seller id */
    public boolean cancelOrder(Long orderId, String reasonMessage) throws StripeException {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with this id"));

        if (order.getRefunded()) {
            throw new IllegalStateException("Order has already been refunded.");
        }

        if (!"Confirmed".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Only confirmed orders can be refunded.");
        }

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(order.getPaymentIntentId())
                .build();

        Refund refund = Refund.create(params);

        if ("succeeded".equalsIgnoreCase(refund.getStatus())) {
            order.setRefunded(true);
            order.setRefundDate(new Date());
            order.setCancelledReason(reasonMessage);
            order.setStatus("Cancelled");
            // Optional: save refund ID
            // order.setStripeRefundId(refund.getId());
            orderRepo.save(order);



            // Restore stock
            for (OrderItem item : order.getOrderItems()) {
                Query query = new Query(Criteria.where("_id").is(item.getProductId()));
                Update update = new Update().inc("quantity", item.getQuantity());
                mongoTemplate.updateFirst(query, update, Product.class);
            }

            String cancellationReason = "Customer requested cancellation"; // or any dynamic reason
            String cancellationEmailContent = emailService.buildOrderCancellationEmail(order, cancellationReason);
            emailService.sendHtmlEmail(order.getUser().getEmail(), "Order Cancellation Confirmation", cancellationEmailContent);


            System.out.println("Refund successful. Stock restored.");
            System.out.println("Refund ID: " + refund.getId());
            return true;
        } else {
            System.out.println("Refund failed with status: " + refund.getStatus());
            return false;
        }
    }

    @Override
    public PaymentIntentResponseDto doPayment(Long userId, PaymentRequest paymentRequest) throws StripeException {
        Order order = orderRepo.findById(Long.valueOf(paymentRequest.getOrderId()))
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("CONFIRMED".equalsIgnoreCase(order.getStatus())) {
            throw new OrderIsAlreadyPlacedException(" Order is already placed successfully.");
        }
//            For COD payment
        if ("COD".equalsIgnoreCase(order.getPaymentMethod())) {
            throw new OrderIsAlreadyPlacedException(" Order is already placed successfully.");
        }
        //For card payment
        if ("CARD".equalsIgnoreCase(order.getPaymentMethod())) {
            PaymentIntentResponseDto responseDto = new PaymentIntentResponseDto();
            responseDto.setPaymentIntentId(order.getPaymentIntentId());
            responseDto.setClientSecret(order.getClientSecret());
            return responseDto;
        }

        order.setPaymentMethod(paymentRequest.getPaymentMethod());

        if (paymentRequest.getPaymentMethod().equalsIgnoreCase("COD")) {
            order.setPaymentStatus("succeeded");
            order.setPaymentDate(LocalDateTime.now());
            order.setStatus("Confirmed");
            order.setOrderStatus(OrderStatus.PROCESSING);
        } else if (paymentRequest.getPaymentMethod().equalsIgnoreCase("CARD")) {

            PaymentIntentRequest intentRequest = new PaymentIntentRequest();
            intentRequest.setAmount(order.getFinalAmount().longValue());
            intentRequest.setOrderId(order.getId());
            intentRequest.setUserId(userId);

            PaymentIntentResponseDto responseDto = paymentService.createPaymentIntent(intentRequest);
            order.setPaymentIntentId(responseDto.getPaymentIntentId());
            order.setClientSecret(responseDto.getClientSecret());
            order.setPaymentStatus("PENDING");
            orderRepo.save(order);

            return responseDto;
        }

        LocalDateTime deliveryDateTime = LocalDateTime.now().plusDays(5);
        Date deliveryDate = Date.from(deliveryDateTime.atZone(ZoneId.systemDefault()).toInstant());

        order.setDeliveryDate(deliveryDate);

        // ✅ Subtract stock ONLY when payment is successful
        if (order.getPaymentStatus().equalsIgnoreCase("succeeded")) {
            for (OrderItem item : order.getOrderItems()) {
                // Atomic quantity update using MongoTemplate (recommended)
                Query query = new Query(Criteria.where("_id").is(item.getProductId()).and("quantity").gte(item.getQuantity()));
                Update update = new Update().inc("quantity", -item.getQuantity());
                UpdateResult result = mongoTemplate.updateFirst(query, update, Product.class);

                if (result.getModifiedCount() == 0) {
                    // Optional: Handle insufficient stock rollback or notify
                    throw new RuntimeException("Insufficient stock for product: " + item.getProductId());
                }
            }

        }

        String emailBody = emailService.buildOrderConfirmationEmail(order);
        emailService.sendHtmlEmail(order.getUser().getEmail(), "Your Order is Confirmed", emailBody);

        orderRepo.save(order);
        return new PaymentIntentResponseDto("COD_PAYMENT", "Order placed successfully with COD");
    }


}
