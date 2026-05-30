package com.lotus.lani_coffe.controller;

import com.lotus.lani_coffe.model.User;
import com.lotus.lani_coffe.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private static final Logger log = LoggerFactory.getLogger(UserApiController.class);

    @Autowired
    private UserService userService;

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    // Đăng ký khách hàng mới
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            user.setRole("customer");
            User saved = userService.registerUser(user);
            log.info("[SECURITY] Đăng ký thành công tài khoản mới: {}", user.getUsername());
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Đăng nhập tài khoản
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, 
                                   HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   HttpSession session) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            Optional<User> userOpt = userService.loginUser(username, password);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Lưu thông tin đăng nhập vào Session cho frontend cũ dùng
                session.setAttribute("currentUser", user);

                // Đồng bộ hóa với Spring Security Context để phân quyền
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    user, null, AuthorityUtils.createAuthorityList("ROLE_" + user.getRole().toUpperCase())
                );
                
                // Spring Security 6 yêu cầu lưu trữ rõ ràng Context vào Repository
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);
                securityContextRepository.saveContext(context, request, response);

                log.info("[SECURITY] Người dùng [{}] đăng nhập thành công với vai trò [{}]. IP: {}, Session: {}", 
                    user.getUsername(), user.getRole(), request.getRemoteAddr(), session.getId());
                return ResponseEntity.ok(user);
            }
            log.warn("[SECURITY] Đăng nhập thất bại cho tài khoản [{}]. Sai thông tin.", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tài khoản hoặc mật khẩu không chính xác!");
        } catch (IllegalStateException e) {
            log.warn("[SECURITY] Tài khoản bị khóa đăng nhập: {}", username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // Đăng xuất
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            log.info("[SECURITY] Người dùng [{}] đăng xuất thành công.", currentUser.getUsername());
        }
        SecurityContextHolder.clearContext();
        session.invalidate();
        return ResponseEntity.ok("Đã đăng xuất thành công!");
    }

    // Lấy thông tin tài khoản hiện tại đang đăng nhập
    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập!");
        }
        return ResponseEntity.ok(currentUser);
    }

    // Danh sách tất cả người dùng (Admin)
    @GetMapping
    public ResponseEntity<?> getAllUsers(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không có quyền truy cập!");
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Khóa/mở khóa tài khoản (Admin)
    @PostMapping("/toggle/{id}")
    public ResponseEntity<?> toggleActive(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không có quyền thực hiện!");
        }
        try {
            User updated = userService.toggleUserActive(id);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
