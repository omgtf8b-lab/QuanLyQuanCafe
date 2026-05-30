package com.lotus.lani_coffe.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải dài từ 3 đến 50 ký tự")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng Email không hợp lệ")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Column(nullable = false)
    private String password;

    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0")
    private String phone;

    @NotBlank(message = "Vai trò người dùng không được để trống")
    @Column(nullable = false)
    private String role; // "admin" hoặc "customer"

    @Column(nullable = false)
    private Boolean active = true; // Cho phép khóa/mở khóa tài khoản
}
