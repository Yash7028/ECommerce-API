package com.ecommerce.ecomapi.controller;

import com.ecommerce.ecomapi.dto.AddCartDto;
import com.ecommerce.ecomapi.dto.CartDto;
import com.ecommerce.ecomapi.dto.UpdateCartItemRequestDto;
import com.ecommerce.ecomapi.entity.Cart;
import com.ecommerce.ecomapi.service.CartService;
import com.ecommerce.ecomapi.utility.ObjectIdUtils;
import com.ecommerce.ecomapi.utility.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@PreAuthorize("hasRole('USER')")
@Tag(
        name = "05. Cart APIs",
        description = "Endpoints for managing the user's shopping cart, including adding, updating, removing products, and viewing cart contents."
)
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private SecurityUtil securityUtil;

    /* View cart by user id */
    @Operation(summary = "Get cart", description = "Get cart details. Only User can access.")
    @GetMapping("/get-cart")
    public ResponseEntity<?> viewCart() {
        try{
        CartDto cartDto = cartService.getCartByUserId(securityUtil.getCurrentUserId());
        return ResponseEntity.ok(cartDto);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart is empty or does not exist");
        }
    }

    /* Add item to cart */
    @Operation(
            summary = "Create or update cart",
            description = "Adds items to the user's cart. If the cart doesn't exist for the user, a new one will be created. "
                    + "If the item already exists in the cart, its quantity will be updated. The cart total is recalculated accordingly."
    )
    @PostMapping("/add-product")
    public ResponseEntity<?> addProductTOCart( @RequestBody AddCartDto addCartDto){
        Cart cart = cartService.addProuctToCart(securityUtil.getCurrentUserId(), ObjectIdUtils.toObjectId(addCartDto.getProductId()), addCartDto.getQuantity());
        if (cart != null){
            return ResponseEntity.ok(cart);
        }
        return new ResponseEntity<>("Something went to wrong will create cart.",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /* Update item quantity in cart*/
    @Operation(
            summary = "Update cart item quantity",
            description = "Updates the quantity of a specific item in the user's cart by item ID. "
                    + "If the item exists, its quantity is changed to the specified value. "
                    + "The cart total is recalculated accordingly."
    )
    @PutMapping("/item")
    public ResponseEntity<?> updateCardItemQunatity(@RequestBody UpdateCartItemRequestDto request){
        cartService.updateCartItemQuantity(request.getItemId(), request.getQuantity());
        return ResponseEntity.ok("Cart item quantity updated successfully.");
    }

    /* Delete cart item from cart */
    @Operation(
            summary = "Remove cart item",
            description = "Deletes a specific item from the user's cart by cart item ID. "
                    + "Only the authenticated owner of the cart is authorized to perform this action."
    )
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<String> removeItemByProductId(@PathVariable Long cartItemId) {
        cartService.removeCartItem(cartItemId);
        return ResponseEntity.ok("Item removed from cart successfully.");
    }

    /* Clear cart */
    @Operation(
            summary = "Clear user's cart",
            description = "Removes all items from the authenticated user's cart. "
                    + "Only the owner of the cart is authorized to perform this action."
    )
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart(securityUtil.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

}
