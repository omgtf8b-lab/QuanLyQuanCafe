package com.lotus.lani_coffe.service;

import com.lotus.lani_coffe.model.Order;
import com.lotus.lani_coffe.model.OrderItem;
import com.lotus.lani_coffe.model.Product;
import com.lotus.lani_coffe.repository.OrderItemRepository;
import com.lotus.lani_coffe.repository.OrderRepository;
import com.lotus.lani_coffe.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    // Nghiệp vụ đặc thù 1: Logic Giảm Tồn Kho & Chặn Đặt Quá Số Lượng
    @Transactional
    public Order placeOrder(Order order, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng của bạn đang trống!");
        }

        double subtotal = 0.0;

        // Kiểm tra tồn kho của từng sản phẩm trước khi chốt đơn
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm mã " + item.getProductId() + " không tồn tại!"));

            if (!product.getAvailable()) {
                throw new IllegalArgumentException("Sản phẩm '" + product.getName() + "' hiện tại đã ngừng kinh doanh!");
            }

            // Chặn đặt quá số lượng tồn kho
            if (product.getStock() < item.getQuantity()) {
                throw new IllegalArgumentException("Sản phẩm '" + product.getName() + "' chỉ còn tồn kho " 
                    + product.getStock() + " gói, bạn không thể đặt mua " + item.getQuantity() + " gói!");
            }

            // Tính toán tổng phụ dựa trên giá bán online
            double activePrice = product.getSalePrice() != null ? product.getSalePrice() : product.getPrice();
            item.setPrice(activePrice);
            item.setName(product.getName());
            subtotal += activePrice * item.getQuantity();

            // Trừ tồn kho trong cơ sở dữ liệu
            product.setStock(product.getStock() - item.getQuantity());
            if (product.getStock() == 0) {
                product.setAvailable(false); // Hết hàng
            }
            productRepository.save(product);
        }

        // Thiết lập các thông số tài chính cho hóa đơn
        order.setSubtotal(subtotal);
        double total = subtotal - order.getDiscount() + order.getTax();
        order.setTotalAmount(Math.max(0.0, total));
        order.setStatus("PENDING"); // Bắt đầu ở trạng thái Chờ xác nhận
        order.setCreatedAt(LocalDateTime.now());

        // Lưu đơn hàng chính
        Order savedOrder = orderRepository.save(order);

        // Lưu chi tiết các mặt hàng của đơn
        for (OrderItem item : items) {
            item.setOrderId(savedOrder.getId());
            orderItemRepository.save(item);
        }

        return savedOrder;
    }

    // Nghiệp vụ đặc thù 2: Quy trình Quản lý Trạng thái Vận chuyển
    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại!"));

        String currentStatus = order.getStatus().toUpperCase();
        newStatus = newStatus.toUpperCase();

        // Kiểm tra các trạng thái hợp lệ
        if (!List.of("PENDING", "CONFIRMED", "SHIPPING", "COMPLETED", "CANCELLED").contains(newStatus)) {
            throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ!");
        }

        // Chặn hủy đơn hàng khi đã xác nhận hoặc đang giao
        if (newStatus.equals("CANCELLED")) {
            if (!currentStatus.equals("PENDING")) {
                throw new IllegalStateException("Không thể hủy đơn hàng vì đơn hàng đã được " 
                    + (currentStatus.equals("CONFIRMED") ? "XÁC NHẬN" : "ĐANG VẬN CHUYỂN") + "!");
            }
            
            // Nếu hủy đơn hàng thành công, hoàn lại tồn kho cho các sản phẩm
            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
            for (OrderItem item : items) {
                productRepository.findById(item.getProductId()).ifPresent(product -> {
                    product.setStock(product.getStock() + item.getQuantity());
                    product.setAvailable(true);
                    productRepository.save(product);
                });
            }
        }

        // Cập nhật trạng thái mới
        order.setStatus(newStatus);

        // Nếu hoàn thành giao hàng, ghi nhận completedAt
        if (newStatus.equals("COMPLETED")) {
            order.setCompletedAt(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }
}
