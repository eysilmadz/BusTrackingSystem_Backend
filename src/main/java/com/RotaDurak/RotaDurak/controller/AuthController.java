package com.RotaDurak.RotaDurak.controller;

import com.RotaDurak.RotaDurak.dto.LoginRequest;
import com.RotaDurak.RotaDurak.dto.LoginResponse;
import com.RotaDurak.RotaDurak.dto.RegisterRequest;
import com.RotaDurak.RotaDurak.model.User;
import com.RotaDurak.RotaDurak.repository.UserRepository;
import com.RotaDurak.RotaDurak.security.JwtService;
import com.RotaDurak.RotaDurak.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") //tüm domainlere izin ver
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if(authHeader==null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        String token = authHeader.substring(7); //Bearer kısmını çıkar
        String userEmail = jwtService.extractEmail(token); //emaili al
        User user = userRepository.findByEmail(userEmail);

        if(user==null) {
            return ResponseEntity.notFound().build();
        }

        user.setPassword(null); //Şifreyi gönderme
        return ResponseEntity.ok(user);
    }
}
