package com.RotaDurak.RotaDurak.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "İsim boş olamaz")
    private String firstName;

    @NotBlank(message = "Soyisim boş olamaz")
    private String lastName;

    @NotBlank(message = "Telefon numarası boş olamaz")
    private String phoneNumber;

    @Email(message = "Geçerli bir e-posta giriniz")
    @NotBlank(message = "E-posta boş olamaz")
    private String email;

    @NotBlank(message = "Şifre boş olamaz")
    @Size(min=6, message = "Şifre en az 6 karakter olmalıdır.")
    private String password;
}
