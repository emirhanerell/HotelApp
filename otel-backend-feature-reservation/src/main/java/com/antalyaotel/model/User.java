package com.antalyaotel.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.Valid;


import jakarta.persistence.*;
import lombok.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;



@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

    @Enumerated(EnumType.STRING) // Enum olarak kaydet
    @Column(nullable = false)
    private Role role;

    //private String phoneNumber;

    //public String getPhoneNumber() {
      //  return phoneNumber;
    //}
    //public void setPhoneNumber(String phoneNumber) {
     //   this.phoneNumber = phoneNumber;
   // }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    @NotBlank(message = "Name cannot be empty")
    private String name;
    //private String phoneNumber; // Telefon numarası alanı eklendi

    @JsonIgnore
    @Column(nullable = false)
    private String password;


@Enumerated(EnumType.STRING)

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.toString())); // ENUM'un string halini kullan
    }



    @Override
    public String getUsername() {
        return email; // Username olarak email dönüyor
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Hesabın süresi dolmamış
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Hesap kilitli değil
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Şifre süresi dolmamış
    }

    @Override
    public boolean isEnabled() {
        return true; // Kullanıcı aktif durumda
    }

}
