package com.RotaDurak.RotaDurak.service;
import com.RotaDurak.RotaDurak.dto.LoginRequest;
import com.RotaDurak.RotaDurak.dto.LoginResponse;
import com.RotaDurak.RotaDurak.dto.RegisterRequest;
import com.RotaDurak.RotaDurak.model.User;
import com.RotaDurak.RotaDurak.repository.UserRepository;
import com.RotaDurak.RotaDurak.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String register(RegisterRequest request) {
        System.out.println("Register Request: " + request); // Log ekle

        if(userRepository.findByEmail(request.getEmail()) != null){
            throw new RuntimeException("Bu e-posta adresi zaten kullanılıyor.");
        }

        User newUser = new User();
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword())); //şifre hashleme

        System.out.println("User to be saved: " + newUser); // Log ekle

        userRepository.save(newUser);
        return "Kayıt başarıyla tamamlandı.";
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());

        if(user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Geçersiz email veya şifre");
        }

        String token = jwtService.generateToken(user.getEmail());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setEmail(user.getEmail());

        return response;
    }
}
