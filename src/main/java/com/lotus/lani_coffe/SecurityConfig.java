package com.lotus.lani_coffe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("[SECURITY] Khởi tạo hệ thống Spring Security...");

        http
            .csrf(csrf -> csrf.disable()) // Vô hiệu hóa CSRF cho API AJAX
            .authorizeHttpRequests(auth -> auth
                // 1. Phân quyền truy cập giao diện Admin (Admin và Nhân viên đều xem được)
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "STAFF")

                // 2. Bảo vệ các trang cá nhân nhạy cảm của người dùng/khách hàng
                .requestMatchers("/checkout", "/my-orders").authenticated()

                // 3. API Thống kê doanh thu: Chỉ duy nhất ADMIN được quyền xem doanh thu nhạy cảm
                .requestMatchers("/api/orders/statistics").hasRole("ADMIN")

                // API thay đổi dữ liệu sản phẩm, danh mục, coupon: Chỉ Admin mới có quyền thêm/sửa/xóa
                .requestMatchers(HttpMethod.POST, "/api/products/**", "/api/categories/**", "/api/coupons/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**", "/api/categories/**", "/api/coupons/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**", "/api/categories/**", "/api/coupons/**").hasRole("ADMIN")
                
                // API duyệt đơn hàng (Cả Admin và Nhân viên pha chế - STAFF đều được quyền cập nhật trạng thái đơn)
                .requestMatchers(HttpMethod.POST, "/api/orders/*/status").hasAnyRole("ADMIN", "STAFF")
                
                // Các API quản trị người dùng (khóa/mở khóa tài khoản): Chỉ Admin
                .requestMatchers("/api/users/toggle/**").hasRole("ADMIN")

                // 4. Cho phép tất cả các trang/tài nguyên tĩnh khác được truy cập công khai
                .anyRequest().permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.warn("[SECURITY] [CẢNH BÁO] Từ chối quyền truy cập (403 Forbidden). URL: {}, IP: {}", 
                            request.getRequestURI(), request.getRemoteAddr());
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\": \"Bạn không có quyền truy cập tài nguyên này!\"}");
                })
                .authenticationEntryPoint((request, response, authException) -> {
                    log.warn("[SECURITY] Yêu cầu xác thực đăng nhập (401 Unauthorized) để truy cập URL: {}, IP: {}", 
                            request.getRequestURI(), request.getRemoteAddr());
                    // Nếu là truy cập trang giao diện HTML -> Redirect sang trang Login
                    String acceptHeader = request.getHeader("Accept");
                    if (acceptHeader != null && acceptHeader.contains("text/html")) {
                        response.sendRedirect("/login?redirect=" + request.getRequestURI());
                    } else {
                        // Nếu là API -> Trả về mã lỗi 401
                        response.setStatus(401);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"error\": \"Yêu cầu đăng nhập tài khoản!\"}");
                    }
                })
            );

        return http.build();
    }
}
