package com.ecommerce.ecomapi.serviceImpl;

import com.ecommerce.ecomapi.dto.CartDto;
import com.ecommerce.ecomapi.dto.CartItemDto;
import com.ecommerce.ecomapi.entity.Cart;
import com.ecommerce.ecomapi.entity.CartItem;
import com.ecommerce.ecomapi.entity.Product;
import com.ecommerce.ecomapi.entity.User;
import com.ecommerce.ecomapi.exception.ProductNotFoundException;
import com.ecommerce.ecomapi.exception.UserNotFoundException;
import com.ecommerce.ecomapi.repository.CartItemsRepo;
import com.ecommerce.ecomapi.repository.CartRepo;
import com.ecommerce.ecomapi.repository.ProductRepo;
import com.ecommerce.ecomapi.repository.UserRepo;
import com.ecommerce.ecomapi.service.CartService;
import com.ecommerce.ecomapi.utility.ObjectIdUtils;
import com.ecommerce.ecomapi.utility.SecurityUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private CartItemsRepo cartItemsRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SecurityUtil securityUtil;

    /* Get cart by user id */
    public CartDto getCartByUserId(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Cart cart = cartRepo.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart(user, new ArrayList<>());
            return cartRepo.save(newCart); // save newly created cart
        });

        List<ObjectId> productIds = cart.getCartItems().stream()
                .map(item -> ObjectIdUtils.toObjectId(item.getProductId()))
                .collect(Collectors.toList());

        Map<ObjectId, Product> productMap = productRepo.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<CartItemDto> items = cart.getCartItems().stream().map(item -> {
            ObjectId productId = ObjectIdUtils.toObjectId(item.getProductId());
            Product product = productMap.get(productId);
            if (product == null) {
                throw new ProductNotFoundException("Product not found for ID: " + item.getProductId());
            }
            return new CartItemDto(
                    item.getId(),
                    ObjectIdUtils.toString(product.getId()),
                    product.getProductName(),
                    product.getMainImageUrl(),
                    product.getDiscountedPrice(),
                    item.getQuantity(),
                    product.getDiscountedPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }).collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .map(CartItemDto::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDto(cart.getId(), totalAmount, items);
    }

    /* add product to cart */
    public Cart addProuctToCart(Long userId, ObjectId productId, Integer quantity){
        Cart cart = cartRepo.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(userRepo.findById(userId).orElseThrow(()-> new RuntimeException("User Not found")));
            return cartRepo.save(newCart);
        });

        Product product = productRepo.findById(productId).orElseThrow(() -> new ProductNotFoundException("Product not found"));

        Optional<CartItem> existingCartItem = cartItemsRepo.findByCartIdAndProductId(cart.getId(), ObjectIdUtils.toString(productId));

        if (existingCartItem.isPresent()){
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setPrice(product.getDiscountedPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            cartItemsRepo.save(cartItem);
        }else {
            CartItem newCartItem = new CartItem();
            newCartItem.setCart(cart);
            newCartItem.setProductId(ObjectIdUtils.toString(productId));
            newCartItem.setQuantity(quantity);
            newCartItem.setMainImageUrl(product.getMainImageUrl());
            newCartItem.setPrice(product.getDiscountedPrice().multiply(BigDecimal.valueOf(quantity)));
            cartItemsRepo.save(newCartItem);
        }

        List<CartItem> updatedCartItems = cartItemsRepo.findByCartId(cart.getId());
        BigDecimal totalAmount = updatedCartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalAmount(totalAmount);
        return cartRepo.save(cart);
    }

    /* update cart item quantity */
    @Transactional
    public void updateCartItemQuantity(Long itemId, int quantity) {
        Long currentUserId = securityUtil.getCurrentUserId();

        CartItem cartItem = cartItemsRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart Item not found with id: " + itemId));

        Cart cart = cartItem.getCart();

        if (!cart.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to modify this cart item.");
        }

        Product product = productRepo.findById(ObjectIdUtils.toObjectId(cartItem.getProductId()))
                .orElseThrow(() -> new RuntimeException("Product not found in database."));

        int updatedQuantity = cartItem.getQuantity() + quantity;

        if (updatedQuantity <= 0) {
            // Remove item if resulting quantity is zero or less
            cartItemsRepo.delete(cartItem);
        } else {
            cartItem.setQuantity(updatedQuantity);
            BigDecimal productPrice = product.getDiscountedPrice();
            cartItem.setPrice(productPrice.multiply(BigDecimal.valueOf(updatedQuantity)));
            cartItemsRepo.save(cartItem);
        }

        // Recalculate cart total after update
        BigDecimal totalAmount = cart.getCartItems().stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalAmount(totalAmount);
        cartRepo.save(cart);
    }

    /* Remove cart item */
    public void removeCartItem(Long cartItemId){
        Long currentUserId = securityUtil.getCurrentUserId();

        CartItem cartItem = cartItemsRepo.findById(cartItemId).orElseThrow(() -> new RuntimeException("Cart item not found"));

        Cart cart = cartItem.getCart();

        if (!cart.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to delete this cart item.");
        }

        cart.getCartItems().remove(cartItem);
        cart.setTotalAmount(
                cart.getCartItems().stream()
                        .map(CartItem::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );
        cartItemsRepo.delete(cartItem);
        cartRepo.save(cart);
    }

    /* Clear cart */
    @Override
    public void clearCart(Long id) {

        Cart cart = cartRepo.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.getCartItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepo.save(cart);
    }


}
