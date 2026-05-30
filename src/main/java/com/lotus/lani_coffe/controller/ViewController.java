package com.lotus.lani_coffe.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id) {
        return "product-detail";
    }

    @GetMapping("/cart")
    public String cart() {
        return "cart";
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "checkout";
    }

    @GetMapping("/order-tracking")
    public String orderTracking() {
        return "order-tracking";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/my-orders")
    public String myOrders() {
        return "my-orders";
    }

    // --- PHÂN HỆ QUẢN TRỊ (ADMIN VIEW) ---
    @GetMapping("/admin")
    public String adminIndex() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/products")
    public String adminProducts() {
        return "admin/products";
    }

    @GetMapping("/admin/categories")
    public String adminCategories() {
        return "admin/categories";
    }

    @GetMapping("/admin/orders")
    public String adminOrders() {
        return "admin/orders";
    }

    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin/users";
    }

    @GetMapping("/admin/reviews")
    public String adminReviews() {
        return "admin/reviews";
    }

    @GetMapping("/admin/coupons")
    public String adminCoupons() {
        return "admin/coupons";
    }
}
