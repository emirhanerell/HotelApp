package com.antalyaotel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long id;

    @NotBlank(message = "Ad alanı zorunludur")
    @Size(min = 2, max = 50, message = "Ad 2-50 karakter arasında olmalıdır")
    private String firstName;

    @NotBlank(message = "Soyad alanı zorunludur")
    @Size(min = 2, max = 50, message = "Soyad 2-50 karakter arasında olmalıdır")
    private String lastName;

    @NotBlank(message = "E-posta alanı zorunludur")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String email;

    @NotBlank(message = "Şifre alanı zorunludur")
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
    private String password;

    @NotBlank(message = "Telefon numarası zorunludur")
    private String phoneNumber;

    @NotNull(message = "Doğum tarihi zorunludur")
    private LocalDate birthDate;

    @NotBlank(message = "Şehir alanı zorunludur")
    private String city;

    @NotBlank(message = "Ülke alanı zorunludur")
    private String country;

    @NotBlank(message = "Posta kodu zorunludur")
    private String postalCode;

    @NotBlank(message = "TC Kimlik No zorunludur")
    @Size(min = 11, max = 11, message = "TC Kimlik No 11 haneli olmalıdır")
    private String idNumber;
} 