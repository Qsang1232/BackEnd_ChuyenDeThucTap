package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthenticationResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.service.AuthenticationService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(@RequestBody RegisterRequest request) {
        User newUser = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(Role.USER)
                .build();

        String token = authenticationService.register(newUser);

        return ResponseEntity.ok(ApiResponse.<AuthenticationResponse>builder()
                .success(true)
                .message("Đăng ký thành công")
                .data(new AuthenticationResponse(token))
                .build());
    }

    // Bạn cần inject thêm UserRepository vào Controller này
    private final com.example.demo.repository.UserRepository userRepository; // <--- Thêm dòng này

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> authenticate(@RequestBody AuthRequest request) {
        // 1. Xác thực và lấy Token
        String token = authenticationService.authenticate(request.getUsername(), request.getPassword());
        
        // 2. Lấy thông tin User để biết Role
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Trả về cả Token và Role
        return ResponseEntity.ok(Map.of(
            "token", token,
            "role", user.getRole().name(), // <--- QUAN TRỌNG: Gửi role về
            "name", user.getUsername()
        ));
    }
}