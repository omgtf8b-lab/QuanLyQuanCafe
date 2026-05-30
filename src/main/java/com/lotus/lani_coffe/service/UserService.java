package com.lotus.lani_coffe.service;

import com.lotus.lani_coffe.model.User;
import com.lotus.lani_coffe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Mã hóa mật khẩu sử dụng SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi mã hóa mật khẩu", e);
        }
    }

    // Đăng ký tài khoản mới
    public User registerUser(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống!");
        }
        if (user.getEmail() == null || !user.getEmail().matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            throw new IllegalArgumentException("Định dạng Email không hợp lệ!");
        }
        if (user.getPhone() == null || !user.getPhone().matches("^0[0-9]{9}$")) {
            throw new IllegalArgumentException("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0!");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Tài khoản đã tồn tại!");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải dài từ 6 ký tự trở lên!");
        }
        // Mã hóa mật khẩu trước khi lưu vào DB
        user.setPassword(hashPassword(user.getPassword()));
        user.setActive(true);
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("customer");
        }
        return userRepository.save(user);
    }

    // Đăng nhập tài khoản
    public Optional<User> loginUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.getActive()) {
                throw new IllegalStateException("Tài khoản này đã bị khóa!");
            }
            String hashedInput = hashPassword(password);
            if (user.getPassword().equals(hashedInput)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    // Khóa hoặc mở khóa tài khoản
    public User toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));
        user.setActive(!user.getActive());
        return userRepository.save(user);
    }
}
