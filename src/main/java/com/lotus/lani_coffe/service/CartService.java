package com.lotus.lani_coffe.service;

import com.lotus.lani_coffe.model.CartItem;
import com.lotus.lani_coffe.model.Product;
import com.lotus.lani_coffe.repository.CartItemRepository;
import com.lotus.lani_coffe.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<CartItem> getCartItemsByUser(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Transactional
    public CartItem addToCart(Long userId, Long productId, Integer quantity) {
        // Kiểm tra sản phẩm có tồn tại không
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại!"));

        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(userId, productId);
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            // Kiểm tra tồn kho (tùy chọn)
            if (newQuantity > product.getStock()) {
                throw new IllegalArgumentException("Số lượng sản phẩm trong kho không đủ!");
            }
            item.setQuantity(newQuantity);
            return cartItemRepository.save(item);
        } else {
            if (quantity > product.getStock()) {
                throw new IllegalArgumentException("Số lượng sản phẩm trong kho không đủ!");
            }
            CartItem newItem = CartItem.builder()
                    .userId(userId)
                    .productId(productId)
                    .quantity(quantity)
                    .build();
            return cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public CartItem updateCartItemQuantity(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            cartItemRepository.deleteByUserIdAndProductId(userId, productId);
            return null;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại!"));

        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không có trong giỏ hàng!"));

        if (quantity > product.getStock()) {
            throw new IllegalArgumentException("Số lượng sản phẩm trong kho không đủ!");
        }

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}
