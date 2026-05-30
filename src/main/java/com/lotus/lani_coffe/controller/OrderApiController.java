package com.lotus.lani_coffe.controller;

import com.lotus.lani_coffe.model.Order;
import com.lotus.lani_coffe.model.OrderItem;
import com.lotus.lani_coffe.model.User;
import com.lotus.lani_coffe.service.OrderService;
import com.lotus.lani_coffe.service.CouponService;
import com.lotus.lani_coffe.service.CartService;
import com.lotus.lani_coffe.model.CartItem;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    // DTO để nhận dữ liệu đặt hàng từ Client
    @Data
    public static class OrderRequest {
        private String customerName;
        private String customerPhone;
        private String shippingAddress;
        private String paymentMethod;
        private Double discount = 0.0;
        private String note;
        private Long couponId;
        private List<OrderItem> items;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest request, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("currentUser");
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập!");
            }
            
            // Lấy danh sách items từ DB thay vì từ request
            List<CartItem> cartItems = cartService.getCartItemsByUser(currentUser.getId());
            if (cartItems == null || cartItems.isEmpty()) {
                return ResponseEntity.badRequest().body("Giỏ hàng rỗng!");
            }
            
            // Chuyển đổi CartItem sang OrderItem
            List<OrderItem> orderItems = cartItems.stream().map(ci -> {
                OrderItem oi = new OrderItem();
                oi.setProductId(ci.getProductId());
                oi.setQuantity(ci.getQuantity());
                oi.setPrice(ci.getProduct().getSalePrice() != null ? ci.getProduct().getSalePrice() : ci.getProduct().getPrice());
                return oi;
            }).collect(java.util.stream.Collectors.toList());

            Order order = Order.builder()
                    .userId(currentUser.getId())
                    .customerName(request.getCustomerName())
                    .customerPhone(request.getCustomerPhone())
                    .shippingAddress(request.getShippingAddress())
                    .paymentMethod(request.getPaymentMethod())
                    .discount(request.getDiscount())
                    .note(request.getNote())
                    .couponId(request.getCouponId())
                    .tax(0.0)
                    .build();

            Order savedOrder = orderService.placeOrder(order, orderItems);
            
            // Xóa giỏ hàng sau khi đặt thành công
            cartService.clearCart(currentUser.getId());
            
            return ResponseEntity.ok(savedOrder);
        } catch (IllegalArgumentException e) {
            // Nghiệp vụ 1: Lỗi chặn đặt quá số lượng tồn kho (400 Bad Request)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    // Lấy thông tin chi tiết một đơn hàng kèm danh sách items
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long id) {
        return orderService.getOrderById(id).map(order -> {
            List<OrderItem> items = orderService.getOrderItems(id);
            Map<String, Object> response = new HashMap<>();
            response.put("order", order);
            response.put("items", items);
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Lấy danh sách đơn hàng (Admin xem hết, Customer xem của riêng mình)
    @GetMapping
    public ResponseEntity<?> getOrders(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập!");
        }

        if ("admin".equalsIgnoreCase(currentUser.getRole()) || "staff".equalsIgnoreCase(currentUser.getRole())) {
            return ResponseEntity.ok(orderService.getAllOrders());
        } else {
            return ResponseEntity.ok(orderService.getOrdersByUser(currentUser.getId()));
        }
    }

    // Nghiệp vụ 2: Cập nhật trạng thái đơn hàng (Duyệt vận chuyển hoặc Hủy đơn)
    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body, HttpSession session) {
        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isEmpty()) {
            return ResponseEntity.badRequest().body("Trạng thái mới không được để trống!");
        }

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập!");
        }

        try {
            // Nếu người dùng là customer, họ chỉ được phép chuyển trạng thái sang CANCELLED
            if (!"admin".equalsIgnoreCase(currentUser.getRole()) && !"staff".equalsIgnoreCase(currentUser.getRole())) {
                if (!"CANCELLED".equalsIgnoreCase(newStatus)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền chuyển sang trạng thái này!");
                }
            }

            Order updated = orderService.updateOrderStatus(id, newStatus);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            // Trả về cảnh báo khi không cho phép hủy đơn (ví dụ: đã CONFIRMED hoặc SHIPPING)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi cập nhật đơn hàng: " + e.getMessage());
        }
    }

    // Hủy đơn hàng trực tiếp từ Client (chỉ cho phép trạng thái PENDING)
    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập!");
        }
        try {
            Order order = orderService.getOrderById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại!"));
            if (!order.getUserId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền hủy đơn hàng này!");
            }
            Order updated = orderService.updateOrderStatus(id, "CANCELLED");
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hủy đơn hàng: " + e.getMessage());
        }
    }

    @Autowired
    private CouponService couponService;

    // API Áp Coupon giảm giá từ Database
    @GetMapping("/coupons/validate")
    public ResponseEntity<?> validateCoupon(@RequestParam String code, @RequestParam(defaultValue = "0") Double orderValue) {
        Map<String, Object> response = new HashMap<>();
        try {
            var couponOpt = couponService.validateCoupon(code, orderValue);
            if (couponOpt.isPresent()) {
                var coupon = couponOpt.get();
                response.put("valid", true);
                // Với coupon percent thì trả về trực tiếp, fixed thì quy đổi sang discount
                if ("PERCENTAGE".equalsIgnoreCase(coupon.getType())) {
                    response.put("discountPercent", coupon.getDiscountValue());
                    response.put("message", "Áp dụng thành công mã " + coupon.getCode() + "! Giảm " + coupon.getDiscountValue() + "% hóa đơn.");
                } else {
                    // Fixed amount quy sang discountPercent giả lập dựa trên orderValue hoặc xử lý trực tiếp ở client
                    double pct = (coupon.getDiscountValue() / orderValue) * 100;
                    response.put("discountPercent", Math.min(100.0, pct));
                    response.put("message", "Áp dụng thành công mã " + coupon.getCode() + "! Giảm " + coupon.getDiscountValue() + " đ.");
                }
            } else {
                response.put("valid", false);
                response.put("discountPercent", 0.0);
                response.put("message", "Mã giảm giá không hợp lệ hoặc đã hết hạn!");
            }
        } catch (IllegalArgumentException e) {
            response.put("valid", false);
            response.put("discountPercent", 0.0);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("valid", false);
            response.put("discountPercent", 0.0);
            response.put("message", "Lỗi kiểm tra mã giảm giá!");
        }

        return ResponseEntity.ok(response);
    }

    // API Thống kê doanh thu, đơn hàng dành riêng cho ADMIN
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không có quyền truy cập thống kê!");
        }

        try {
            List<Order> orders = orderService.getAllOrders();
            
            double totalRevenue = 0.0;
            long totalOrdersCount = orders.size();
            long completedOrdersCount = 0;
            long cancelledOrdersCount = 0;
            long pendingOrdersCount = 0;
            long confirmedOrdersCount = 0;
            long shippingOrdersCount = 0;

            double codRevenue = 0.0;
            double transferRevenue = 0.0;
            long codCount = 0;
            long transferCount = 0;

            for (Order order : orders) {
                String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PENDING";
                String payMethod = order.getPaymentMethod() != null ? order.getPaymentMethod().toUpperCase() : "COD";
                double amount = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;

                // Nhóm theo trạng thái đơn hàng
                switch (status) {
                    case "COMPLETED":
                        completedOrdersCount++;
                        totalRevenue += amount;
                        // Nhóm theo phương thức thanh toán đối với đơn thành công
                        if ("COD".equals(payMethod)) {
                            codRevenue += amount;
                            codCount++;
                        } else {
                            transferRevenue += amount;
                            transferCount++;
                        }
                        break;
                    case "CANCELLED":
                        cancelledOrdersCount++;
                        break;
                    case "PENDING":
                        pendingOrdersCount++;
                        break;
                    case "CONFIRMED":
                        confirmedOrdersCount++;
                        break;
                    case "SHIPPING":
                        shippingOrdersCount++;
                        break;
                }
            }

            double averageOrderValue = completedOrdersCount > 0 ? (totalRevenue / completedOrdersCount) : 0.0;

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalRevenue", totalRevenue);
            statistics.put("totalOrders", totalOrdersCount);
            
            // Nhóm trạng thái
            Map<String, Object> statusStats = new HashMap<>();
            statusStats.put("COMPLETED", completedOrdersCount);
            statusStats.put("CANCELLED", cancelledOrdersCount);
            statusStats.put("PENDING", pendingOrdersCount);
            statusStats.put("CONFIRMED", confirmedOrdersCount);
            statusStats.put("SHIPPING", shippingOrdersCount);
            statistics.put("statusGrouping", statusStats);

            // Nhóm thanh toán
            Map<String, Object> paymentStats = new HashMap<>();
            paymentStats.put("codCount", codCount);
            paymentStats.put("codRevenue", codRevenue);
            paymentStats.put("transferCount", transferCount);
            paymentStats.put("transferRevenue", transferRevenue);
            statistics.put("paymentGrouping", paymentStats);

            statistics.put("averageOrderValue", averageOrderValue);

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi tính toán thống kê: " + e.getMessage());
        }
    }
}
